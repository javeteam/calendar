package com.aspect.calendar.utils;

import com.aspect.calendar.entity.user.Person;

import java.time.LocalTime;


public class test {
    static int startTP;
    static int endTP;

    public static boolean status(){


        /*
        int lunchStartTP = UserDayCalendar.LUNCH_START_TP;
        int lunchEndTP = UserDayCalendar.LUNCH_END_TP;
        boolean isDuringLunch = startTP >= lunchStartTP && endTP <= lunchEndTP;
        boolean lunchDuringItem = lunchStartTP > startTP && lunchEndTP < endTP;
        boolean startTPDuringLunch = lunchStartTP <= startTP && lunchEndTP > startTP;
        boolean endTPDuringLunch = lunchStartTP < endTP && lunchEndTP >= endTP;
        return !isDuringLunch && (startTPDuringLunch || endTPDuringLunch || lunchDuringItem);

         */
        //return endTPDuringLunch;
        return false;
    }

    public static void main(String[] args) {
        System.out.println(LocalTime.now());
        /*String fullName = "Крутько Валерій Олександрович";
        Person person = new Person();
        int indexOfSpace = fullName.indexOf(' ');
        if(indexOfSpace > 0){
            person.setSurname(fullName.substring(0, indexOfSpace));
            fullName = fullName.substring(indexOfSpace).trim();
            indexOfSpace = fullName.indexOf(' ');
            if(indexOfSpace > 0) {
                person.setName(fullName.substring(0, indexOfSpace));
            }
            if(indexOfSpace > 0) person.setName(fullName.substring(0, indexOfSpace));
            else if(fullName.length() > 2) person.setName(fullName);
        } else {
            person.setName("-");
            person.setSurname(fullName);
        }

        System.out.println(person.getFullName());*/
    }
}
