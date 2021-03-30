package com.aspect.calendar.utils;

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
        startTP = LocalTime.of(12,0).toSecondOfDay();
        endTP = LocalTime.of(15,0).toSecondOfDay();
        System.out.println(status());

        startTP = LocalTime.of(13,0).toSecondOfDay();
        endTP = LocalTime.of(15,0).toSecondOfDay();
        System.out.println(status());

        startTP = LocalTime.of(13,0).toSecondOfDay();
        endTP = LocalTime.of(14,0).toSecondOfDay();
        System.out.println(status());

        startTP = LocalTime.of(14,0).toSecondOfDay();
        endTP = LocalTime.of(15,0).toSecondOfDay();
        System.out.println(status());
    }
}
