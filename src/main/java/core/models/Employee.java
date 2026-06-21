package core.models;

// Represents a single employee record parsed from the CSV file.
// Uses a raw string array to match column positions without strict field coupling.
public class Employee {

    public static final int COL_EMP_ID = 0;
    public static final int COL_LAST_NAME = 1;
    public static final int COL_FIRST_NAME = 2;
    public static final int COL_BIRTHDAY = 3;
    public static final int COL_ADDRESS = 4;
    public static final int COL_PHONE = 5;
    public static final int COL_SSS = 6;
    public static final int COL_PHILHEALTH = 7;
    public static final int COL_TIN = 8;
    public static final int COL_PAGIBIG = 9;
    public static final int COL_STATUS = 10;
    public static final int COL_POSITION = 11;
    public static final int COL_SUPERVISOR = 12;
    public static final int COL_BASIC_SALARY = 13;
    public static final int COL_RICE_SUBSIDY = 14;
    public static final int COL_PHONE_ALLOWANCE = 15;
    public static final int COL_CLOTHING_ALLOWANCE = 16;
    public static final int COL_GROSS_SEMI_MONTHLY = 17;
    public static final int COL_HOURLY_RATE = 18;
    public static final int COL_DAYS_WORKED = 19;
    public static final int COL_GROSS_PAY = 20;
    public static final int COL_SSS_DEDUCTION = 21;
    public static final int COL_PHILHEALTH_DEDUCTION = 22;
    public static final int COL_PAGIBIG_DEDUCTION = 23;
    public static final int COL_WITHHOLDING_TAX = 24;
    public static final int COL_TOTAL_DEDUCTIONS = 25;
    public static final int COL_NET_PAY = 26;
    public static final int COL_MINIMUM_REQUIRED_COLUMNS = 19;
    public static final int COL_TOTAL_COLUMNS = 27;

    private String[] rawRow;

    private Employee(String[] rawRow) {
        this.rawRow = rawRow;
    }

    public static Employee fromCsvRow(String[] row) {
        return new Employee(row);
    }

    // Builds a new Employee from individual field values.
    public static Employee fromFields(String id, String lastName, String firstName, String birthday,
                                      String address, String phone, String sss, String philhealth,
                                      String tin, String pagibig, String status, String position,
                                      String supervisor, String basicSalary, String riceSubsidy,
                                      String phoneAllowance, String clothingAllowance,
                                      String grossSemiMonthly, String hourlyRate) {
        String[] row = new String[COL_MINIMUM_REQUIRED_COLUMNS];
        row[COL_EMP_ID] = id.trim();
        row[COL_LAST_NAME] = lastName.trim();
        row[COL_FIRST_NAME] = firstName.trim();
        row[COL_BIRTHDAY] = birthday.trim();
        row[COL_ADDRESS] = address.trim();
        row[COL_PHONE] = phone.trim();
        row[COL_SSS] = sss.trim();
        row[COL_PHILHEALTH] = philhealth.trim();
        row[COL_TIN] = tin.trim();
        row[COL_PAGIBIG] = pagibig.trim();
        row[COL_STATUS] = status.trim();
        row[COL_POSITION] = position.trim();
        row[COL_SUPERVISOR] = supervisor.trim();
        row[COL_BASIC_SALARY] = basicSalary.trim();
        row[COL_RICE_SUBSIDY] = riceSubsidy.trim();
        row[COL_PHONE_ALLOWANCE] = phoneAllowance.trim();
        row[COL_CLOTHING_ALLOWANCE] = clothingAllowance.trim();
        row[COL_GROSS_SEMI_MONTHLY] = grossSemiMonthly.trim();
        row[COL_HOURLY_RATE] = hourlyRate.trim();
        return new Employee(row);
    }

    public String getId() {
        return rawRow[COL_EMP_ID].trim();
    }

    public String getLastName() {
        return rawRow[COL_LAST_NAME].trim();
    }

    public String getFirstName() {
        return rawRow[COL_FIRST_NAME].trim();
    }

    public String getBirthday() {
        return rawRow[COL_BIRTHDAY].trim();
    }

    public String getPosition() {
        return rawRow[COL_POSITION].trim();
    }

    public double getHourlyRate() {
        return Double.parseDouble(rawRow[COL_HOURLY_RATE].replace("\"", "").replace(",", "").trim());
    }

    public String getValueAt(int index) {
        return (index < rawRow.length) ? rawRow[index] : "";
    }

    // Returns a mutable copy of the raw row for use in edit dialogs.
    public String[] getRawRow() {
        return rawRow.clone();
    }

    // Makes sure the row has enough columns for salary fields.
    public void ensureSalaryColumns() {
        if (rawRow.length < COL_TOTAL_COLUMNS) {
            String[] extendedRow = new String[COL_TOTAL_COLUMNS];
            System.arraycopy(rawRow, 0, extendedRow, 0, rawRow.length);
            for (int i = rawRow.length; i < COL_TOTAL_COLUMNS; i++) {
                extendedRow[i] = "";
            }
            rawRow = extendedRow;
        }
    }

    // Saves computed salary values back into the employee row.
    public void setSalaryFields(double daysWorked, double grossPay, double sss,
                                double philHealth, double pagIbig, double withholdingTax,
                                double totalDeductions, double netPay) {
        ensureSalaryColumns();
        rawRow[COL_DAYS_WORKED] = String.format("%.0f", daysWorked);
        rawRow[COL_GROSS_PAY] = String.format("%.2f", grossPay);
        rawRow[COL_SSS_DEDUCTION] = String.format("%.2f", sss);
        rawRow[COL_PHILHEALTH_DEDUCTION] = String.format("%.2f", philHealth);
        rawRow[COL_PAGIBIG_DEDUCTION] = String.format("%.2f", pagIbig);
        rawRow[COL_WITHHOLDING_TAX] = String.format("%.2f", withholdingTax);
        rawRow[COL_TOTAL_DEDUCTIONS] = String.format("%.2f", totalDeductions);
        rawRow[COL_NET_PAY] = String.format("%.2f", netPay);
    }
}
