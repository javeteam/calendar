package com.aspect.calendar.entity.report;

import java.time.LocalDate;

public class ReportRowUnit {
    private final LocalDate date;
    private final int EXPECTED_LOAD = 28800;
    private int confirmed;
    private int potential;
    private int absence;

    public ReportRowUnit(LocalDate date){
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setConfirmed(int confirmed) {
        this.confirmed = confirmed;
    }

    public void setPotential(int potential) {
        this.potential = potential;
    }

    public void setAbsence(int absence) {
        this.absence = absence;
    }

    public int getLoad(){
        return confirmed + potential;
    }

    public String getFormattedLoad(){
        if(getLoad() == 0) return "0";
        int hours = getLoad() / 60 / 60;
        int minutes = getLoad() / 60 % 60;
        String h = hours > 0 ? hours + "h " : "";
        String m = minutes > 0 ? minutes + "m" : "";

        return h + m;
    }

    public int getExpectedLoad(){
        if(date.getDayOfWeek().getValue() <= 5) return EXPECTED_LOAD - absence;
        else return 0;
    }

    public String getFormattedExpectedLoad(){
        if(getExpectedLoad() == 0) return "0";
        int hours = getExpectedLoad() / 60 / 60;
        int minutes = getExpectedLoad() / 60 % 60;
        String h = hours > 0 ? hours + "h " : "";
        String m = minutes > 0 ? minutes + "m" : "";

        return h + m;
    }

    public String toCSV(){
        return "\"" + getFormattedLoad() + "/" + getFormattedExpectedLoad() + "\"";
    }
}
