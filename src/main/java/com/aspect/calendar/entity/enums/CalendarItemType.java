package com.aspect.calendar.entity.enums;

public enum CalendarItemType {
    CONFIRMED,
    POTENTIAL,
    ABSENCE,
    NOT_AVAILABLE;


    @Override
    public String toString() {
        return name().substring(0,1) + name().substring(1).toLowerCase();
    }
}
