package com.aspect.calendar.form;

import com.aspect.calendar.entity.calendar.CalendarCell;
import com.aspect.calendar.entity.enums.CalendarItemType;

public class CalendarItemForm {
    private int id;
    private int providerId;
    private String providerName;
    private int managerId;
    private String managerName;
    private String createdByName;
    private String creationDate;
    private String modifiedByName;
    private String modificationDate;
    private String itemDate;
    private int cellStartTP;
    private int startTP;
    private int endTP;
    private int duration;
    private String period;
    private CalendarItemType type;
    private String title;
    private String description;
    private int groupId;
    private int positionInGroup;
    private int groupSize;
    private String groupPeriod;
    private int groupDuration;
    boolean groupStartDatePassed;
    boolean startDatePassed;
    boolean deadlinePassed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProviderId() {
        return providerId;
    }

    public void setProviderId(int providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }


    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getModifiedByName() {
        return modifiedByName;
    }

    public void setModifiedByName(String modifiedByName) {
        this.modifiedByName = modifiedByName;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getItemDate() {
        return itemDate;
    }

    public void setItemDate(String itemDate) {
        this.itemDate = itemDate;
    }

    public void setCellStartTP(int cellStartTP) {
        this.cellStartTP = cellStartTP;
    }

    public int getStartTP() {
        return startTP;
    }

    public void setStartTP(int startTP) {
        this.startTP = startTP;
    }

    public int getEndTP() {
        return endTP;
    }

    public void setEndTP(int endTP) {
        this.endTP = endTP;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFormattedDuration(){
        return formatDuration(duration);
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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

    public String getShortTitle(){
        if(title != null && title.matches("^\\d{4}_\\d{4}.*")) return title.substring(5,9) + '_' + title.substring(0,4);
        else return title;
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

    public String getGroupPeriod() {
        return groupPeriod;
    }

    public void setGroupPeriod(String groupPeriod) {
        this.groupPeriod = groupPeriod;
    }

    public int getGroupDuration() {
        return groupDuration;
    }

    public String getFormattedGroupDuration(){
        return formatDuration(groupDuration);
    }

    public void setGroupDuration(int groupDuration) {
        this.groupDuration = groupDuration;
    }

    public boolean isGroupStartDatePassed(){
        return groupStartDatePassed;
    }

    public void setGroupStartDatePassed(boolean groupStartDatePassed) {
        this.groupStartDatePassed = groupStartDatePassed;
    }

    public boolean isStartDatePassed() {
        return startDatePassed;
    }

    public void setStartDatePassed(boolean startDatePassed) {
        this.startDatePassed = startDatePassed;
    }

    public boolean isDeadlinePassed() {
        return deadlinePassed;
    }

    public void setDeadlinePassed(boolean deadlinePassed) {
        this.deadlinePassed = deadlinePassed;
    }

    public String getCSSStyles(){
        float cellDuration = CalendarCell.DEFAULT_CELL_DURATION;
        float startPoint = (startTP - cellStartTP) / cellDuration;
        float durationInCells = duration / cellDuration;
        int wholeCellsNumber = (int)durationInCells;
        StringBuilder sb = new StringBuilder();
        sb.append("left: ").append(startPoint * 100).append("%; ");
        sb.append("width: calc(").append(durationInCells * 100).append("% + ").append(wholeCellsNumber).append("px").append(");");

        return sb.toString();
    }

    private String formatDuration(int durationInSeconds){
        int durationMinutes = durationInSeconds/60;
        return durationMinutes/60 + "h " + (durationMinutes % 60 == 0 ? "" : durationMinutes % 60 + "m");
    }

    public int getStartHour(){
        return this.startTP / 60 / 60;
    }

    public int getStartMinute(){
        return this.startTP / 60 % 60;
    }

    public int getDeadlineHour(){
        return this.endTP / 60 / 60;
    }

    public int getDeadlineMinute(){
        return this.endTP / 60 % 60;
    }


}
