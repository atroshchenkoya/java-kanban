package managers;

import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.interfaces.TaskManager;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.managers.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {
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
