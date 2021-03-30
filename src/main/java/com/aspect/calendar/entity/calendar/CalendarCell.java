package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.form.CalendarItemForm;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.aspect.calendar.entity.calendar.UserDayCalendar.*;

public class CalendarCell {
    public static final int DEFAULT_CELL_DURATION = 30*60;
    private final int startTP;
    private List<CalendarItemForm> items = new ArrayList<>();

    CalendarCell(int startTP){
        this.startTP = startTP;
    }

    public int getStartTP(){
        return startTP;
    }

    public LocalTime getTime(){
        return LocalTime.ofSecondOfDay(startTP);
    }

    public boolean isWorkingHours() {
        return  ( startTP >= WORKING_DAY_START_TP && startTP < LUNCH_START_TP ) || ( startTP >= LUNCH_END_TP && startTP < WORKING_DAY_END_TP );
    }

    public List<CalendarItemForm> getItems() {
        return items;
    }

    public void addItem(CalendarItemForm item){
        item.setCellStartTP(startTP);
        this.items.add(item);
    }

    public int getDefaultDuration(){
        return DEFAULT_CELL_DURATION;
    }
}
