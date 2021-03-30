package com.aspect.calendar.entity.exceptions;

import com.aspect.calendar.entity.exceptions.FolderCreationException;

public class InvalidValueException extends FolderCreationException {
    public InvalidValueException(String message){
        super(message);
    }
}
