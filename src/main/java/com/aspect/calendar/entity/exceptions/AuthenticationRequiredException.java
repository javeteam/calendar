package com.aspect.calendar.entity.exceptions;

public class AuthenticationRequiredException extends Exception {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
