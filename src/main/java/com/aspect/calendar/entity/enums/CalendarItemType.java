package com.aspect.calendar.entity.enums;

public enum CalendarItemType {
    PROJECT("Project"),
    JOB("Job"),
    POTENTIAL("Potential"),
    ABSENCE("Absence"),
    NOT_AVAILABLE("Not Available");

    private final String title;

    CalendarItemType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
