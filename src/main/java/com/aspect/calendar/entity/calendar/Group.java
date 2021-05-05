package com.aspect.calendar.entity.calendar;

import com.aspect.calendar.entity.enums.CalendarItemType;
import com.aspect.calendar.entity.user.Person;
import com.aspect.calendar.utils.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.aspect.calendar.utils.CommonUtils.timeFormatter;

public class Group {
    @JsonProperty("id")
    private long id;
    @JsonProperty("name")
    private String name;
    private CalendarItemType type;
    private LocalDateTime creationDate = LocalDateTime.now();
    private Person createdBy;
    private int size;
    private LocalDateTime startDate;
    private LocalDateTime deadline;
    private int duration;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName(){
        return name;
    }

    public CalendarItemType getType() {
        return type;
    }

    public void setType(CalendarItemType type) {
        this.type = type;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean startDatePassed(){
        return LocalDateTime.now().minusMinutes(15).isAfter(startDate);
    }

    public String getPeriod() {
        StringBuilder period = new StringBuilder();
        period.append(startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .append(' ').append(startDate.getDayOfMonth())
                .append(", ").append(startDate.getYear());

        if (startDate.toLocalDate().isEqual(deadline.toLocalDate())) {
            period.append(" (")
                    .append(startDate.toLocalTime().format(timeFormatter))
                    .append(" - ")
                    .append(deadline.toLocalTime().format(timeFormatter))
                    .append(')');
        } else {
            period.append(' ').append(startDate.toLocalTime().format(timeFormatter))
                    .append(" - ")
                    .append(deadline.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                    .append(' ').append(deadline.getDayOfMonth())
                    .append(", ").append(deadline.getYear())
                    .append(' ').append(deadline.toLocalTime().format(timeFormatter));
        }
        return period.toString();
    }

    public String getFormattedDuration(){
        return CommonUtils.formatDuration(duration);
    }

    public boolean isHasCollision(){
        return false;
    }
}
