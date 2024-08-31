package test;

import managers.Managers;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {
    @Test
    void checkInitializationOfTaskManager() {
        assertNotNull(Managers.getDefault());
    }
    @Test
    void checkInitializationOfHistoryManager() {
        assertNotNull(Managers.getDefault().getHistory());
    }
}
