package com.aspect.calendar.utils;


import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;


@Component
public final class CommonUtils {

    public static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter sqlDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter sqlDTFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter jDateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

}
