package managers;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackerManager {

    private final Map<Integer, Task> taskStorage = new HashMap<>();
    private final Map<Integer, SubTask> subTaskStorage = new HashMap<>();
    private final Map<Integer, Epic> epicStorage = new HashMap<>();
    private int idCounter = 0;

    public List<Task> getTaskStorage() {
        if (taskStorage.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(taskStorage.values());
    }
    public List<SubTask> getSubTaskStorage() {
        if (subTaskStorage.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(subTaskStorage.values());
    }
    public List<Epic> getEpicStorage() {
        if (epicStorage.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(epicStorage.values());
    }

    public void clearTaskStorage() {
        taskStorage.clear();
    }
    public void clearSubTuskStorage() {
        subTaskStorage.clear();
        if (epicStorage.isEmpty())
            return;
        for (Epic epic: epicStorage.values()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epic.clearSubTasksLinks();
        }
    }
    public void clearEpicStorage() {
        epicStorage.forEach((key, value) ->
                value.getSubTasksId().forEach(subTaskStorage::remove));
        epicStorage.clear();
    }

    public Task getTaskById(int id) {
        return taskStorage.get(id);
    }
    public SubTask getSubTaskById(int id) {
        return subTaskStorage.get(id);
    }
    public Epic getEpicById(int id) {
        return epicStorage.get(id);
    }

    public void createTaskInTaskStorage(Task task) {
        task.setId(idCounter);
        idCounter++;
        taskStorage.put(task.getId(), task);
    }

    public void createSubTaskInSubTaskStorage(SubTask subTask) { // TODO: add epic status and link logic
        subTask.setId(idCounter);
        idCounter++;
        subTaskStorage.put(subTask.getId(), subTask);
    }

    public void createEpicInEpicStorage(Epic epic) {
        epic.setId(idCounter);
        idCounter++;
        epicStorage.put(epic.getId(), epic);
    }

    public void updateTaskInTaskStorage(Task task) {
        taskStorage.put(task.getId(), task);
    }

    public void updateEpicInEpicStorage(Epic incomingEpic) {

        Epic forUpdateEpic = new Epic(incomingEpic.getId(),
                incomingEpic.getName(),
                incomingEpic.getDescription(),
                TaskStatus.NEW);

        forUpdateEpic.setSubTasksId((ArrayList<Integer>) epicStorage.get(incomingEpic.getId()).getSubTasksId());
        epicStorage.put(forUpdateEpic.getId(), forUpdateEpic);
        setEpicStatus(forUpdateEpic);
    }

    public void updateSubTaskInSubTaskStorage(SubTask subTask) {
        subTaskStorage.put(subTask.getId(), subTask);
        for (Epic epic: epicStorage.values()) {
            if (epic.getSubTasksId().contains(subTask.getId())) {
                setEpicStatus(epic);
                return;
            }
        }
    }


    private void setEpicStatus(Epic epic) {
        if (epic.getSubTasksId() == null || epic.getSubTasksId().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epicStorage.put(epic.getId(), epic);
            return;
        }
        TaskStatus taskStatus = epicStorage.get(epic.getSubTasksId().get(0)).getTaskStatus();
        for (int i = 1; i < epic.getSubTasksId().size(); i++) {
            if (taskStatus != subTaskStorage.get(epic.getSubTasksId().get(i)).getTaskStatus()) {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                epicStorage.put(epic.getId(), epic);
                return;
            }
        }
        epic.setTaskStatus(taskStatus);
        epicStorage.put(epic.getId(), epic);
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
