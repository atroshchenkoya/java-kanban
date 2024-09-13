package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {
    @Test
    void epicsWithEqualIdShouldBeEqual() {
        Epic task1 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Epic task2 = new Epic(0, "Dop", "Gop", TaskStatus.DONE);

        assertEquals(task1, task2);
    }
}
