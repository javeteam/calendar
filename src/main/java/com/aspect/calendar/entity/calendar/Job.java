package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.user.Person;

import java.util.ArrayList;
import java.util.List;

public class Job {
    private Person provider;
    // value in hours
    private float xtrfDuration;
    // value in seconds
    private int calendarDuration;
    List<CalendarItem> calendarItems = new ArrayList<>();

    public Person getProvider() {
        return provider;
    }

    public void setProvider(Person provider) {
        this.provider = provider;
    }

    public float getXtrfDuration() {
        return Math.round(xtrfDuration * 100) / 100f;
    }

    public void addXtrfDuration(float xtrfDuration) {
        this.xtrfDuration += xtrfDuration;
    }

    public float getCalendarDuration() {
        return Math.round(calendarDuration / 36f) / 100f;
    }

    public List<CalendarItem> getCalendarItems() {
        return calendarItems;
    }

    public void addCalendarItem(CalendarItem calendarItem) {
        this.calendarItems.add(calendarItem);
        this.calendarDuration += calendarItem.getDuration();
    }
}
