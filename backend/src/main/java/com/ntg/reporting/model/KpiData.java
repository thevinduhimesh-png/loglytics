package com.ntg.reporting.model;

import java.util.Map;

public class KpiData {
    private String month;
    private Map<String, DivisionKpi> divisions;
    private double totalRevenue;
    private double totalEbitda;
    private double totalGrossProfit;
    private double totalCashFlow;

    public KpiData() {}

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Map<String, DivisionKpi> getDivisions() { return divisions; }
    public void setDivisions(Map<String, DivisionKpi> divisions) { this.divisions = divisions; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public double getTotalEbitda() { return totalEbitda; }
    public void setTotalEbitda(double totalEbitda) { this.totalEbitda = totalEbitda; }
    public double getTotalGrossProfit() { return totalGrossProfit; }
    public void setTotalGrossProfit(double totalGrossProfit) { this.totalGrossProfit = totalGrossProfit; }
    public double getTotalCashFlow() { return totalCashFlow; }
    public void setTotalCashFlow(double totalCashFlow) { this.totalCashFlow = totalCashFlow; }
}
