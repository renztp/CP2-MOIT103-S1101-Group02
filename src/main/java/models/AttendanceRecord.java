/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;


public class AttendanceRecord {

    private String employeeId;
    private String employeeName;
    private String attendanceDate;
    private String timeIn;
    private String timeOut;
    private int lateMinutes;
    private double overtimeHours;
    private double totalHoursWorked;

    public AttendanceRecord(
            String employeeId,
            String employeeName,
            String attendanceDate,
            String timeIn,
            String timeOut,
            int lateMinutes,
            double overtimeHours,
            double totalHoursWorked) {

        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.attendanceDate = attendanceDate;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.lateMinutes = lateMinutes;
        this.overtimeHours = overtimeHours;
        this.totalHoursWorked = totalHoursWorked;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public String getTimeIn() {
        return timeIn;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public double getTotalHoursWorked() {
        return totalHoursWorked;
    }
}
