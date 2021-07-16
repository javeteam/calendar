package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.entity.enums.CalendarItemType;
import com.aspect.calendar.utils.CommonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CalendarItem {
    private long id;
    private Person createdBy = new Person();
    private Person modifiedBy = new Person();
    private Person manager = new Person();
    private Person provider = new Person();
    private LocalDateTime creationDate;
    private LocalDateTime modificationDate;
    private LocalDate itemDate;
    private LocalTime startDate;
    private LocalTime deadline;
    private String description;
    private Group group;
    private int positionInGroup;
    private int groupDuration;
    private boolean deleted;

    public CalendarItem(){
        this.creationDate = LocalDateTime.now();
        this.group = new Project();
    }

    public CalendarItem(CalendarItem item){
        this.creationDate = LocalDateTime.now();
        this.createdBy = item.getCreatedBy();
        this.provider = item.getProvider();
        this.manager = item.getManager();
        this.itemDate = item.getItemDate();
        this.startDate = item.getStartDate();
        this.deadline = item.getDeadline();
        this.group = item.getGroup();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Person getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Person createdBy) {
        this.createdBy = createdBy;
        this.group.setCreatedBy(createdBy);
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

    public LocalDateTime getStartDateTime(){
        return this.itemDate.atTime(this.startDate);
    }

    public void setStartDate(LocalTime startDate) {
        this.startDate = startDate;
    }

    public LocalTime getDeadline() {
        return deadline;
    }

    public LocalDateTime getDeadlineDateTime(){
        return this.itemDate.atTime(this.deadline);
    }

    public void setDeadline(LocalTime deadline) {
        this.deadline = deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public int getPositionInGroup() {
        return positionInGroup;
    }

    public void setPositionInGroup(int positionInGroup) {
        this.positionInGroup = positionInGroup;
    }

    public int getGroupDuration() {
        return groupDuration;
    }

    public void setGroupDuration(int groupDuration) {
        this.groupDuration = groupDuration;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getDuration(){
        return deadline.toSecondOfDay() - startDate.toSecondOfDay();
    }

    public boolean startDatePassed(){
        return LocalDateTime.now().minusMinutes(15).isAfter(itemDate.atTime(startDate));
    }

    public boolean deadlinePassed(){
        return LocalDateTime.now().minusMinutes(15).isAfter(itemDate.atTime(deadline));
    }

    public String getPeriod(){
        return startDate + " - " + deadline;
    }

    public String getFormattedDuration(){
        return CommonUtils.formatDuration(deadline.toSecondOfDay() - startDate.toSecondOfDay());
    }


}
