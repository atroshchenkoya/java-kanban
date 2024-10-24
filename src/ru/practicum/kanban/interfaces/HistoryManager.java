package ru.practicum.kanban.interfaces;

import ru.practicum.kanban.entity.Task;
import java.util.List;

public interface HistoryManager {

    void addTask(Task task);

    List<Task> getHistory();

    void removeTask(Task task);

}
