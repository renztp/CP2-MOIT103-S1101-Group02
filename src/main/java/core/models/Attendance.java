package core.models;

public class Attendance {

    public static final int ATT_EMP_ID = 0;
    public static final int ATT_DATE = 3;
    public static final int ATT_TIME_IN = 4;
    public static final int ATT_TIME_OUT = 5;
    public static final int ATT_MINIMUM_REQUIRED_COLUMNS = 6;

    private final String empId;
    private final String date;
    private final String timeIn;
    private final String timeOut;

    private Attendance(String empId, String date, String timeIn, String timeOut) {
        this.empId = empId;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public static Attendance fromCsvRow(String[] row) {
        return new Attendance(
                row[ATT_EMP_ID].trim(),
                row[ATT_DATE],
                row[ATT_TIME_IN],
                row[ATT_TIME_OUT]
        );
    }

    public String getEmpId() {
        return empId;
    }

    public String getDate() {
        return date;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }
}
