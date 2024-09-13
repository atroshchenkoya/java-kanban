import entity.Task;
import interfaces.TaskManager;
import managers.Managers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {
    @Test
    void checkInitializationOfTaskManager() {
        TaskManager taskManager;

        taskManager = Managers.getDefault();

        assertNotNull(taskManager);
    }
    @Test
    void checkInitializationOfHistoryManagerByGettingHistory() {
        List<Task> historyTasks;

        historyTasks = Managers.getDefault().getHistory();

        assertNotNull(historyTasks);
    }
}
