package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.form.CalendarItemForm;

import java.util.ArrayList;
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
    private int plannedLoad;
    private int factLoad;


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

    private void setEfficiencyUnits() {
        this.plannedLoad = 28800;
        this.factLoad = 0;

        for(CalendarCell cell : calendarCells){
            for(CalendarItemForm item: cell.getItems()){
                switch(item.getType()) {
                    case POTENTIAL:
                    case CONFIRMED:
                        this.factLoad += item.getDuration();
                        break;
                    case ABSENCE:
                        this.plannedLoad -= item.getDuration();
                }
            }
        }
    }

    public void setItems(List<CalendarItemForm> calendarItems){
        int cellNumber = 0;
        for(CalendarItemForm item : calendarItems){
            int itemTP = item.getStartTP();
            for( ;cellNumber < calendarCells.size(); cellNumber++){
                int sellTP = calendarCells.get(cellNumber).getStartTP();
                if(itemTP == sellTP){
                    calendarCells.get(cellNumber).addItem(item);
                    break;
                } else if(itemTP > sellTP) {
                    // this sell is final
                    if(cellNumber == calendarCells.size() -1){
                        calendarCells.get(cellNumber).addItem(item);
                        break;
                    } else if(itemTP < sellTP + cellDuration){
                        calendarCells.get(cellNumber).addItem(item);
                        break;
                    }
                }
            }
        }
        this.setEfficiencyUnits();
    }


}
