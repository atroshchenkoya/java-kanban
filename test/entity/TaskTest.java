package entity;

import org.junit.jupiter.api.Test;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.entity.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {
    @Test
    void tasksWithEqualIdShouldBeEqual() {
        Task task1;
        Task task2;

        task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        task2 = new Task(0, "Dop", "Gop", TaskStatus.DONE);

        assertEquals(task1, task2);
    }
}
