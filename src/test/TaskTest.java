package test;

import entity.Task;
import entity.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {
    @Test
    void tasksWithEqualIdShouldBeEqual() {
        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(0, "Dop", "Gop", TaskStatus.DONE);
        assertEquals(task1, task2);
    }
}
