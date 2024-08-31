package test;

import entity.SubTask;
import entity.TaskStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubTuskTest {
    @Test
    void SubTasksWithEqualIdShouldBeEqual() {
        SubTask task1 = new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, 2);
        SubTask task2 = new SubTask(0, "Dop", "Gop", TaskStatus.DONE, 1);
        assertEquals(task1, task2);
    }
}
