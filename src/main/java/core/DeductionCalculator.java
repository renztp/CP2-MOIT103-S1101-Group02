package core;

public class DeductionCalculator {
    private final double salary;

    public DeductionCalculator(double salary) {
        this.salary = salary;
    }

    // ============================================================
    // GOVERNMENT DEDUCTION FORMULAS
    // ============================================================

    // SSS contribution based on salary brackets.
    // Bracket formula: increments of ₱22.50 per ₱500 salary bracket starting at ₱3,250.
    static double computeSSS(double salary) {
        if (salary < 3250)   return 135.0;
        if (salary >= 24750) return 1125.0;
        int bracket = (int) ((salary - 3250) / 500);
        return 157.5 + bracket * 22.5;
    }

    // PhilHealth: 3% of monthly salary, employee pays half (1.5%).
    // Floor at ₱150 for salaries at or below ₱10,000.
    // Ceiling at ₱900 for salaries at or above ₱60,000.
    static double computePhilHealth(double salary) {
        if (salary <= 10000) return 150.0;
        if (salary >= 60000) return 900.0;
        return (salary * 0.03) / 2.0;
    }

    // Pag-IBIG: 1% for salaries at or below ₱1,500, otherwise 2%.
    // Capped at ₱100 regardless of salary.
    static double computePagIbig(double salary) {
        double rate = (salary <= 1500) ? 0.01 : 0.02;
        return Math.min(salary * rate, 100.0);
    }

    // Withholding tax using the BIR progressive bracket table.
    public static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20832)  return 0.0;
        if (taxableIncome <= 33332)  return (taxableIncome - 20833)  * 0.20;
        if (taxableIncome <= 66666)  return 2500.0   + (taxableIncome - 33333)  * 0.25;
        if (taxableIncome <= 166666) return 10833.0  + (taxableIncome - 66667)  * 0.30;
        if (taxableIncome <= 666666) return 40833.33 + (taxableIncome - 166667) * 0.32;
        return 200833.33 + (taxableIncome - 666667) * 0.35;
    }

    public double getSalary() {
        return salary;
    }
}
