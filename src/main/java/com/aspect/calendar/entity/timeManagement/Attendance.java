package com.aspect.calendar.entity.timeManagement;

import com.aspect.calendar.entity.user.Person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Attendance {
    private int id;
    private Person user;
    private LocalDate date;
    private LocalTime clockIn;
    private LocalDateTime actualClockIn;
    private LocalTime clockOut;
    private LocalDateTime actualClockOut;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Person getUser() {
        return user;
    }

    public void setUser(Person user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getClockIn() {
        return clockIn;
    }

    public void setClockIn(LocalTime clockIn) {
        this.clockIn = clockIn;
    }

    public LocalDateTime getActualClockIn() {
        return actualClockIn;
    }

    public void setActualClockIn(LocalDateTime actualClockIn) {
        this.actualClockIn = actualClockIn;
    }

    public LocalTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalTime clockOut) {
        this.clockOut = clockOut;
    }

    public LocalDateTime getActualClockOut() {
        return actualClockOut;
    }

    public void setActualClockOut(LocalDateTime actualClockOut) {
        this.actualClockOut = actualClockOut;
    }
}
