package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.user.Person;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserDayCalendar {
    public static final int WORKING_DAY_START_TP = 32400;
    public static final int WORKING_DAY_END_TP = 64800;
    public static final int LUNCH_START_TP = 46800;
    public static final int LUNCH_END_TP = 50400;
    public static final int cellDuration = CalendarCell.DEFAULT_CELL_DURATION;

    private Person user;
    private final List<CalendarCell> calendarCells = new ArrayList<>();
    private int dayStart = WORKING_DAY_START_TP;
    private int dayEnd = WORKING_DAY_END_TP;
    private int plannedLoad = 28800;
    private int factLoad = 0;


    public UserDayCalendar(Integer startTP, Integer endTP){
        if(startTP != null && startTP < dayStart){
            while (startTP < dayStart && dayStart >= cellDuration) dayStart -= cellDuration;
        }
        if(endTP != null && endTP > dayEnd){
            while (endTP > dayEnd && dayEnd < 84600) dayEnd += cellDuration;
        }

        for(int index = dayStart; index <= dayEnd; index += cellDuration){
            calendarCells.add(new CalendarCell(index));
        }
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public List<CalendarCell> getCalendarCells() {
        return calendarCells;
    }

    public UserDayCalendar getCopy(){
        return new UserDayCalendar(dayStart, dayEnd);
    }

    public String getUserLoad(){
        int workLoadMinutes = this.factLoad / 60;
        return workLoadMinutes / 60 + "h " + workLoadMinutes % 60 + "m";
    }

    public String getEfficiency(){
        float efficiency = (float) this.factLoad / (float) this.plannedLoad * 100.0f;
        return Math.round(efficiency * 100) / 100f + "%";
    }

    public void addItem(CalendarItem item){
        Iterator<CalendarCell> iterator = calendarCells.iterator();

        while (iterator.hasNext()){
            CalendarCell cell = iterator.next();
            int itemStartTP = item.getStartDate().toSecondOfDay();
            if((itemStartTP >= cell.getStartTP() && itemStartTP < cell.getStartTP() + cellDuration) || !iterator.hasNext()){
                cell.addItem(item);
                break;
            }
        }

        switch(item.getGroup().getType()) {
            case POTENTIAL:
            case PROJECT:
            case JOB:
                this.factLoad += item.getDuration();
                break;
            case ABSENCE:
                this.plannedLoad -= item.getDuration();
        }
    }

}
