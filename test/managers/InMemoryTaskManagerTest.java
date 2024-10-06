package managers;

import entity.Epic;
import entity.SubTask;
import entity.Task;
import entity.TaskStatus;
import interfaces.TaskManager;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private final TaskManager taskManager = Managers.getDefault();

    @Test
    void epicCantBeLinkedToHimself() {
        SubTask subTask1;

        subTask1 = new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, 0);

        assertThrowsExactly(
                NullPointerException.class,
                ()->taskManager.createSubTask(subTask1)
        );
    }
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

    @Test
    void historyHasAllGotByIdItemsAndThereIsNoDuplicates() {
        Epic epic2 = new Epic(0, "Pop2", "Pop3", TaskStatus.IN_PROGRESS);
        Epic epic3 = new Epic(1, "Pop2", "Pop4", TaskStatus.IN_PROGRESS);
        SubTask subTask1 = new SubTask(2, "Pop", "Pop", TaskStatus.IN_PROGRESS, 0);
        SubTask subTask2 = new SubTask(3, "Pop", "Pop", TaskStatus.IN_PROGRESS, 0);
        taskManager.createEpic(epic2);
        taskManager.createEpic(epic3);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        List<Task> gotTasks = new ArrayList<>();
        gotTasks.add(epic2);
        gotTasks.add(epic3);
        gotTasks.add(subTask1);
        gotTasks.add(subTask2);

        taskManager.getEpic(0);
        taskManager.getEpic(0);
        taskManager.getEpic(1);
        taskManager.getSubTask(2);
        taskManager.getSubTask(3);

        assertTrue(taskManager.getHistory().containsAll(gotTasks));
        assertEquals(taskManager.getHistory().getLast(), subTask2);
        assertEquals(taskManager.getHistory().size(), 4);
    }
}