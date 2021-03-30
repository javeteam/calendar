package com.aspect.calendar.entity.report;

import com.aspect.calendar.entity.user.Person;

import java.time.LocalDate;
import java.util.*;

public class ReportRow {
    private Person person;
    private float fact;
    private float planned;
    private String idle;
    private final Map<LocalDate, ReportRowUnit> rowUnits;

    public ReportRow(LocalDate from, LocalDate to){
        rowUnits = new HashMap<>();
        LocalDate sd = from;

        while (!sd.isAfter(to)){
            rowUnits.put(sd, new ReportRowUnit(sd));
            sd = sd.plusDays(1);
        }
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public List<ReportRowUnit> getRowUnits(){
        List<ReportRowUnit> list = new ArrayList<>(rowUnits.values());
        list.sort(Comparator.comparing(ReportRowUnit::getDate));
        return list;
    }

    public ReportRowUnit getRowItem(LocalDate itemDate){
        return rowUnits.get(itemDate);
    }

    public void calculateHours(){
        List<ReportRowUnit> items = getRowUnits();
        int fact = 0;
        int planned = 0;
        for(ReportRowUnit item : items){
            fact += item.getLoad();
            planned += item.getExpectedLoad();
        }
        this.fact = Math.round(fact /36f) / 100f;
        this.planned = Math.round(planned /36f) / 100f;
        this.idle = Math.round( (1 - this.fact / this.planned) * 100 * 10) / 10f + "%";
    }

    public float getFact(){
        return fact;
    }

    public float getPlanned(){
        return planned;
    }

    public String getIdle(){
        return idle;
    }

    public String toCSV(){
        List<ReportRowUnit> items = getRowUnits();
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(person.getFullName()).append("\",\"").append(person.getDivision().toString()).append('"');

        for(ReportRowUnit item : items){
            sb.append(',').append(item.toCSV());
        }

        sb.append(',').append('"')
                .append(fact).append("\",\"")
                .append(planned).append("\",\"")
                .append(getIdle()).append('"');

        return sb.append('\n').toString();
    }

}
