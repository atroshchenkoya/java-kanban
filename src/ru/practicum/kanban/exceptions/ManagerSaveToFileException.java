package ru.practicum.kanban.exceptions;

public class ManagerSaveToFileException extends RuntimeException {

    public ManagerSaveToFileException(Throwable throwable) {
        super(throwable);
    }
}
