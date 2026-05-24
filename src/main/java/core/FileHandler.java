package core;

import models.CSVColumnIndex;
import models.Employee;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class FileHandler {
    static final int COL_HOURLY_RATE = 18;

    // TODO: replace for Employee/AttendanceRecord classes and refactor code
    private String[][] employees;
    private String[][] attendance;
    private LinkedHashMap<Integer, Employee> allEmployees = new LinkedHashMap<>();

    public FileHandler() throws IOException {
        loadEmployeeRecords();
//        loadAttendanceRecords();
    }

    private String removeQuotes(String column) {
        return column.replace("\"", "").trim();
    }

    private String[] splitCSVLine(String line) {
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }

    private void setEmployeeRecords(String[] column) {
        Employee singleEmployee = new Employee(
                Integer.parseInt(column[CSVColumnIndex.COL_EMP_ID]),
                column[CSVColumnIndex.COL_LAST_NAME],
                column[CSVColumnIndex.COL_FIRST_NAME],
                column[CSVColumnIndex.COL_ROLE],
                column[CSVColumnIndex.COL_ROLE],
                Double.parseDouble(column[CSVColumnIndex.COL_HOURLY_RATE]),
                Double.parseDouble(column[CSVColumnIndex.COL_RICE_SUBSIDY]),
                Double.parseDouble(column[CSVColumnIndex.COL_PHONE_ALLOWANCE]),
                Double.parseDouble(column[CSVColumnIndex.COL_CLOTHING_ALLOWANCE])
        );
        int empId = singleEmployee.getEmployeeNumber();
        allEmployees.put(empId, singleEmployee);
    }

    private void loadEmployeeRecords() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("Employee Details.csv");
        List<String[]> rows = new ArrayList<>();
        String line;
        boolean header = true;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // Skip the column name row.

                // Regex splits on commas that are NOT inside double-quote pairs.
                String[] columns = splitCSVLine(line);

                // Guard against malformed rows with too few columns.
                if (columns.length < 19) continue;


                for (int i = 0; i < columns.length; i++) {
                    columns[i] = removeQuotes(columns[i]);
                }

                // Remove comma separators from the hourly rate field (e.g. "1,250" → "1250").
                columns[CSVColumnIndex.COL_HOURLY_RATE] = columns[CSVColumnIndex.COL_HOURLY_RATE].replace(",", "");
                rows.add(columns);
                System.out.println(columns[CSVColumnIndex.COL_HOURLY_RATE]);
                setEmployeeRecords(singleEmployee);
//                allEmployees.add(columns);
            }
        }
        employees = rows.toArray(new String[0][]);
    }

    private void loadAttendanceRecords() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("Attendance Record.csv");
        List<String[]> rows = new ArrayList<>();
        String line;
        boolean header = true;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            while ((line = reader.readLine()) != null) {
                if (header) { header = false; continue; } // Skip the column name row.

                String[] columns = line.split(",");

                // Guard against malformed rows with too few columns.
                if (columns.length < 6) continue;

                rows.add(columns);
            }
        }

        attendance = rows.toArray(new String[0][]);
    }

    public String[][] getEmployeesRecords() { return employees; }
    public String[][] getAttendanceRecords() { return attendance; }
}
