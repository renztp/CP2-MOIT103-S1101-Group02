package core.models;

public class Employee {

    public static final int COL_EMP_ID = 0;
    public static final int COL_LAST_NAME = 1;
    public static final int COL_FIRST_NAME = 2;
    public static final int COL_BIRTHDAY = 3;
    public static final int COL_HOURLY_RATE = 18;
    public static final int COL_MINIMUM_REQUIRED_COLUMNS = 19;

    private final String[] rawRow;

    private Employee(String[] rawRow) {
        this.rawRow = rawRow;
    }

    public static Employee fromCsvRow(String[] row) {
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

    public double getHourlyRate() {
        return Double.parseDouble(rawRow[COL_HOURLY_RATE].replace("\"", "").replace(",", "").trim());
    }

    public String getValueAt(int index) {
        return (index < rawRow.length) ? rawRow[index] : "";
    }

    public String[] getRawRow() {
        return rawRow;
    }
}
