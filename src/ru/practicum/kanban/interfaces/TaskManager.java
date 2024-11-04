package ru.practicum.kanban.interfaces;

import ru.practicum.kanban.entity.Epic;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.entity.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getAllTask();

    List<SubTask> getAllSubTask();

    List<Epic> getAllEpic();

    List<Task> getPrioritizedTasks();

    void deleteAllTask();

    void deleteAllSubTask();

    void deleteAllEpic();

    Task getTask(int id);

    SubTask getSubTask(int id);

    Epic getEpic(int id);

    void createTask(Task incomingTask);

    void createSubTask(SubTask incomingSubTask);

    void createEpic(Epic incomingEpic);

    void updateTask(Task task);

    void updateEpic(Epic incomingEpic);

    void updateSubTask(SubTask subTask);

    void removeTask(int id);

    void removeSubTask(int id);

    void removeEpic(int id);

    List<SubTask> getAllSubTask(int epicId);

    List<Task> getHistory();
}
