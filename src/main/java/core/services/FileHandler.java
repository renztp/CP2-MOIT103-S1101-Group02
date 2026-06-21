package core.services;

import core.models.Attendance;
import core.models.Employee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

// Handles reading and writing of employee and attendance CSV files.
// On first run, the bundled CSV files are copied to a writable folder in the user's home directory.
// All subsequent reads and writes go to that folder so changes persist across sessions.
public class FileHandler {

    // Folder name created in the user's home directory to store writable data files.
    private static final String DATA_FOLDER_NAME = "MotorPH-Data";

    public static final String EMPLOYEE_FILE_NAME = "Employee Details.csv";
    public static final String ATTENDANCE_FILE_NAME = "Attendance Record.csv";

    private static final String CSV_HEADER =
            "Employee #,Last Name,First Name,Birthday,Address,Phone Number," +
            "SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position," +
            "Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance," +
            "Clothing Allowance,Gross Semi-monthly Rate,Hourly Rate," +
            "Days Worked,Gross Pay,SSS,PhilHealth,Pag-IBIG,Withholding Tax," +
            "Total Deductions,Net Pay";

    // In-memory cache; populated on first load, kept in sync with disk after every write.
    private List<Employee> employeeCache = null;

    // Returns the writable data folder path, creating it if it doesn't exist yet.
    private Path getDataFolder() throws IOException {
        Path folder = Paths.get(System.getProperty("user.home"), DATA_FOLDER_NAME);
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        return folder;
    }

    // Returns the writable path for a given file name.
    // Copies from the classpath if missing, or if the employee file has a corrupted header.
    private Path getWritablePath(String fileName) throws IOException {
        Path dataFile = getDataFolder().resolve(fileName);
        if (!Files.exists(dataFile) || (fileName.equals(EMPLOYEE_FILE_NAME) && hasCorruptedHeader(dataFile))) {
            copyFromClasspath(fileName, dataFile);
            if (fileName.equals(EMPLOYEE_FILE_NAME)) {
                employeeCache = null;
            }
        }
        return dataFile;
    }

    // Returns true if the employee CSV on disk has the wrong header format.
    private boolean hasCorruptedHeader(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            return header != null && !header.trim().endsWith("Net Pay");
        } catch (IOException e) {
            return true;
        }
    }

    // Copies a bundled resource file to the given destination path.
    private void copyFromClasspath(String resourceName, Path destination) throws IOException {
        try (InputStream source = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (source == null) {
                throw new IOException("Bundled resource not found: " + resourceName);
            }
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Loads employees from the writable CSV file, using the in-memory cache after the first load.
    public List<Employee> loadEmployees() throws Exception {
        if (employeeCache == null) {
            Path filePath = getWritablePath(EMPLOYEE_FILE_NAME);
            employeeCache = parseEmployeeFile(filePath);
        }
        return new ArrayList<>(employeeCache);
    }

    // Loads attendance records from the writable CSV file.
    public List<Attendance> loadAttendance() throws Exception {
        Path filePath = getWritablePath(ATTENDANCE_FILE_NAME);
        return parseAttendanceFile(filePath);
    }

    // Parses the employee CSV file at the given path into a list of Employee objects.
    private List<Employee> parseEmployeeFile(Path filePath) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                    .filter(columns -> columns.length >= Employee.COL_MINIMUM_REQUIRED_COLUMNS)
                    .map(columns -> {
                        String[] cleaned = Arrays.stream(columns)
                                .map(cell -> cell.replace("\"", "").trim())
                                .toArray(String[]::new);
                        cleaned[Employee.COL_HOURLY_RATE] = cleaned[Employee.COL_HOURLY_RATE].replace(",", "");
                        return cleaned;
                    })
                    .map(Employee::fromCsvRow)
                    .collect(Collectors.toList());
        }
    }

    // Parses the attendance CSV file at the given path into a list of Attendance objects.
    private List<Attendance> parseAttendanceFile(Path filePath) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            return reader.lines()
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty())
                    .map(line -> line.split(","))
                    .filter(columns -> columns.length >= Attendance.ATT_MINIMUM_REQUIRED_COLUMNS)
                    .map(Attendance::fromCsvRow)
                    .collect(Collectors.toList());
        }
    }

    // Writes the current in-memory employee list back to the CSV file on disk.
    private void persistEmployeesToDisk() throws Exception {
        Path filePath = getWritablePath(EMPLOYEE_FILE_NAME);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(CSV_HEADER);
            writer.newLine();
            for (Employee emp : employeeCache) {
                writer.write(buildCsvRow(emp));
                writer.newLine();
            }
        }
    }

    // Converts an Employee back to a CSV row string, quoting fields that contain commas.
    private String buildCsvRow(Employee emp) {
        emp.ensureSalaryColumns();
        String[] values = new String[Employee.COL_TOTAL_COLUMNS];
        for (int i = 0; i < values.length; i++) {
            String val = emp.getValueAt(i);
            values[i] = val.contains(",") ? "\"" + val + "\"" : val;
        }
        return String.join(",", values);
    }

    // Saves computed salary values for all employees and writes them to the CSV file.
    public void saveSalaryResults(SalaryComputationModule.SalaryResult result) throws Exception {
        if (employeeCache == null) {
            loadEmployees();
        }

        for (int i = 0; i < result.employeeIds.length; i++) {
            String employeeId = result.employeeIds[i];
            for (Employee employee : employeeCache) {
                if (employee.getId().equals(employeeId)) {
                    employee.setSalaryFields(
                            result.daysWorked[i],
                            result.grossPay[i],
                            result.sss[i],
                            result.philHealth[i],
                            result.pagIbig[i],
                            result.withholdingTax[i],
                            result.totalDeductions[i],
                            result.netPay[i]
                    );
                    break;
                }
            }
        }

        persistEmployeesToDisk();
    }

    public Employee findEmployeeById(List<Employee> employeeRecords, String targetId) {
        return employeeRecords.stream()
                .filter(employee -> employee.getId().equals(targetId))
                .findFirst()
                .orElse(null);
    }

    // Adds a new employee to the cache and saves to disk.
    public void addEmployee(Employee newEmployee) throws Exception {
        if (employeeCache == null) loadEmployees();
        employeeCache.add(newEmployee);
        persistEmployeesToDisk();
    }

    // Replaces an existing employee in the cache and saves to disk.
    public void updateEmployee(Employee updatedEmployee) throws Exception {
        if (employeeCache == null) loadEmployees();
        for (int i = 0; i < employeeCache.size(); i++) {
            if (employeeCache.get(i).getId().equals(updatedEmployee.getId())) {
                employeeCache.set(i, updatedEmployee);
                break;
            }
        }
        persistEmployeesToDisk();
    }

    // Removes an employee from the cache and saves to disk.
    public void deleteEmployee(String employeeId) throws Exception {
        if (employeeCache == null) loadEmployees();
        employeeCache.removeIf(emp -> emp.getId().equals(employeeId));
        persistEmployeesToDisk();
    }

    // Returns true if an employee with the given ID already exists.
    public boolean employeeIdExists(String employeeId) throws Exception {
        return loadEmployees().stream()
                .anyMatch(emp -> emp.getId().equals(employeeId.trim()));
    }

    // Searches employees by ID, first name, last name, or position (case-insensitive).
    public List<Employee> searchEmployees(String query) throws Exception {
        String lowerQuery = query.toLowerCase().trim();
        return loadEmployees().stream()
                .filter(emp ->
                        emp.getId().toLowerCase().contains(lowerQuery) ||
                        emp.getFirstName().toLowerCase().contains(lowerQuery) ||
                        emp.getLastName().toLowerCase().contains(lowerQuery) ||
                        emp.getPosition().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}
