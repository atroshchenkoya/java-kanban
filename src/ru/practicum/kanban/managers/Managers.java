package ru.practicum.kanban.managers;

import ru.practicum.kanban.interfaces.HistoryManager;
import ru.practicum.kanban.interfaces.TaskManager;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    protected static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
