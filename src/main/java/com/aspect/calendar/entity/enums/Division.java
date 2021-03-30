package com.aspect.calendar.entity.enums;

public enum Division {
    ADMINISTRATOR("Administrator"),
    MANAGER("Project Manager"),
    TRANSLATOR("Translator"),
    QC("QC Manager");

    private final String title;

    Division(String title){
        this.title = title;
    }


    @Override
    public String toString() {
        return title;
    }
}
