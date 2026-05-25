package core.services;

import core.models.Attendance;
import core.models.Employee;
import core.models.PayrollBreakdown;

import java.util.List;
import java.util.stream.IntStream;

public class PayrollProcessor {

    public static final int PAYROLL_MONTH_START = 6;
    public static final int PAYROLL_MONTH_END = 12;

    private static final int WORK_START_MINUTES = 8 * 60;
    private static final int WORK_END_MINUTES = 17 * 60;
    private static final int GRACE_PERIOD_END_MINUTES = 8 * 60 + 10;
    private static final int LUNCH_BREAK_MINUTES = 60;

    public static final String PAYSLIP_SEPARATOR_MAJOR = "=".repeat(55);
    public static final String PAYSLIP_SEPARATOR_MINOR = "-".repeat(55);

    public boolean validateMonthInput(String rawInput) {
        try {
            int parsedMonth = Integer.parseInt(rawInput);
            return parsedMonth >= PAYROLL_MONTH_START && parsedMonth <= PAYROLL_MONTH_END;
        } catch (NumberFormatException invalidInput) {
            return false;
        }
    }

    public String buildFullPayrollReportForEmployee(Employee employeeRow, List<Attendance> attendanceRecords) {
        double hourlyRate = employeeRow.getHourlyRate();
        int payrollYear = resolveYearFromAttendance(attendanceRecords, employeeRow.getId());

        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");
        reportBuilder.append(String.format("   EMPLOYEE: %s, %s  (ID: %s)%n",
                employeeRow.getLastName(),
                employeeRow.getFirstName(),
                employeeRow.getId()));
        reportBuilder.append(String.format("   Birthday : %s%n", employeeRow.getBirthday()));
        reportBuilder.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");

        IntStream.rangeClosed(PAYROLL_MONTH_START, PAYROLL_MONTH_END).forEach(month -> {
            int lastDayOfMonth = resolveLastDayOfMonth(month, payrollYear);
            double firstCutoffHours = calculateWorkedHoursInRange(attendanceRecords, employeeRow.getId(), month, 1, 15);
            double secondCutoffHours = calculateWorkedHoursInRange(
                    attendanceRecords, employeeRow.getId(), month, 16, lastDayOfMonth);

            PayrollBreakdown payrollValues = computeMonthlyPayroll(firstCutoffHours, secondCutoffHours, hourlyRate);

            reportBuilder.append(formatMonthlyPayrollBlock(
                    month, lastDayOfMonth, firstCutoffHours, secondCutoffHours, payrollValues));
        });

        return reportBuilder.toString();
    }

    public String buildPayslipForEmployee(
            Employee employeeRow,
            List<Attendance> attendanceRecords,
            int targetMonth,
            int payrollYear) {

        double hourlyRate = employeeRow.getHourlyRate();
        int lastDayOfMonth = resolveLastDayOfMonth(targetMonth, payrollYear);
        double firstCutoffHours = calculateWorkedHoursInRange(attendanceRecords, employeeRow.getId(), targetMonth, 1, 15);
        double secondCutoffHours = calculateWorkedHoursInRange(
                attendanceRecords, employeeRow.getId(), targetMonth, 16, lastDayOfMonth);

        PayrollBreakdown payrollValues = computeMonthlyPayroll(firstCutoffHours, secondCutoffHours, hourlyRate);

        StringBuilder payslip = new StringBuilder();
        payslip.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");
        payslip.append(String.format("   PAYSLIP - %s%n", resolveMonthName(targetMonth).toUpperCase()));
        payslip.append(String.format("   Employee : %s, %s  (ID: %s)%n",
                employeeRow.getLastName(),
                employeeRow.getFirstName(),
                employeeRow.getId()));
        payslip.append(String.format("   Hourly Rate : %s%n", hourlyRate));
        payslip.append(PAYSLIP_SEPARATOR_MAJOR).append("\n");
        payslip.append(formatMonthlyPayrollBlock(
                targetMonth, lastDayOfMonth, firstCutoffHours, secondCutoffHours, payrollValues));

        return payslip.toString();
    }

    public PayrollBreakdown computeMonthlyPayroll(
            double firstCutoffHours,
            double secondCutoffHours,
            double hourlyRate) {
        double firstCutoffGross = firstCutoffHours * hourlyRate;
        double secondCutoffGross = secondCutoffHours * hourlyRate;
        double totalMonthlyGross = firstCutoffGross + secondCutoffGross;

        double sssContribution = (totalMonthlyGross > 0) ? DeductionCalculator.computeSSSContribution(totalMonthlyGross) : 0.0;
        double philHealthContribution = (totalMonthlyGross > 0)
                ? DeductionCalculator.computePhilHealthContribution(totalMonthlyGross) : 0.0;
        double pagIbigContribution = (totalMonthlyGross > 0)
                ? DeductionCalculator.computePagIbigContribution(totalMonthlyGross) : 0.0;

        double totalGovernmentDeductions = sssContribution + philHealthContribution + pagIbigContribution;
        double taxableIncome = totalMonthlyGross - totalGovernmentDeductions;
        double withholdingTax = (taxableIncome > 0) ? DeductionCalculator.computeWithholdingTax(taxableIncome) : 0.0;

        double totalDeductions = totalGovernmentDeductions + withholdingTax;
        double firstCutoffNetPay = firstCutoffGross;
        double secondCutoffNetPay = secondCutoffGross - totalDeductions;

        return new PayrollBreakdown(
                firstCutoffGross,
                secondCutoffGross,
                totalMonthlyGross,
                sssContribution,
                philHealthContribution,
                pagIbigContribution,
                withholdingTax,
                totalDeductions,
                firstCutoffNetPay,
                secondCutoffNetPay
        );
    }

    public double calculateWorkedHoursInRange(
            List<Attendance> attendanceRecords,
            String targetEmployeeId,
            int targetMonth,
            int rangeStartDay,
            int rangeEndDay) {

        long totalMinutesWorked = attendanceRecords.stream()
                .filter(attendance -> attendance.getEmpId().equals(targetEmployeeId))
                .filter(attendance -> {
                    String[] dateParts = attendance.getDate().split("/");
                    if (dateParts.length < 2) {
                        return false;
                    }

                    int recordMonth = Integer.parseInt(dateParts[0].trim());
                    int recordDay = Integer.parseInt(dateParts[1].trim());
                    return recordMonth == targetMonth
                            && recordDay >= rangeStartDay
                            && recordDay <= rangeEndDay;
                })
                .mapToLong(attendance -> {
                    int[] loginTime = parseTimeToHoursMinutes(attendance.getTimeIn());
                    int[] logoutTime = parseTimeToHoursMinutes(attendance.getTimeOut());

                    int loginMinutes = loginTime[0] * 60 + loginTime[1];
                    int logoutMinutes = logoutTime[0] * 60 + logoutTime[1];

                    if (loginMinutes <= GRACE_PERIOD_END_MINUTES) {
                        loginMinutes = WORK_START_MINUTES;
                    }
                    if (loginMinutes < WORK_START_MINUTES) {
                        loginMinutes = WORK_START_MINUTES;
                    }
                    if (logoutMinutes > WORK_END_MINUTES) {
                        logoutMinutes = WORK_END_MINUTES;
                    }

                    int effectiveMinutesWorked = logoutMinutes - loginMinutes - LUNCH_BREAK_MINUTES;
                    return Math.max(effectiveMinutesWorked, 0);
                })
                .sum();

        return totalMinutesWorked / 60.0;
    }

    public int resolveYearFromAttendance(List<Attendance> attendanceRecords, String employeeId) {
        return attendanceRecords.stream()
                .filter(attendance -> attendance.getEmpId().equals(employeeId))
                .map(attendance -> attendance.getDate().split("/"))
                .filter(parts -> parts.length >= 3)
                .mapToInt(parts -> Integer.parseInt(parts[2].trim()))
                .findFirst()
                .orElse(2024);
    }

    public int resolveLastDayOfMonth(int month, int year) {
        if (month == 2) {
            boolean isLeapYear = (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
            return isLeapYear ? 29 : 28;
        }
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            return 30;
        }
        return 31;
    }

    public String resolveMonthName(int monthNumber) {
        String[] monthNames = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return monthNames[monthNumber];
    }

    public String formatMonthlyPayrollBlock(
            int month,
            int lastDayOfMonth,
            double firstCutoffHours,
            double secondCutoffHours,
            PayrollBreakdown payrollValues) {

        String monthName = resolveMonthName(month);
        StringBuilder block = new StringBuilder();

        block.append(String.format("%n   --- %s ---%n", monthName));

        block.append(String.format("   Cutoff 1: %s 1-15%n", monthName));
        block.append(String.format("      Hours Worked : %s%n", firstCutoffHours));
        block.append(String.format("      Gross Pay    : %s%n", payrollValues.getFirstCutoffGross()));
        block.append(String.format("      Net Pay      : %s%n", payrollValues.getFirstCutoffNet()));

        block.append(String.format("   Cutoff 2: %s 16-%d%n", monthName, lastDayOfMonth));
        block.append(String.format("      Hours Worked : %s%n", secondCutoffHours));
        block.append(String.format("      Gross Pay    : %s%n", payrollValues.getSecondCutoffGross()));
        block.append(String.format("      Net Pay      : %s%n", payrollValues.getSecondCutoffNet()));

        block.append(PAYSLIP_SEPARATOR_MINOR).append("\n");
        block.append(String.format("   Total Gross    : %s%n", payrollValues.getTotalMonthlyGross()));
        block.append("   Deductions:\n");
        block.append(String.format("      SSS              : %s%n", payrollValues.getSss()));
        block.append(String.format("      PhilHealth       : %s%n", payrollValues.getPhilHealth()));
        block.append(String.format("      Pag-IBIG         : %s%n", payrollValues.getPagIbig()));
        block.append(String.format("      Withholding Tax  : %s%n", payrollValues.getWithholdingTax()));
        block.append(String.format("      Total Deductions : %s%n", payrollValues.getTotalDeductions()));

        return block.toString();
    }

    private int[] parseTimeToHoursMinutes(String timeString) {
        String[] parts = timeString.trim().split(":");
        return new int[]{
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        };
    }
}
