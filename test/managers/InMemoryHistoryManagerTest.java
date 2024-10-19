package managers;

import entity.Task;
import entity.TaskStatus;
import interfaces.HistoryManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    HistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();

    @Test
    void addFistTaskToHistoryWhenHistoryIsEmptyShouldBeOk() {
        Task task = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);

        inMemoryHistoryManager.addTask(task);

        assertEquals(1, inMemoryHistoryManager.getHistory().size());
        assertTrue(inMemoryHistoryManager.getHistory().contains(task));
    }

    @Test
    void deleteFistAndOnlyTaskFromHistoryShouldBeOk() {
        Task task = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        inMemoryHistoryManager.addTask(task);

        inMemoryHistoryManager.removeTask(task);
        assertTrue(inMemoryHistoryManager.getHistory().isEmpty());
    }

    @Test
    void addTaskToHistoryWhenThisTaskAlreadyInHistoryShouldRemoveTaskFromHistoryAndAddItAgainAsALast() {
        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(1, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task3 = new Task(2, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        inMemoryHistoryManager.addTask(task1);
        inMemoryHistoryManager.addTask(task2);
        inMemoryHistoryManager.addTask(task3);

        inMemoryHistoryManager.addTask(task2);

        assertEquals(inMemoryHistoryManager.getHistory().getLast(), task2);
        assertTrue(inMemoryHistoryManager.getHistory().containsAll(List.of(task1, task2, task3)));
    }

    @Test
    void deleteFirstTaskFromHistoryShouldBeOk() {
        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(1, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task3 = new Task(2, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        inMemoryHistoryManager.addTask(task1);
        inMemoryHistoryManager.addTask(task2);
        inMemoryHistoryManager.addTask(task3);

        inMemoryHistoryManager.removeTask(task1);

        assertEquals(inMemoryHistoryManager.getHistory().getFirst(), task2);
        assertFalse(inMemoryHistoryManager.getHistory().containsAll(List.of(task1, task2, task3)));
    }

    @Test
    void deleteLastTaskFromHistoryShouldBeOk() {
        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(1, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task3 = new Task(2, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        inMemoryHistoryManager.addTask(task1);
        inMemoryHistoryManager.addTask(task2);
        inMemoryHistoryManager.addTask(task3);

        inMemoryHistoryManager.removeTask(task3);

        assertEquals(inMemoryHistoryManager.getHistory().getLast(), task2);
        assertFalse(inMemoryHistoryManager.getHistory().containsAll(List.of(task1, task2, task3)));
    }

    @Test
    void deleteMiddleTaskFromHistoryShouldBeOk() {
        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(1, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task3 = new Task(2, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        inMemoryHistoryManager.addTask(task1);
        inMemoryHistoryManager.addTask(task2);
        inMemoryHistoryManager.addTask(task3);

        inMemoryHistoryManager.removeTask(task2);

        assertEquals(2, inMemoryHistoryManager.getHistory().size());
        assertFalse(inMemoryHistoryManager.getHistory().containsAll(List.of(task1, task2, task3)));
    }
}
