package ru.practicum.kanban.exceptions;

public class TimeCollisionException extends RuntimeException {

    public TimeCollisionException(String errorMessage) {
        super(errorMessage);
    }
}
