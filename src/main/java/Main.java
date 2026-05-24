
import core.DatabaseService;
import core.FileHandler;
import models.Employee;


import models.AttendanceRecord;
import models.Credentials;


public class Main {

    public static void main(String[] args) {

        AttendanceRecord record =
                new AttendanceRecord(
                        "1001",
                        "Ashley Ramos",
                        "2026-05-24",
                        "08:00",
                        "17:00",
                        5,
                        1.0,
                        8.0
                );

        Credentials credentials =
                new Credentials(
                        "ashley",
                        "1234",
                        true
                );

        System.out.println(
                record.getEmployeeName()
        );

        System.out.println(
                credentials.getUsername()
        );
    }
}