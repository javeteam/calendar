package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.enums.CalendarItemType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CalendarItem {
    private int id;
    private Person createdBy = new Person();
    private Person modifiedBy = new Person();
    private Person manager = new Person();
    private Person provider = new Person();
    private LocalDateTime creationDate = LocalDateTime.now();
    private LocalDateTime modificationDate;
    private LocalDate itemDate;
    private LocalTime startDate;
    private LocalTime deadline;
    private CalendarItemType type;
    private String title;
    private String description;
    private int groupId;
    private int positionInGroup;
    private int groupSize;
    private int groupDuration;
    private LocalDateTime groupStartDate;
    private LocalDateTime groupDeadline;

    public CalendarItem(){}

    public CalendarItem(CalendarItem item){
        this.createdBy = item.getCreatedBy();
        this.provider = item.getProvider();
        this.manager = item.getManager();
        this.itemDate = item.getItemDate();
        this.startDate = item.getStartDate();
        this.deadline = item.getDeadline();
        this.type = item.getType();
        this.title = item.getTitle();
        this.groupId = item.getGroupId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Person getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
    }

    public Person getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Person modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Person getManager() {
        return manager;
    }

    public void setManager(Person manager) {
        this.manager = manager;
    }

    public Person getProvider() {
        return provider;
    }

    public void setProvider(Person provider) {
        this.provider = provider;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public void setItemDate(LocalDate itemDate) {
        this.itemDate = itemDate;
    }

    public LocalTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalTime startDate) {
        this.startDate = startDate;
    }

    public LocalTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalTime deadline) {
        this.deadline = deadline;
    }

    public CalendarItemType getType() {
        return type;
    }

    public void setType(CalendarItemType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getPositionInGroup() {
        return positionInGroup;
    }

    public void setPositionInGroup(int positionInGroup) {
        this.positionInGroup = positionInGroup;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    public int getGroupDuration() {
        return groupDuration;
    }

    public void setGroupDuration(int groupDuration) {
        this.groupDuration = groupDuration;
    }

    public LocalDateTime getGroupStartDate() {
        return groupStartDate;
    }

    public void setGroupStartDate(LocalDateTime groupStartDate) {
        this.groupStartDate = groupStartDate;
    }

    public LocalDateTime getGroupDeadline() {
        return groupDeadline;
    }

    public void setGroupDeadline(LocalDateTime groupDeadline) {
        this.groupDeadline = groupDeadline;
    }
}
