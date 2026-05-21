package core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    static final int COL_HOURLY_RATE = 18;

    // TODO: replace soon for Employee/AttendanceRecord classes and refactor code
    private String[][] employees;
    private String[][] attendance;

    public FileHandler() throws IOException {
        loadEmployeeRecords();
        loadAttendanceRecords();
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
                String[] columns = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                // Guard against malformed rows with too few columns.
                if (columns.length < 19) continue;

                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].replace("\"", "").trim();
                }

                // Remove comma separators from the hourly rate field (e.g. "1,250" → "1250").
                columns[COL_HOURLY_RATE] = columns[COL_HOURLY_RATE].replace(",", "");

                rows.add(columns);
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
