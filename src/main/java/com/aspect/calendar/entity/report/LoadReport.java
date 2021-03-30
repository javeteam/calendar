package com.aspect.calendar.entity.report;

import com.aspect.calendar.entity.user.Person;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

import static com.aspect.calendar.utils.CommonUtils.dtFormatter;

public class LoadReport {
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final Map<Integer,ReportRow> rows = new HashMap<>();

    public LoadReport(@NotNull LocalDate dateFrom, @NotNull LocalDate dateTo, List<Person> personList){
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        for(Person person : personList){
            ReportRow row = new ReportRow(this.dateFrom, this.dateTo);
            row.setPerson(person);
            rows.put(person.getId(), row);
        }
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public ReportRow getRow(int providerId){
        return rows.get(providerId);
    }

    public List<ReportRow> getRows() {
        List<ReportRow> rows = new ArrayList<>(this.rows.values());
        rows.sort(Comparator
                .comparing((ReportRow r) -> r.getPerson().getDivision())
                .thenComparing((ReportRow r) -> r.getPerson().getFullName())
        );

        for (ReportRow row : rows){
            row.calculateHours();
        }

        return rows;
    }

    public String getPeriodDisplayValue(){
        return "(" + dateFrom.format(dtFormatter) + " - " + dateTo.format(dtFormatter) + ")";
    }

    private String getCSVHeader(){
        StringBuilder sb = new StringBuilder();
        sb.append("\"User Name\"").append(',').append("\"Division\"");

        LocalDate df = dateFrom;
        while (!df.isAfter(dateTo)){
            sb.append(',').append('"').append(df.format(dtFormatter)).append('"');
            df = df.plusDays(1);
        }
        sb.append(',').append("\"Fact\",").append("\"Planned\",").append("\"Idle, %\"").append('\n');
        return sb.toString();
    }

    public String toCSV(){
        StringBuilder sb = new StringBuilder();
        sb.append(getCSVHeader());
        for(ReportRow row: getRows()){
            sb.append(row.toCSV());
        }

        return sb.toString();
    }
}



