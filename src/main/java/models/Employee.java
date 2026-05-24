/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author aesgu
 */
public class Employee {
    
    private int employeeNumber;
    private String lastName;
    private String firstName;
    private String position;

    private double hourlyRate;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;

    public Employee(
            int employeeNumber,
            String lastName,
            String firstName,
            String position,
            double hourlyRate,
            double riceSubsidy,
            double phoneAllowance,
            double clothingAllowance
    ) {

        this.employeeNumber = employeeNumber;
        this.lastName = lastName;
        this.firstName = firstName;
        this.position = position;

        this.hourlyRate = hourlyRate;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
    }

    public int getEmployeeNumber() {
        return employeeNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPosition() {
        return position;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}