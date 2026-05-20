import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * MotorPH Payroll System — Combined Best-of-Three
 * ----------------------------------------
 * Features:
 * - Login authentication (employee / payroll_staff)
 * - Employee record lookup
 * - Payroll computation for June through December
 * - Government deductions: SSS, PhilHealth, Pag-IBIG, Withholding Tax
 *
 * Business Rules:
 * - Only hours between 8:00 AM and 5:00 PM are counted
 * - Grace period: log-in from 8:01 to 8:10 is treated as exactly 8:00 AM
 * - 1-hour lunch break is deducted per day
 * - Deductions are computed from the combined monthly gross (cutoff 1 + cutoff 2)
 * - First cutoff net  = first cutoff gross (no deductions applied yet)
 * - Second cutoff net = second cutoff gross minus all monthly deductions
 */
public class MotorPH {

    // ============================================================
    // CSV COLUMN INDEX CONSTANTS (avoids magic numbers throughout)
    // ============================================================
    static final int COL_EMP_ID      = 0;
    static final int COL_LAST_NAME   = 1;
    static final int COL_FIRST_NAME  = 2;
    static final int COL_BIRTHDAY    = 3;
    static final int COL_HOURLY_RATE = 18;

    static final int ATT_EMP_ID   = 0;
    static final int ATT_DATE     = 3;
    static final int ATT_TIME_IN  = 4;
    static final int ATT_TIME_OUT = 5;

    // ============================================================
    // MAIN — Login, then route to the correct menu
    // ============================================================
    public static void main(String[] args) {
        String employeeFile   = "Employee Details.csv";
        String attendanceFile = "Attendance Record.csv";

        Scanner scanner = new Scanner(System.in);

        System.out.println("======================================");
        System.out.println("        MOTORPH PAYROLL SYSTEM        ");
        System.out.println("======================================");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (!isValidLogin(username, password)) {
            System.out.println("Incorrect username and/or password. Exiting.");
            scanner.close();
            return;
        }

        try {
            // Load both CSV files into memory once so we don't re-read disk on every lookup.
            String[][] employees  = loadEmployees(employeeFile);
            String[][] attendance = loadAttendance(attendanceFile);

            if (username.equals("employee")) {
                runEmployeeMenu(scanner, employees);
            } else {
                runPayrollStaffMenu(scanner, employees, attendance);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println("\nExiting program...");
        scanner.close();
    }

    // ============================================================
    // AUTHENTICATION
    // Valid usernames: "employee", "payroll_staff"
    // Valid password:  "12345"
    // ============================================================
    static boolean isValidLogin(String username, String password) {
        boolean validUser = username.equals("employee") || username.equals("payroll_staff");
        boolean validPass = password.equals("12345");
        return validUser && validPass;
    }

    // ============================================================
    // EMPLOYEE MENU
    // Employees can view their own basic details only.
    // ============================================================
    static void runEmployeeMenu(Scanner scanner, String[][] employees) {
        boolean active = true;
        while (active) {
            System.out.println("\n======================================");
            System.out.println("         MOTORPH EMPLOYEE MENU        ");
            System.out.println("======================================");
            System.out.println("1. View my employee details");
            System.out.println("2. Exit");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter Employee Number: ");
                    String empId = scanner.nextLine().trim();
                    String[] employee = findEmployee(employees, empId);

                    if (employee == null) {
                        System.out.println("Employee number does not exist.");
                    } else {
                        printEmployeeHeader(employee);
                    }
                    active = false;
                    break;

                case "2":
                    active = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }

    // ============================================================
    // PAYROLL STAFF MENU
    // Payroll staff can run payroll for one employee or all employees.
    // ============================================================
    static void runPayrollStaffMenu(Scanner scanner, String[][] employees, String[][] attendance) {
        boolean active = true;
        while (active) {
            System.out.println("\n======================================");
            System.out.println("      MOTORPH PAYROLL STAFF MENU      ");
            System.out.println("======================================");
            System.out.println("1. Process Single Employee Payslip");
            System.out.println("2. Generate All-Employee Summary");
            System.out.println("3. Logout");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter Employee Number: ");
                    String empId = scanner.nextLine().trim();
                    String[] employee = findEmployee(employees, empId);

                    if (employee == null) {
                        System.out.println("Employee number does not exist.");
                    } else {
                        processEmployeePayroll(employee, attendance);
                    }
                    active = false;
                    break;

                case "2":
                    for (String[] emp : employees) {
                        processEmployeePayroll(emp, attendance);
                        System.out.println("\n" + "=".repeat(40));
                    }
                    active = false;
                    break;

                case "3":
                    active = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    // ============================================================
    // PAYROLL PROCESSING — June (6) to December (12)
    // For each month, computes both cutoffs then applies deductions.
    // ============================================================
    static void processEmployeePayroll(String[] employee, String[][] attendance) {
        printEmployeeHeader(employee);

        double hourlyRate = parseAmount(employee[COL_HOURLY_RATE]);

        // Read the year from the attendance data so leap years are handled correctly.
        int year = getYearFromAttendance(attendance, employee[COL_EMP_ID]);

        // Always loop June through December. Months with no attendance still display
        // with zero values so no month is ever silently skipped.
        for (int month = 6; month <= 12; month++) {
            int lastDay = getLastDayOfMonth(month, year);

            // Calculate hours per cutoff using minute-level precision for accuracy.
            double firstCutoffHours  = calculateWorkedHours(attendance, employee[COL_EMP_ID], month, 1, 15);
            double secondCutoffHours = calculateWorkedHours(attendance, employee[COL_EMP_ID], month, 16, lastDay);

            // Compute all payroll values — deductions based on combined monthly gross.
            double[] payroll = calculateMonthlyPayroll(firstCutoffHours, secondCutoffHours, hourlyRate);

            printPayrollMonth(month, lastDay, firstCutoffHours, secondCutoffHours, payroll);
        }
    }

    // ============================================================
    // HOURS WORKED CALCULATION
    // Uses integer minutes throughout to avoid floating-point drift.
    //
    // Rules:
    // - Log-in at or before 8:10 AM is treated as exactly 8:00 AM (grace period).
    // - Log-in before 8:00 AM is floored to 8:00 AM (no credit for early arrival).
    // - Log-out after 5:00 PM is capped at 5:00 PM (no overtime counted).
    // - 60 minutes are deducted per day for the mandatory lunch break.
    // ============================================================
    static double calculateWorkedHours(
            String[][] attendance,
            String employeeId,
            int month,
            int startDay,
            int endDay) {

        double totalMinutesWorked = 0;

        for (String[] row : attendance) {

            // Skip rows for other employees.
            if (!row[ATT_EMP_ID].trim().equals(employeeId)) continue;

            // Guard against malformed rows.
            if (row.length <= ATT_TIME_OUT) continue;

            String[] dateParts = row[ATT_DATE].split("/");
            if (dateParts.length < 2) continue;

            int recordMonth = Integer.parseInt(dateParts[0].trim());
            int recordDay   = Integer.parseInt(dateParts[1].trim());

            // Skip records outside the target month and cutoff window.
            if (recordMonth != month) continue;
            if (recordDay < startDay || recordDay > endDay) continue;

            int[] loginTime  = parseTime(row[ATT_TIME_IN]);
            int[] logoutTime = parseTime(row[ATT_TIME_OUT]);

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

    // ============================================================
    // PAYROLL CALCULATION
    // Step 1: Add both cutoff gross amounts.
    // Step 2: Compute SSS, PhilHealth, Pag-IBIG from combined total.
    // Step 3: Compute withholding tax on (combined total - gov deductions).
    // Step 4: First cutoff net = gross (no deductions yet).
    //         Second cutoff net = gross - all deductions.
    //
    // Returns double[] where:
    // [0] first cutoff gross,   [1] second cutoff gross,
    // [2] total monthly gross,  [3] SSS,
    // [4] PhilHealth,           [5] Pag-IBIG,
    // [6] withholding tax,      [7] total deductions,
    // [8] first cutoff net,     [9] second cutoff net
    // ============================================================
    static double[] calculateMonthlyPayroll(double firstCutoffHours, double secondCutoffHours, double hourlyRate) {

        double firstCutoffGross  = firstCutoffHours  * hourlyRate;
        double secondCutoffGross = secondCutoffHours * hourlyRate;

        // Step 1: Combine both cutoffs before any deduction is computed.
        double totalMonthlyGross = firstCutoffGross + secondCutoffGross;

        // Step 2: Government deductions based on combined monthly gross.
        double sss        = (totalMonthlyGross > 0) ? computeSSS(totalMonthlyGross)        : 0.0;
        double philHealth = (totalMonthlyGross > 0) ? computePhilHealth(totalMonthlyGross)  : 0.0;
        double pagIbig    = (totalMonthlyGross > 0) ? computePagIbig(totalMonthlyGross)     : 0.0;

        // Step 3: Withholding tax on income after mandatory deductions.
        double totalGovDeductions = sss + philHealth + pagIbig;
        double taxableIncome      = totalMonthlyGross - totalGovDeductions;
        double withholdingTax     = (taxableIncome > 0) ? computeWithholdingTax(taxableIncome) : 0.0;

        double totalDeductions = totalGovDeductions + withholdingTax;

        // Step 4: First cutoff pays gross only; second cutoff absorbs all deductions.
        double firstCutoffNet  = firstCutoffGross;
        double secondCutoffNet = secondCutoffGross - totalDeductions;

        return new double[] {
                firstCutoffGross,    // [0]
                secondCutoffGross,   // [1]
                totalMonthlyGross,   // [2]
                sss,                 // [3]
                philHealth,          // [4]
                pagIbig,             // [5]
                withholdingTax,      // [6]
                totalDeductions,     // [7]
                firstCutoffNet,      // [8]
                secondCutoffNet      // [9]
        };
    }

    // ============================================================
    // GOVERNMENT DEDUCTION FORMULAS
    // ============================================================

    // SSS contribution based on salary brackets.
    // Bracket formula: increments of ₱22.50 per ₱500 salary bracket starting at ₱3,250.
    static double computeSSS(double salary) {
        if (salary < 3250)   return 135.0;
        if (salary >= 24750) return 1125.0;
        int bracket = (int) ((salary - 3250) / 500);
        return 157.5 + bracket * 22.5;
    }

    // PhilHealth: 3% of monthly salary, employee pays half (1.5%).
    // Floor at ₱150 for salaries at or below ₱10,000.
    // Ceiling at ₱900 for salaries at or above ₱60,000.
    static double computePhilHealth(double salary) {
        if (salary <= 10000) return 150.0;
        if (salary >= 60000) return 900.0;
        return (salary * 0.03) / 2.0;
    }

    // Pag-IBIG: 1% for salaries at or below ₱1,500, otherwise 2%.
    // Capped at ₱100 regardless of salary.
    static double computePagIbig(double salary) {
        double rate = (salary <= 1500) ? 0.01 : 0.02;
        return Math.min(salary * rate, 100.0);
    }

    // Withholding tax using the BIR progressive bracket table.
    static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832)  return 0.0;
        if (taxableIncome <= 33332)  return (taxableIncome - 20833)  * 0.20;
        if (taxableIncome <= 66666)  return 2500.0   + (taxableIncome - 33333)  * 0.25;
        if (taxableIncome <= 166666) return 10833.0  + (taxableIncome - 66667)  * 0.30;
        if (taxableIncome <= 666666) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    // ============================================================
    // DISPLAY METHODS
    // ============================================================

    static void printEmployeeHeader(String[] employee) {
        System.out.println("\n===================================");
        System.out.println("Employee #    : " + employee[COL_EMP_ID]);
        System.out.println("Employee Name : " + employee[COL_LAST_NAME] + ", " + employee[COL_FIRST_NAME]);
        System.out.println("Birthday      : " + employee[COL_BIRTHDAY]);
        System.out.println("===================================");
    }

    // Prints the full payroll breakdown for one month.
    // Hours are passed separately because they are not stored in the payroll array.
    static void printPayrollMonth(
            int month,
            int lastDay,
            double firstCutoffHours,
            double secondCutoffHours,
            double[] p) {

        String monthName = getMonthName(month);

        System.out.println("\n--- " + monthName + " ---");

        System.out.println("\nCutoff: " + monthName + " 1-15");
        System.out.println("  Hours Worked : " + firstCutoffHours);
        System.out.println("  Gross Pay    : " + p[0]);
        System.out.println("  Net Pay      : " + p[8]);

        System.out.println("\nCutoff: " + monthName + " 16-" + lastDay);
        System.out.println("  Hours Worked : " + secondCutoffHours);
        System.out.println("  Gross Pay    : " + p[1]);
        System.out.println("  Net Pay      : " + p[9]);

        System.out.println("\nMonthly Summary");
        System.out.println("  Total Gross Salary : " + p[2]);
        System.out.println("  Deductions:");
        System.out.println("    SSS              : " + p[3]);
        System.out.println("    PhilHealth       : " + p[4]);
        System.out.println("    Pag-IBIG         : " + p[5]);
        System.out.println("    Withholding Tax  : " + p[6]);
        System.out.println("    Total Deductions : " + p[7]);
    }

    // ============================================================
    // UTILITY METHODS
    // ============================================================

    // Parses a time string "HH:mm" into an int array [hours, minutes].
    static int[] parseTime(String time) {
        String[] parts = time.trim().split(":");
        return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        };
    }

    // Reads the year from the first attendance record found for the given employee.
    // Date format is M/D/YYYY, so the year is the third segment after splitting on "/".
    // Returns 2024 as a safe default if no record is found.
    static int getYearFromAttendance(String[][] attendance, String employeeId) {
        for (String[] row : attendance) {
            if (!row[ATT_EMP_ID].trim().equals(employeeId)) continue;
            String[] parts = row[ATT_DATE].split("/");
            if (parts.length >= 3) {
                return Integer.parseInt(parts[2].trim());
            }
        }
        return 2024;
    }

    // Returns the last day of the given month, accounting for leap years.
    // Leap year rule: divisible by 4, except centuries unless also divisible by 400.
    static int getLastDayOfMonth(int month, int year) {
        if (month == 2) {
            boolean isLeapYear = (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
            return isLeapYear ? 29 : 28;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) return 30;
        return 31;
    }

    // Returns the full month name for a given month number (1 = January, etc.).
    static String getMonthName(int month) {
        String[] names = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return names[month];
    }

    // Searches the employees array for a matching ID and returns that row.
    // Returns null if not found.
    static String[] findEmployee(String[][] employees, String employeeId) {
        for (String[] employee : employees) {
            if (employee[COL_EMP_ID].trim().equals(employeeId)) {
                return employee;
            }
        }
        return null;
    }

    // Parses a numeric string that may contain quotes and comma separators
    // (e.g. "1,250.00") into a plain double value.
    static double parseAmount(String value) {
        return Double.parseDouble(value.replace("\"", "").replace(",", "").trim());
    }

    // ============================================================
    // CSV FILE LOADING
    // ============================================================

    // Loads the employee details CSV into a 2D array.
    // Uses regex to correctly split fields that contain commas inside quotes.
    // Strips surrounding quotes and whitespace from every field.
    static String[][] loadEmployees(String filePath) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        List<String[]> rows = new ArrayList<>();
        String line;
        boolean header = true;

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

        reader.close();
        return rows.toArray(new String[0][]);
    }

    // Loads the attendance CSV into a 2D array.
    // Attendance fields do not contain quoted commas, so a plain split is sufficient.
    static String[][] loadAttendance(String filePath) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        List<String[]> rows = new ArrayList<>();
        String line;
        boolean header = true;

        while ((line = reader.readLine()) != null) {
            if (header) { header = false; continue; } // Skip the column name row.

            String[] columns = line.split(",");

            // Guard against malformed rows with too few columns.
            if (columns.length < 6) continue;

            rows.add(columns);
        }

        reader.close();
        return rows.toArray(new String[0][]);
    }
}