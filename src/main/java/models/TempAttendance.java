package models;

import java.util.Date;

public class TempAttendance {
    private int empId;
    private String LastName;
    private String FirstName;
    private Date recordedDate;
    private String loggedIn;
    private String loggedOut;

    public TempAttendance(
            int empId,
            String LastName,
            String FirstName,
            Date recordedDate,
            String loggedIn,
            String loggedOut
    ) {
        this.empId = empId;
        this.LastName = LastName;
        this.FirstName = FirstName;
        this.recordedDate = recordedDate;
        this.loggedIn = loggedIn;
        this.loggedOut = loggedOut;
    }

    public Date getRecordedDate() {
        return recordedDate;
    }

    public int getEmpId() {
        return empId;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getLoggedIn() {
        return loggedIn;
    }

    public String getLoggedOut() {
        return loggedOut;
    }
}
