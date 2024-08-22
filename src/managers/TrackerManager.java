package managers;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.HashMap;

public class TrackerManager {

    private final HashMap<Integer, Task> taskStorage = new HashMap<>();
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

    public void updateTaskInTaskStorage(Task task) { //TODO: Если типы обновляемого и обновления не совпадают - логика усложнится
        taskStorage.put(task.getId(), task);
        if (task instanceof Epic) {
            setEpicStatus((Epic) task);
        }
        if (task instanceof SubTask) {
            setEpicStatusBySubTaskUpdated((SubTask) task);
        }
    }

    private void setEpicStatusBySubTaskUpdated(SubTask subTask) {
        for (Task task: taskStorage.values()) {
            if (task instanceof Epic epic) {
                if (epic.getSubTasksId().contains(subTask.getId())) {
                    setEpicStatus(epic);
                    return;
                }
            }
        }
    }

    private void setEpicStatus(Epic epic) {
        if (epic.getSubTasksId() == null || epic.getSubTasksId().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            taskStorage.put(epic.getId(), epic);
            return;
        }
        TaskStatus taskStatus = taskStorage.get(epic.getSubTasksId().get(0)).getTaskStatus();
        for (int i = 1; i < epic.getSubTasksId().size(); i++) {
            if (taskStatus != taskStorage.get(epic.getSubTasksId().get(i)).getTaskStatus()) {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                taskStorage.put(epic.getId(), epic);
                return;
            }
        }
        epic.setTaskStatus(taskStatus);
        taskStorage.put(epic.getId(), epic);
    }

    public void addTaskInTaskStorage(Task task) {
        task.setId(idCounter);
        idCounter++;
        taskStorage.put(task.getId(), task);
        if (task instanceof Epic) {
            setEpicStatus((Epic) task);
        }
    }

    public void removeTaskFromTaskStorageById(int id) {
        if (taskStorage.get(id) instanceof SubTask) {
            setEpicStatusBySubTaskDeleted((SubTask) taskStorage.get(id));
            return;
        }
        taskStorage.remove(id);
    }

    private void setEpicStatusBySubTaskDeleted(SubTask subTask) {
        taskStorage.remove(subTask.getId());
        for (Task task: taskStorage.values()) {
            if (task instanceof Epic epic) {
                if (epic.getSubTasksId().contains(subTask.getId())) {
                    epic.getSubTasksId().removeIf(x -> x == subTask.getId());
                    setEpicStatus(epic);
                    return;
                }
            }
        }
    }

    public HashMap<Integer, SubTask> getAllSubTusksForEpic(Epic epic) {
        HashMap<Integer, SubTask> subTasksForEpic = new HashMap<>();
        if (epic.getSubTasksId() == null || epic.getSubTasksId().isEmpty())
            return subTasksForEpic;

        epic.getSubTasksId().forEach(x ->
            subTasksForEpic.put(x, (SubTask) taskStorage.get(x))
        );

        return subTasksForEpic;
    }

}
