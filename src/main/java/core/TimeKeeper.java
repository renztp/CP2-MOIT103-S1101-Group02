package core;

import models.CSVColumnIndex;

public class TimeKeeper {
    public int getYearFromAttendance(String[][] attendance, String employeeId) {
        for (String[] row : attendance) {
            if (!row[CSVColumnIndex.ATT_EMP_ID].trim().equals(employeeId)) continue;
            String[] parts = row[CSVColumnIndex.ATT_DATE].split("/");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2].trim());
            }
        }
        return 2024;
    }

    public double parseAmount(String value) {
        return Double.parseDouble(value.replace("\"", "").replace(",", "").trim());
    }

    public static int[] parseTime(String time) {
        String[] parts = time.trim().split(":");
        return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        };
    }

    public double calculateHoursWorked(
            String[][] attendance,
            String employeeId,
            int month,
            int startDay,
            int endDay
    ) {
        double totalMinutesWorked = 0;

        for (String[] row : attendance) {

            // Skip rows for other employees.
            if (!row[CSVColumnIndex.ATT_EMP_ID].trim().equals(employeeId)) continue;

            // Guard against malformed rows.
            if (row.length <= CSVColumnIndex.ATT_TIME_OUT) continue;

            String[] dateParts = row[CSVColumnIndex.ATT_DATE].split("/");
            if (dateParts.length < 2) continue;

            int recordMonth = Integer.parseInt(dateParts[0].trim());
            int recordDay   = Integer.parseInt(dateParts[1].trim());

            // Skip records outside the target month and cutoff window.
            if (recordMonth != month) continue;
            if (recordDay < startDay || recordDay > endDay) continue;

            int[] loginTime  = parseTime(row[CSVColumnIndex.ATT_TIME_IN]);
            int[] logoutTime = parseTime(row[CSVColumnIndex.ATT_TIME_OUT]);

            int loginMinutes  = loginTime[0]  * 60 + loginTime[1];
            int logoutMinutes = logoutTime[0] * 60 + logoutTime[1];

            // Grace period: log-in at or before 8:10 AM counts as exactly 8:00 AM.
            if (loginMinutes <= 8 * 60 + 10) {
                loginMinutes = 8 * 60;
            }

            // Floor: work cannot start before 8:00 AM.
            if (loginMinutes < 8 * 60) {
                loginMinutes = 8 * 60;
            }

            // Cap: work stops counting at 5:00 PM.
            if (logoutMinutes > 17 * 60) {
                logoutMinutes = 17 * 60;
            }

            // Deduct 60 minutes for the mandatory lunch break.
            int minutesWorked = logoutMinutes - loginMinutes - 60;

            if (minutesWorked > 0) {
                totalMinutesWorked += minutesWorked;
            }
        }

        // Convert total minutes to hours.
        return totalMinutesWorked / 60.0;
    }
    public String getMonthName(int month) {
        String[] names = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return names[month];
    }

    public int getLastDayOfMonth(int month, int year) {
        if (month == 2) {
            boolean isLeapYear = (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
            return isLeapYear ? 29 : 28;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) return 30;
        return 31;
    }
}
