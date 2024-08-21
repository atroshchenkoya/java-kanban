import java.util.HashMap;

public class TrackerManager {

    final HashMap<Integer, Task> taskStorage = new HashMap<>();
    static int idCounter = 0;

    public HashMap<Integer, Task> getTaskStorage() {
        return taskStorage;
    }

    public void clearTaskStorage() {
        taskStorage.clear();
    }

    public Task getTaskById(int id) {
        return taskStorage.get(id);
    }

    public void addTaskToTaskStorage(Task task) {
        task.setId(idCounter);
        taskStorage.put(task.getId(), task);
        idCounter++;
    }

    public void replaceTaskInTaskStorage(Task task) {
        taskStorage.put(task.getId(), task);
    }

    public void removeTaskFromTaskStorageById(int id) {
        taskStorage.remove(id);
    }

    public HashMap<Integer, SubTask> getAllSubTusksForEpic(Epic epic) {
        HashMap<Integer, SubTask> subTasksForEpic = new HashMap<>();
        if (epic.subTasksId == null || epic.subTasksId.isEmpty())
            return subTasksForEpic;

        epic.subTasksId.forEach(x ->
            subTasksForEpic.put(x, (SubTask) taskStorage.get(x))
        );

        return subTasksForEpic;
    }

}
