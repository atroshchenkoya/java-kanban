package ru.practicum.kanban.exceptions;

public class ManagerLoadFromFileException extends RuntimeException {

    public ManagerLoadFromFileException(Throwable throwable) {
        super(throwable);
    }

}
