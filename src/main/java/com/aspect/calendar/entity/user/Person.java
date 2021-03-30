package com.aspect.calendar.entity.user;

import com.aspect.calendar.entity.enums.Division;

public class Person {
    private int id;
    private String name;
    private String surname;
    private boolean active;
    private Division division;

    public Person(){}

    public Person(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFullName(){
        return surname + " " + name;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

}
