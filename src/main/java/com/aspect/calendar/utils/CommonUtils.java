package com.aspect.calendar.utils;


import com.aspect.calendar.entity.calendar.CalendarCell;
import com.aspect.calendar.entity.calendar.CalendarItem;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;


@Component
public final class CommonUtils {

    public static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter sqlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter sqlDTFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter jDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static String formatDuration(int durationInSeconds){
        int durationMinutes = durationInSeconds/60;
        return durationMinutes/60 + "h " + (durationMinutes % 60 == 0 ? "" : durationMinutes % 60 + "m");
    }

    public static String getCSSStyles(CalendarCell cell, Integer itemOrder){
        if(itemOrder >= cell.getItems().size()) return "";
        CalendarItem item = cell.getItems().get(itemOrder);
        float cellDuration = CalendarCell.DEFAULT_CELL_DURATION;
        float startPoint = (item.getStartDate().toSecondOfDay() - cell.getStartTP()) / cellDuration;
        float durationInCells = (item.getDuration()) / cellDuration;
        int wholeCellsNumber = (int)durationInCells;

        return "left: " + startPoint * 100 + "%; " +
                "width: calc(" + durationInCells * 100 + "% + " + wholeCellsNumber + "px" + ");";
    }

}
