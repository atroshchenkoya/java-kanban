package managers;

import interfaces.HistoryManager;
import interfaces.TaskManager;

public class Managers {
    private static final HistoryManager defaultHistoryManager = new InMemoryHistoryManager();
    private static final TaskManager defaultTaskManager = new InMemoryTaskManager();
    public static TaskManager getDefault() {
        return defaultTaskManager;
    }

    protected static HistoryManager getDefaultHistory() {
        return defaultHistoryManager;
    }

}
