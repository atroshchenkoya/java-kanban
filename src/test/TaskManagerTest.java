package test;

import entity.Epic;
import entity.SubTask;
import entity.Task;
import entity.TaskStatus;
import interfaces.TaskManager;
import managers.Managers;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private final TaskManager taskManager = Managers.getDefault();
    @Test
    void epicContainsAddedToHimSubTask() {
        Epic epic1 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        SubTask subtask1 = new SubTask(1, "Pop", "Pop", TaskStatus.IN_PROGRESS, 0);
        taskManager.createEpic(epic1);
        taskManager.createSubTask(subtask1);
        assertTrue(taskManager.getAllSubTask(epic1).contains(subtask1));
    }
    @Test
    void taskAddedSuccessfully() {
        Task task = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        taskManager.createTask(task);
        Task addedTask = taskManager.getTask(0);
        assertEquals(addedTask.getId(), 0);
        assertEquals(addedTask.getTaskStatus(), task.getTaskStatus());
        assertEquals(addedTask.getName(), task.getName());
        assertEquals(addedTask.getDescription(), task.getDescription());
    }

    @Test
    void subTaskAddThrowExceptionWhenNotEpicFound() {
        SubTask subTask = new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, 5);
        assertThrowsExactly(
                NullPointerException.class,
                ()->taskManager.createSubTask(subTask)
        );
    }

    @Test
    void subTaskAddSuccessfullyWhenEpicExists() {
        SubTask subTask = new SubTask(1, "Pop", "Pop", TaskStatus.IN_PROGRESS, 0);
        Epic epic = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        taskManager.createEpic(epic);
        taskManager.createSubTask(subTask);
        assertEquals(taskManager.getSubTask(1), subTask);
    }

}