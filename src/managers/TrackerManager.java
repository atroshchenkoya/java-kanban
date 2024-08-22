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
    public void createSubTaskInSubTaskStorage(SubTask subTask) {
        subTask.setId(idCounter);
        idCounter++;
        subTaskStorage.put(subTask.getId(), subTask);
        epicStorage.get(subTask.getLinkedEpicId()).addSubTaskIdToSubTasksId(subTask.getId());
        setEpicStatus(epicStorage.get(subTask.getLinkedEpicId()));
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

        SubTask forUpdateSubTask = new SubTask(
                subTask.getId(),
                subTask.getName(),
                subTask.getDescription(),
                subTask.getTaskStatus(),
                subTaskStorage.get(subTask.getId()).getLinkedEpicId());

        subTaskStorage.put(forUpdateSubTask.getId(), forUpdateSubTask);
        setEpicStatus(epicStorage.get(forUpdateSubTask.getLinkedEpicId()));
    }
    public void removeTaskFromTaskStorageById(int id) {
        taskStorage.remove(id);
    }
    public void removeSubTaskFromSubTaskStorageById(int id) {
        epicStorage.get(subTaskStorage.get(id).getLinkedEpicId()).removeSubTaskIdFromSubTasksId(id);
        setEpicStatus(epicStorage.get(subTaskStorage.get(id).getLinkedEpicId()));
        subTaskStorage.remove(id);
    }
    public void removeEpicFromEpicStorageById(int id) {
        epicStorage.get(id).getSubTasksId().forEach(subTaskStorage::remove);
        epicStorage.remove(id);
    }
    public List<SubTask> getAllSubTusksForEpicByEpic(Epic epic) {
        ArrayList<SubTask> subTasksForEpic = new ArrayList<>();
        if (epic.getSubTasksId() == null || epic.getSubTasksId().isEmpty())
            return subTasksForEpic;
        epic.getSubTasksId().forEach(x -> subTasksForEpic.add(subTaskStorage.get(x)));
        return subTasksForEpic;
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

}
