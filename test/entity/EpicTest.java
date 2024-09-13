package entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Эпик")
public class EpicTest {
    @Test
    @DisplayName("Должен быть равен эпику с таким же ID")
    void epicsWithEqualIdShouldBeEqual() {
        Epic task1 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Epic task2 = new Epic(0, "Dop", "Gop", TaskStatus.DONE);

        assertEquals(task1, task2);
    }
}
