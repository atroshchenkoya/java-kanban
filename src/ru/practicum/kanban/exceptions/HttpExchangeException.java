package ru.practicum.kanban.exceptions;

public class HttpExchangeException extends RuntimeException {

    public HttpExchangeException(Exception e) {
        super(e);
    }
}
