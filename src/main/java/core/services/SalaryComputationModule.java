package core.services;

import core.models.Attendance;
import core.models.Employee;

import java.util.HashSet;
import java.util.List;

// Feature 3 - Salary computation using simple array-based methods.
// Keeps all calculation logic separate from the GUI.
public class SalaryComputationModule {

    public static final int HOURS_PER_WORK_DAY = 8;

    // Builds the daily rate from the hourly rate stored in the employee record.
    public static double getRatePerDay(Employee employee) {
        return employee.getHourlyRate() * HOURS_PER_WORK_DAY;
    }

    // Counts how many unique attendance days an employee has logged within a single
    // target month (6-12). Scoping to one month keeps this consistent with the monthly
    // contribution ceilings applied later (SSS/PhilHealth/Pag-IBIG/tax are all monthly
    // brackets, so the gross pay they are applied to must also be a single month's pay).
    public static int countDaysWorked(List<Attendance> attendanceRecords, String employeeId, int targetMonth) {
        HashSet<String> uniqueDates = new HashSet<>();
        for (Attendance record : attendanceRecords) {
            if (record.getEmpId().equals(employeeId) && extractMonth(record.getDate()) == targetMonth) {
                uniqueDates.add(record.getDate().trim());
            }
        }
        return uniqueDates.size();
    }

    // Reads the month component (e.g. "6" from "6/14/2024") out of an attendance date string.
    // Returns -1 for malformed dates so they simply fail the month-equality check above.
    private static int extractMonth(String date) {
        String[] parts = date.split("/");
        if (parts.length < 1) {
            return -1;
        }
        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    // Checks if a text value can be converted to a valid number.
    public static boolean isValidNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value.replace(",", "").trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // Converts a text value to a double. Returns -1 when the value is invalid.
    public static double parseNumber(String value) {
        if (!isValidNumber(value)) {
            return -1;
        }
        return Double.parseDouble(value.replace(",", "").trim());
    }

    // Validates rate-per-day and days-worked arrays before computation starts.
    // Returns an error message when invalid, or null when everything is okay.
    public static String validateSalaryInputs(double[] ratePerDay, double[] daysWorked, String[] employeeIds) {
        if (ratePerDay == null || daysWorked == null || employeeIds == null) {
            return "Salary data arrays are missing.";
        }
        if (ratePerDay.length != daysWorked.length || ratePerDay.length != employeeIds.length) {
            return "Salary data arrays do not match in size.";
        }
        if (ratePerDay.length == 0) {
            return "No employee records found to compute.";
        }

        for (int i = 0; i < ratePerDay.length; i++) {
            if (ratePerDay[i] < 0) {
                return "Invalid rate per day for employee " + employeeIds[i] + ".";
            }
            if (daysWorked[i] < 0) {
                return "Invalid days worked for employee " + employeeIds[i] + ".";
            }
            if (ratePerDay[i] == 0 && daysWorked[i] > 0) {
                return "Missing hourly rate for employee " + employeeIds[i] + ".";
            }
        }
        return null;
    }

    // Validates numeric employee fields used as salary inputs.
    public static String validateEmployeeNumericFields(List<Employee> employees) {
        for (Employee employee : employees) {
            String hourlyRateText = employee.getValueAt(Employee.COL_HOURLY_RATE);
            if (!isValidNumber(hourlyRateText)) {
                return "Invalid or missing hourly rate for employee " + employee.getId() + ".";
            }
            double hourlyRate = parseNumber(hourlyRateText);
            if (hourlyRate < 0) {
                return "Invalid hourly rate for employee " + employee.getId() + ".";
            }
        }
        return null;
    }

    public static double[] computeGrossPay(double[] ratePerDay, double[] daysWorked) {
        double[] grossPay = new double[ratePerDay.length];
        for (int i = 0; i < ratePerDay.length; i++) {
            grossPay[i] = ratePerDay[i] * daysWorked[i];
        }
        return grossPay;
    }

    public static double[] computeSSS(double[] grossPay) {
        double[] sssValues = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            if (grossPay[i] > 0) {
                sssValues[i] = DeductionCalculator.computeSSSContribution(grossPay[i]);
            } else {
                sssValues[i] = 0.0;
            }
        }
        return sssValues;
    }

    public static double[] computePhilHealth(double[] grossPay) {
        double[] philHealthValues = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            if (grossPay[i] > 0) {
                philHealthValues[i] = DeductionCalculator.computePhilHealthContribution(grossPay[i]);
            } else {
                philHealthValues[i] = 0.0;
            }
        }
        return philHealthValues;
    }

    public static double[] computePagIBIG(double[] grossPay) {
        double[] pagIbigValues = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            if (grossPay[i] > 0) {
                pagIbigValues[i] = DeductionCalculator.computePagIbigContribution(grossPay[i]);
            } else {
                pagIbigValues[i] = 0.0;
            }
        }
        return pagIbigValues;
    }

    public static double[] computeWithholdingTax(double[] grossPay, double[] sss, double[] philHealth, double[] pagIbig) {
        double[] taxValues = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            double taxableIncome = grossPay[i] - sss[i] - philHealth[i] - pagIbig[i];
            if (taxableIncome > 0) {
                taxValues[i] = DeductionCalculator.computeWithholdingTax(taxableIncome);
            } else {
                taxValues[i] = 0.0;
            }
        }
        return taxValues;
    }

    public static double[] computeDeductions(double[] sss, double[] philHealth, double[] pagIbig, double[] withholdingTax) {
        double[] totalDeductions = new double[sss.length];
        for (int i = 0; i < sss.length; i++) {
            totalDeductions[i] = sss[i] + philHealth[i] + pagIbig[i] + withholdingTax[i];
        }
        return totalDeductions;
    }

    public static double[] computeNetPay(double[] grossPay, double[] totalDeductions) {
        double[] netPay = new double[grossPay.length];
        for (int i = 0; i < grossPay.length; i++) {
            netPay[i] = grossPay[i] - totalDeductions[i];
        }
        return netPay;
    }

    // Runs the full salary computation flow for all employees, scoped to a single
    // pay-period month (6-12). Scoping to one month is what keeps grossPay consistent
    // with the monthly SSS/PhilHealth/Pag-IBIG/withholding-tax brackets applied to it.
    public static SalaryResult computeAllSalaries(
            List<Employee> employees, List<Attendance> attendanceRecords, int targetMonth) {
        int count = employees.size();

        String[] employeeIds = new String[count];
        double[] ratePerDay = new double[count];
        double[] daysWorked = new double[count];

        for (int i = 0; i < count; i++) {
            Employee employee = employees.get(i);
            employeeIds[i] = employee.getId();
            ratePerDay[i] = getRatePerDay(employee);
            daysWorked[i] = countDaysWorked(attendanceRecords, employee.getId(), targetMonth);
        }

        String validationError = validateSalaryInputs(ratePerDay, daysWorked, employeeIds);
        if (validationError != null) {
            return SalaryResult.failed(validationError);
        }

        double[] grossPay = computeGrossPay(ratePerDay, daysWorked);
        double[] sss = computeSSS(grossPay);
        double[] philHealth = computePhilHealth(grossPay);
        double[] pagIbig = computePagIBIG(grossPay);
        double[] withholdingTax = computeWithholdingTax(grossPay, sss, philHealth, pagIbig);
        double[] totalDeductions = computeDeductions(sss, philHealth, pagIbig, withholdingTax);
        double[] netPay = computeNetPay(grossPay, totalDeductions);

        return new SalaryResult(
                true,
                null,
                employeeIds,
                ratePerDay,
                daysWorked,
                grossPay,
                sss,
                philHealth,
                pagIbig,
                withholdingTax,
                totalDeductions,
                netPay
        );
    }

    // Builds a simple text report for the GUI results area.
    public static String buildSalaryReport(SalaryResult result, List<Employee> employees, String monthName) {
        StringBuilder report = new StringBuilder();
        report.append("SALARY COMPUTATION RESULTS - ").append(monthName.toUpperCase()).append("\n");
        report.append("=".repeat(55)).append("\n\n");

        for (int i = 0; i < result.employeeIds.length; i++) {
            Employee employee = employees.get(i);
            report.append(String.format("Employee: %s, %s (ID: %s)%n",
                    employee.getLastName(),
                    employee.getFirstName(),
                    result.employeeIds[i]));
            report.append(String.format("   Rate per Day     : %.2f%n", result.ratePerDay[i]));
            report.append(String.format("   Days Worked      : %.0f%n", result.daysWorked[i]));
            report.append(String.format("   Gross Pay        : %.2f%n", result.grossPay[i]));
            report.append(String.format("   Total Deductions : %.2f%n", result.totalDeductions[i]));
            report.append(String.format("   Net Pay          : %.2f%n", result.netPay[i]));
            report.append("-".repeat(55)).append("\n");
        }

        return report.toString();
    }

    // Holds all computed salary values for one run.
    public static class SalaryResult {
        public final boolean success;
        public final String errorMessage;
        public final String[] employeeIds;
        public final double[] ratePerDay;
        public final double[] daysWorked;
        public final double[] grossPay;
        public final double[] sss;
        public final double[] philHealth;
        public final double[] pagIbig;
        public final double[] withholdingTax;
        public final double[] totalDeductions;
        public final double[] netPay;

        public SalaryResult(boolean success, String errorMessage, String[] employeeIds,
                            double[] ratePerDay, double[] daysWorked, double[] grossPay,
                            double[] sss, double[] philHealth, double[] pagIbig,
                            double[] withholdingTax, double[] totalDeductions, double[] netPay) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.employeeIds = employeeIds;
            this.ratePerDay = ratePerDay;
            this.daysWorked = daysWorked;
            this.grossPay = grossPay;
            this.sss = sss;
            this.philHealth = philHealth;
            this.pagIbig = pagIbig;
            this.withholdingTax = withholdingTax;
            this.totalDeductions = totalDeductions;
            this.netPay = netPay;
        }

        public static SalaryResult failed(String errorMessage) {
            return new SalaryResult(false, errorMessage, null, null, null, null,
                    null, null, null, null, null, null);
        }
    }

    // Feature 5 - Payroll Summary Display.
    // Reuses computeAllSalaries() (the same gross pay and deduction routines built for
    // Feature 3) so the summary totals are always consistent with the per-employee
    // salary computation results, rather than being calculated a second, separate way.
    public static SummaryResult generateSummary(
            List<Employee> employees, List<Attendance> attendanceRecords, int targetMonth) {

        if (employees == null || employees.isEmpty()) {
            return SummaryResult.failed(
                    "No employee data is loaded. Please load the employee roster first.");
        }

        String numericValidationError = validateEmployeeNumericFields(employees);
        if (numericValidationError != null) {
            return SummaryResult.failed(numericValidationError);
        }

        SalaryResult salaryResult = computeAllSalaries(employees, attendanceRecords, targetMonth);
        if (!salaryResult.success) {
            return SummaryResult.failed(salaryResult.errorMessage);
        }

        int employeeCount = salaryResult.employeeIds.length;
        double totalGrossPay = sumArray(salaryResult.grossPay);
        double totalDeductions = sumArray(salaryResult.totalDeductions);
        double totalNetPay = sumArray(salaryResult.netPay);
        double averageNetPay = (employeeCount > 0) ? (totalNetPay / employeeCount) : 0.0;

        return new SummaryResult(true, null, targetMonth, employeeCount, totalGrossPay, totalDeductions, averageNetPay);
    }

    private static double sumArray(double[] values) {
        double total = 0.0;
        for (double value : values) {
            total += value;
        }
        return total;
    }

    // Builds a simple, read-only-friendly text report for the payroll summary dialog.
    public static String buildSummaryReport(SummaryResult result, String monthName) {
        StringBuilder report = new StringBuilder();
        report.append("PAYROLL SUMMARY - ").append(monthName.toUpperCase()).append("\n");
        report.append("=".repeat(40)).append("\n\n");
        report.append(String.format("Total Employees   : %d%n", result.employeeCount));
        report.append(String.format("Total Gross Pay    : PHP %,.2f%n", result.totalGrossPay));
        report.append(String.format("Total Deductions   : PHP %,.2f%n", result.totalDeductions));
        report.append(String.format("Average Net Pay    : PHP %,.2f%n", result.averageNetPay));
        report.append("\n").append("-".repeat(40)).append("\n");
        report.append("Average net pay = total net pay / total number of employees.\n");
        return report.toString();
    }

    // Holds the aggregated figures for one payroll summary run.
    public static class SummaryResult {
        public final boolean success;
        public final String errorMessage;
        public final int targetMonth;
        public final int employeeCount;
        public final double totalGrossPay;
        public final double totalDeductions;
        public final double averageNetPay;

        public SummaryResult(boolean success, String errorMessage, int targetMonth, int employeeCount,
                              double totalGrossPay, double totalDeductions, double averageNetPay) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.targetMonth = targetMonth;
            this.employeeCount = employeeCount;
            this.totalGrossPay = totalGrossPay;
            this.totalDeductions = totalDeductions;
            this.averageNetPay = averageNetPay;
        }

        public static SummaryResult failed(String errorMessage) {
            return new SummaryResult(false, errorMessage, 0, 0, 0, 0, 0);
        }
    }
}
