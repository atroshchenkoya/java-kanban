package managers;

import ru.practicum.kanban.entity.Epic;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.entity.TaskStatus;
import ru.practicum.kanban.exceptions.TimeCollisionException;
import ru.practicum.kanban.interfaces.TaskManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.managers.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

        assertEquals(0, addedTask.getId());
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
    void getExceptionIfGetTimeCollision() {
        Task task1 = new Task(0, "Task1", "Description task1",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-18T15:00"), Duration.parse("PT1H10M"));
        Task task4 = new Task(3, "Task4", "Description task4",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-20T15:00"), Duration.parse("PT1H15M"));
        Task task5 = new Task(4, "Task5", "Description task5",
                TaskStatus.NEW, LocalDateTime.parse("0243-10-18T15:00"), Duration.parse("PT1H15M"));
        Task task6 = new Task(5, "Task6", "Description task6",
                TaskStatus.NEW, null, null);
        Task task7 = new Task(6, "Task7", "Description task6",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-20T13:00"), Duration.parse("PT3H15M"));

        taskManager.createTask(task1);
        taskManager.createTask(task4);
        taskManager.createTask(task5);
        taskManager.createTask(task6);
        assertThrowsExactly(
                TimeCollisionException.class,
                ()->taskManager.createTask(task7)
        );
    }

    @Test
    void sortWorksSuccessfully() {
        Task task1 = new Task(0, "Task1", "Description task1",
                TaskStatus.NEW, LocalDateTime.parse("2024-10-18T15:00"), Duration.parse("PT1H10M"));
        Epic epic2 = new Epic(1, "Epic2", "Description epic2", TaskStatus.DONE);
        SubTask subTask3 = new SubTask(2, "Sub Task3", "Description sub task3",
                TaskStatus.DONE, 1, LocalDateTime.parse("2024-10-19T13:20"),
                Duration.parse("PT40M"));
        Task task4 = new Task(3, "Task4", "Description task4",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-18T15:00"), Duration.parse("PT1H15M"));
        Task task5 = new Task(4, "Task5", "Description task5",
                TaskStatus.NEW, LocalDateTime.parse("0243-10-18T15:00"), Duration.parse("PT1H15M"));
        Task task6 = new Task(5, "Task6", "Description task6",
                TaskStatus.NEW, null, null);
        Task task7 = new Task(6, "Task7", "Description task7",
                TaskStatus.NEW, null, null);

        taskManager.createTask(task1);
        taskManager.createEpic(epic2);
        taskManager.createSubTask(subTask3);
        taskManager.createTask(task4);
        taskManager.createTask(task5);
        taskManager.createTask(task6);
        taskManager.createTask(task7);

        Assertions.assertEquals(4,taskManager.getPrioritizedTasks().size());
        Assertions.assertEquals(subTask3,taskManager.getPrioritizedTasks().get(2));
        Assertions.assertEquals(task4, taskManager.getPrioritizedTasks().get(3));
        Assertions.assertEquals(task5, taskManager.getPrioritizedTasks().get(0));
        Assertions.assertEquals(task1, taskManager.getPrioritizedTasks().get(1));
        Assertions.assertEquals("Description task5", taskManager.getPrioritizedTasks().get(0).getDescription());
        Assertions.assertEquals("Task4", taskManager.getPrioritizedTasks().get(3).getName());

    }

    @Test
    void updatedByTimeTaskGiveNoExceptionWhenNoCollisionAndAmountOfSortedTaskCorrect() {
        Task task1 = new Task(0, "Task1", "Description task1",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-20T15:00"), Duration.parse("PT1H10M"));
        Task task2 = new Task(1, "Task2", "Description task2",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-20T17:00"), Duration.parse("PT1H15M"));
        Task task3 = new Task(2, "Task3", "Description task3",
                TaskStatus.NEW, LocalDateTime.parse("2040-10-20T20:00"), Duration.parse("PT1H15M"));
        Task task4 = new Task(2, "Task3Updated", "Description task3Updated",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-20T15:00"), Duration.parse("PT1H20M"));
        Task task5 = new Task(2, "Task3Updated", "Description task3Updated",
                TaskStatus.NEW, LocalDateTime.parse("2028-10-18T15:00"), Duration.parse("PT1H50M"));
        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createTask(task3);
        taskManager.updateTask(task5);

        assertThrowsExactly(
                TimeCollisionException.class,
                ()->taskManager.updateTask(task4)
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
    void checkMultipleUpdateNoDuplicatesInPrioritizedList() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMillis(1);
        LocalDateTime t1 = now.plus(duration);
        Task initialTask = new Task(0, "task", "desc", TaskStatus.IN_PROGRESS, t1, duration);
        taskManager.createTask(initialTask);
        Stream.iterate(0, i -> i + 1)
                .limit(10)
                .map(i -> new Task(initialTask.getId(), "task" + i, "desc" + i, TaskStatus.IN_PROGRESS, now.plus(duration.multipliedBy(i + 2)), duration))
                .forEach(taskManager::updateTask);

        assertEquals(1, taskManager.getPrioritizedTasks().size());
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
        assertEquals(4, taskManager.getHistory().size());
    }
}