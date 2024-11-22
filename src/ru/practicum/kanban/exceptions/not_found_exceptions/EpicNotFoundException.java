package ru.practicum.kanban.exceptions.not_found_exceptions;

public class EpicNotFoundException extends RuntimeException {

    public EpicNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
