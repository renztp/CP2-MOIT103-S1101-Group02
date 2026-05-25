package core.services;

public class DeductionCalculator {
    private static final double SSS_BRACKET_BASE_SALARY = 3250.0;
    private static final double SSS_BRACKET_STEP = 500.0;
    private static final double SSS_BRACKET_BASE_CONTRIBUTION = 157.5;
    private static final double SSS_BRACKET_STEP_AMOUNT = 22.5;
    private static final double SSS_MINIMUM_SALARY = 3250.0;
    private static final double SSS_MINIMUM_CONTRIBUTION = 135.0;
    private static final double SSS_MAXIMUM_SALARY = 24750.0;
    private static final double SSS_MAXIMUM_CONTRIBUTION = 1125.0;

    private static final double PHILHEALTH_RATE = 0.03;
    private static final double PHILHEALTH_MINIMUM_SALARY = 10000.0;
    private static final double PHILHEALTH_MINIMUM_PREMIUM = 150.0;
    private static final double PHILHEALTH_MAXIMUM_SALARY = 60000.0;
    private static final double PHILHEALTH_MAXIMUM_PREMIUM = 900.0;

    private static final double PAGIBIG_LOW_RATE = 0.01;
    private static final double PAGIBIG_HIGH_RATE = 0.02;
    private static final double PAGIBIG_LOW_SALARY_THRESHOLD = 1500.0;
    private static final double PAGIBIG_MAXIMUM_CONTRIBUTION = 100.0;

    private static final double TAX_BRACKET_1_MAX = 20832.0;
    private static final double TAX_BRACKET_2_MAX = 33332.0;
    private static final double TAX_BRACKET_3_MAX = 66666.0;
    private static final double TAX_BRACKET_4_MAX = 166666.0;
    private static final double TAX_BRACKET_5_MAX = 666666.0;

    public static double computeSSSContribution(double grossSalary) {
        if (grossSalary < SSS_MINIMUM_SALARY) {
            return SSS_MINIMUM_CONTRIBUTION;
        }
        if (grossSalary >= SSS_MAXIMUM_SALARY) {
            return SSS_MAXIMUM_CONTRIBUTION;
        }
        int salaryBracket = (int) ((grossSalary - SSS_BRACKET_BASE_SALARY) / SSS_BRACKET_STEP);
        return SSS_BRACKET_BASE_CONTRIBUTION + salaryBracket * SSS_BRACKET_STEP_AMOUNT;
    }

    public static double computePhilHealthContribution(double grossSalary) {
        if (grossSalary <= PHILHEALTH_MINIMUM_SALARY) {
            return PHILHEALTH_MINIMUM_PREMIUM;
        }
        if (grossSalary >= PHILHEALTH_MAXIMUM_SALARY) {
            return PHILHEALTH_MAXIMUM_PREMIUM;
        }
        return (grossSalary * PHILHEALTH_RATE) / 2.0;
    }

    public static double computePagIbigContribution(double grossSalary) {
        double contributionRate = (grossSalary <= PAGIBIG_LOW_SALARY_THRESHOLD)
                ? PAGIBIG_LOW_RATE
                : PAGIBIG_HIGH_RATE;
        return Math.min(grossSalary * contributionRate, PAGIBIG_MAXIMUM_CONTRIBUTION);
    }

    public static double computeWithholdingTax(double taxableIncome) {
        if (taxableIncome <= TAX_BRACKET_1_MAX) {
            return 0.0;
        }
        if (taxableIncome <= TAX_BRACKET_2_MAX) {
            return (taxableIncome - 20833) * 0.20;
        }
        if (taxableIncome <= TAX_BRACKET_3_MAX) {
            return 2500.0 + (taxableIncome - 33333) * 0.25;
        }
        if (taxableIncome <= TAX_BRACKET_4_MAX) {
            return 10833.0 + (taxableIncome - 66667) * 0.30;
        }
        if (taxableIncome <= TAX_BRACKET_5_MAX) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        }
        return 200833.33 + (taxableIncome - 666677) * 0.35;
    }
}
