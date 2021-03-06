package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.enums.CalendarItemType;

import java.util.ArrayList;
import java.util.List;

public class Project extends Group {
    private Long xtrfId;
    private String clientEmailSubject;
    private boolean fewTranslatorsAllowed;
    private boolean fewQCAllowed;
    private boolean hasCollision;
    private final List<Job> jobs = new ArrayList<>();

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

    public List<Job> getJobs() {
        return jobs;
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    public boolean hasJobsWithDifferentValues(){
        for(Job job : jobs){
            if(job.valuesDifferent()) return true;
        }
        return false;
    }
}
