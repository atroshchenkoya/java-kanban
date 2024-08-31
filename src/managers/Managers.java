package managers;

import interfaces.HistoryManager;
import interfaces.TaskManager;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    protected static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
