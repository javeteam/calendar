package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.user.Person;

import java.time.LocalDateTime;

public class Project {
    private long id;
    private Long xtrfId;
    private String name;
    private String clientEmailSubject;
    private boolean fewTranslatorsAllowed;
    private boolean fewQCAllowed;
    private LocalDateTime creationDate;
    private Person createdBy;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getXtrfId() {
        return xtrfId;
    }

    public void setXtrfId(Long xtrfId) {
        this.xtrfId = xtrfId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Person getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
    }
}
