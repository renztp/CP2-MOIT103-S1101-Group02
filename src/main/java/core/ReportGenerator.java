/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.util.Map;
import java.util.HashMap;

import models.Employee;
import models.Payslip;

/**
 *
 * @author aesgu
 */
public class ReportGenerator {
    
      public Map<String, String> generateReport(
            Employee employee,
            Payslip payslip
    ) {

        Map<String, String> reportData = new HashMap<>();

        // Employee Details
        reportData.put(
                "Employee Number",
                String.valueOf(employee.getEmployeeNumber())
        );

        reportData.put(
                "Employee Name",
                employee.getFullName()
        );

        reportData.put(
                "Position",
                employee.getPosition()
        );

        // Allowances
        reportData.put(
                "Rice Subsidy",
                String.valueOf(employee.getRiceSubsidy())
        );

        reportData.put(
                "Phone Allowance",
                String.valueOf(employee.getPhoneAllowance())
        );

        reportData.put(
                "Clothing Allowance",
                String.valueOf(employee.getClothingAllowance())
        );

        // Pay Details
        reportData.put(
                "Gross Pay",
                String.valueOf(payslip.getGrossPay())
        );

        // Deductions
        reportData.put(
                "SSS Deduction",
                String.valueOf(payslip.getSssDeduction())
        );

        reportData.put(
                "PhilHealth Deduction",
                String.valueOf(payslip.getPhilHealthDeduction())
        );

        reportData.put(
                "Pag-IBIG Deduction",
                String.valueOf(payslip.getPagIbigDeduction())
        );

        reportData.put(
                "Withholding Tax",
                String.valueOf(payslip.getWithholdingTax())
        );

        // Totals
        reportData.put(
                "Total Deductions",
                String.valueOf(payslip.getTotalDeductions())
        );

        reportData.put(
                "Net Pay",
                String.valueOf(payslip.getNetPay())
        );

        return reportData;
    }
}