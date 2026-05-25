package core.models;

public class PayrollBreakdown {

    private final double firstCutoffGross;
    private final double secondCutoffGross;
    private final double totalMonthlyGross;
    private final double sss;
    private final double philHealth;
    private final double pagIbig;
    private final double withholdingTax;
    private final double totalDeductions;
    private final double firstCutoffNet;
    private final double secondCutoffNet;

    public PayrollBreakdown(
            double firstCutoffGross,
            double secondCutoffGross,
            double totalMonthlyGross,
            double sss,
            double philHealth,
            double pagIbig,
            double withholdingTax,
            double totalDeductions,
            double firstCutoffNet,
            double secondCutoffNet) {
        this.firstCutoffGross = firstCutoffGross;
        this.secondCutoffGross = secondCutoffGross;
        this.totalMonthlyGross = totalMonthlyGross;
        this.sss = sss;
        this.philHealth = philHealth;
        this.pagIbig = pagIbig;
        this.withholdingTax = withholdingTax;
        this.totalDeductions = totalDeductions;
        this.firstCutoffNet = firstCutoffNet;
        this.secondCutoffNet = secondCutoffNet;
    }

    public double getFirstCutoffGross() {
        return firstCutoffGross;
    }

    public double getSecondCutoffGross() {
        return secondCutoffGross;
    }

    public double getTotalMonthlyGross() {
        return totalMonthlyGross;
    }

    public double getSss() {
        return sss;
    }

    public double getPhilHealth() {
        return philHealth;
    }

    public double getPagIbig() {
        return pagIbig;
    }

    public double getWithholdingTax() {
        return withholdingTax;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getFirstCutoffNet() {
        return firstCutoffNet;
    }

    public double getSecondCutoffNet() {
        return secondCutoffNet;
    }
}
