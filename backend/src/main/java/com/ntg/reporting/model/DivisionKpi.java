package com.ntg.reporting.model;

public class DivisionKpi {
    private String division;
    private double revenue;
    private double revenueYtd;
    private double ebitda;
    private double ebitdaYtd;
    private double grossProfit;
    private double ebitdaMargin;
    private double grossMargin;
    private double cashFlow;

    public DivisionKpi() {}

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
    public double getRevenueYtd() { return revenueYtd; }
    public void setRevenueYtd(double revenueYtd) { this.revenueYtd = revenueYtd; }
    public double getEbitda() { return ebitda; }
    public void setEbitda(double ebitda) { this.ebitda = ebitda; }
    public double getEbitdaYtd() { return ebitdaYtd; }
    public void setEbitdaYtd(double ebitdaYtd) { this.ebitdaYtd = ebitdaYtd; }
    public double getGrossProfit() { return grossProfit; }
    public void setGrossProfit(double grossProfit) { this.grossProfit = grossProfit; }
    public double getEbitdaMargin() { return ebitdaMargin; }
    public void setEbitdaMargin(double ebitdaMargin) { this.ebitdaMargin = ebitdaMargin; }
    public double getGrossMargin() { return grossMargin; }
    public void setGrossMargin(double grossMargin) { this.grossMargin = grossMargin; }
    public double getCashFlow() { return cashFlow; }
    public void setCashFlow(double cashFlow) { this.cashFlow = cashFlow; }
}
