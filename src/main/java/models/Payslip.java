/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author aesgu
 */
public class Payslip {
    
    private Employee employee;

    private double grossPay;
    private double sssDeduction;
    private double philHealthDeduction;
    private double pagIbigDeduction;
    private double withholdingTax;

    private double totalDeductions;
    private double netPay;

    public Payslip(
            Employee employee,
            double grossPay,
            double sssDeduction,
            double philHealthDeduction,
            double pagIbigDeduction,
            double withholdingTax
    ) {

        this.employee = employee;

        this.grossPay = grossPay;
        this.sssDeduction = sssDeduction;
        this.philHealthDeduction = philHealthDeduction;
        this.pagIbigDeduction = pagIbigDeduction;
        this.withholdingTax = withholdingTax;

        this.totalDeductions =
                sssDeduction +
                philHealthDeduction +
                pagIbigDeduction +
                withholdingTax;

        this.netPay = grossPay - totalDeductions;
    }

    public Employee getEmployee() {
        return employee;
    }

    public double getGrossPay() {
        return grossPay;
    }

    public double getSssDeduction() {
        return sssDeduction;
    }

    public double getPhilHealthDeduction() {
        return philHealthDeduction;
    }

    public double getPagIbigDeduction() {
        return pagIbigDeduction;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getNetPay() {
        return netPay;
    }
}