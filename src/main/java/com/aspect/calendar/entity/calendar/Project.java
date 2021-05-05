package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.enums.CalendarItemType;

public class Project extends Group {
    private Long xtrfId;
    private String clientEmailSubject;
    private boolean fewTranslatorsAllowed;
    private boolean fewQCAllowed;
    private boolean hasCollision;

    public Project(){
        setType(CalendarItemType.PROJECT);
    }

    public String getShortName(){
        if(getName() != null && getName().matches("^\\d{4}_\\d{4}.*")) return getName().substring(5,9) + '_' + getName().substring(0,4);
        else return getName();
    }

    public Long getXtrfId() {
        return xtrfId;
    }

    public void setXtrfId(Long xtrfId) {
        this.xtrfId = xtrfId;
    }

    public String getClientEmailSubject() {
        return clientEmailSubject;
    }

    public void setClientEmailSubject(String clientEmailSubject) {
        this.clientEmailSubject = (clientEmailSubject != null && clientEmailSubject.isBlank()) ? null : clientEmailSubject;
    }

    public boolean isFewTranslatorsAllowed() {
        return fewTranslatorsAllowed;
    }

    public void setFewTranslatorsAllowed(boolean fewTranslatorsAllowed) {
        this.fewTranslatorsAllowed = fewTranslatorsAllowed;
    }

    public boolean isFewQCAllowed() {
        return fewQCAllowed;
    }

    public void setFewQCAllowed(boolean fewQCAllowed) {
        this.fewQCAllowed = fewQCAllowed;
    }

    public boolean isHasCollision() {
        return hasCollision;
    }

    public void setHasCollision(boolean hasCollision) {
        this.hasCollision = hasCollision;
    }
}
