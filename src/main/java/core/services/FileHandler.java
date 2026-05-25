package core.services;

import core.models.Attendance;
import core.models.Employee;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileHandler {

    public static final String EMPLOYEE_FILE_PATH = "Employee Details.csv";
    public static final String ATTENDANCE_FILE_PATH = "Attendance Record.csv";

    public List<Employee> loadEmployees() throws Exception {
        return loadEmployeesFromFile(EMPLOYEE_FILE_PATH);
    }

    public List<Attendance> loadAttendance() throws Exception {
        return loadAttendanceFromFile(ATTENDANCE_FILE_PATH);
    }

    public List<Employee> loadEmployeesFromFile(String filePath) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(is))) {
            return csvReader.lines()
                    .skip(1)
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

    public List<Attendance> loadAttendanceFromFile(String filePath) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        try (BufferedReader csvReader = new BufferedReader(new InputStreamReader(is))) {
            return csvReader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(columns -> columns.length >= Attendance.ATT_MINIMUM_REQUIRED_COLUMNS)
                    .map(Attendance::fromCsvRow)
                    .collect(Collectors.toList());
        }
    }

    public Employee findEmployeeById(List<Employee> employeeRecords, String targetId) {
        Optional<Employee> matchedEmployee = employeeRecords.stream()
                .filter(employee -> employee.getId().equals(targetId))
                .findFirst();
        return matchedEmployee.orElse(null);
    }
}
