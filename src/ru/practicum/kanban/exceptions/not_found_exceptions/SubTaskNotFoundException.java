package ru.practicum.kanban.exceptions.not_found_exceptions;

public class SubTaskNotFoundException extends RuntimeException {

    public SubTaskNotFoundException(String message) {
        super(message);
    }
}
