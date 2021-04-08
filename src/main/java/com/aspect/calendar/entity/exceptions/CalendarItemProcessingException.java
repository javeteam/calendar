package com.aspect.calendar.entity.exceptions;

import org.springframework.http.HttpStatus;

public class CalendarItemProcessingException extends Exception {
    private final HttpStatus responseStatus;

    public CalendarItemProcessingException(String message) {
        super(message);
        this.responseStatus = HttpStatus.BAD_REQUEST;
    }

    public CalendarItemProcessingException(String message, HttpStatus responseStatus) {
        super(message);
        this.responseStatus = responseStatus;
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }
}
