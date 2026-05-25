/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import models.Employee;

public class PayrollProcessor {

    public void processPayroll(Employee employee) {

        // Temporary hardcoded hours for testing
        double hoursWorked = 160;

        double hourlyRate =
                employee.getHourlyRate();

        double grossSalary =
                hoursWorked * hourlyRate;

        DeductionCalculator dc =
                new DeductionCalculator();

        double sss =
                dc.getSSSContribution(grossSalary);

        double philHealth =
                dc.calculatePhilHealth(grossSalary);

        double pagIbig =
                dc.calculatePagIbig(grossSalary);

        double withholdingTax =
                dc.getWithholdingTax(grossSalary);

        double totalDeductions =
                sss +
                philHealth +
                pagIbig +
                withholdingTax;

        double netSalary =
                grossSalary -
                totalDeductions;

        System.out.println("===== PAYROLL =====");

        System.out.println(
                "Employee Number: "
                + employee.getEmployeeNumber()
        );

        System.out.println(
                "Employee Name: "
                + employee.getFirstName()
                + " "
                + employee.getLastName()
        );

        System.out.println(
                "Position: "
                + employee.getPosition()
        );

        System.out.println(
                "Gross Salary: "
                + grossSalary
        );

        System.out.println(
                "SSS: "
                + sss
        );

        System.out.println(
                "PhilHealth: "
                + philHealth
        );

        System.out.println(
                "Pag-IBIG: "
                + pagIbig
        );

        System.out.println(
                "Withholding Tax: "
                + withholdingTax
        );

        System.out.println(
                "Net Salary: "
                + netSalary
        );
    }
}