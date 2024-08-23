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

    public List<Task> getAllTask() {
        return new ArrayList<>(taskStorage.values());
    }

    public List<SubTask> getAllSubTask() {
        return new ArrayList<>(subTaskStorage.values());
    }

    public List<Epic> getAllEpic() {
        return new ArrayList<>(epicStorage.values());
    }

    public void deleteAllTask() {
        taskStorage.clear();
    }

    public void deleteAllSubTask() {
        subTaskStorage.clear();
        for (Epic epic : epicStorage.values()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epic.unLinkAllSubTask();
        }
    }

    public void deleteAllEpic() {
        epicStorage.values().stream()
                .flatMap(epic -> epic.getLinkedSubTask().stream())
                .forEach(subTaskStorage::remove);
        epicStorage.clear();
    }

    public Task getTask(int id) {
        return taskStorage.get(id);
    }

    public SubTask getSubTask(int id) {
        return subTaskStorage.get(id);
    }

    public Epic getEpic(int id) {
        return epicStorage.get(id);
    }

    public void createTask(Task incomingTask) {

        Task taskToCreate = new Task(
                idCounter,
                incomingTask.getName(),
                incomingTask.getDescription(),
                incomingTask.getTaskStatus()
        );

        idCounter++;
        taskStorage.put(taskToCreate.getId(), taskToCreate);
    }

    public void createSubTask(SubTask incomingSubTask) {

        SubTask subTaskToCreate = new SubTask(
                idCounter,
                incomingSubTask.getName(),
                incomingSubTask.getDescription(),
                incomingSubTask.getTaskStatus(),
                incomingSubTask.getLinkedEpicId()
        );

        idCounter++;
        subTaskStorage.put(subTaskToCreate.getId(), subTaskToCreate);
        int epicId = subTaskToCreate.getLinkedEpicId();
        Epic epic = epicStorage.get(epicId);
        epic.linkSubTask(subTaskToCreate.getId());
        setEpicStatus(epic);
    }

    public void createEpic(Epic incomingEpic) {

        Epic epicToCreate = new Epic(
                idCounter,
                incomingEpic.getName(),
                incomingEpic.getDescription(),
                TaskStatus.NEW
        );

        idCounter++;
        epicStorage.put(epicToCreate.getId(), epicToCreate);
    }

    public void updateTask(Task task) {
        taskStorage.put(task.getId(), task);
    }

    public void updateEpic(Epic incomingEpic) {

        Epic forUpdateEpic = new Epic(
                incomingEpic.getId(),
                incomingEpic.getName(),
                incomingEpic.getDescription(),
                TaskStatus.NEW
        );

        forUpdateEpic.linkSubTask((ArrayList<Integer>) epicStorage.get(incomingEpic.getId()).getLinkedSubTask());
        epicStorage.put(forUpdateEpic.getId(), forUpdateEpic);
        setEpicStatus(forUpdateEpic);
    }

    public void updateSubTask(SubTask subTask) {

        SubTask forUpdateSubTask = new SubTask(
                subTask.getId(),
                subTask.getName(),
                subTask.getDescription(),
                subTask.getTaskStatus(),
                subTaskStorage.get(subTask.getId()).getLinkedEpicId()
        );

        subTaskStorage.put(forUpdateSubTask.getId(), forUpdateSubTask);
        int epicId = forUpdateSubTask.getLinkedEpicId();
        Epic epic = epicStorage.get(epicId);
        setEpicStatus(epic);
    }

    public void removeTask(int id) {
        taskStorage.remove(id);
    }

    public void removeSubTask(int id) {
        int epicId = subTaskStorage.get(id).getLinkedEpicId();
        Epic linkedEpic = epicStorage.get(epicId);
        linkedEpic.unLinkSubTask(id);
        setEpicStatus(linkedEpic);
        subTaskStorage.remove(id);
    }

    public void removeEpic(int id) {
        epicStorage.get(id).getLinkedSubTask().forEach(subTaskStorage::remove);
        epicStorage.remove(id);
    }

    public List<SubTask> getAllSubTask(Epic epic) {
        ArrayList<SubTask> subTasksForEpic = new ArrayList<>();
        if (epic.getLinkedSubTask() == null || epic.getLinkedSubTask().isEmpty())
            return subTasksForEpic;
        epic.getLinkedSubTask().forEach(x -> subTasksForEpic.add(subTaskStorage.get(x)));
        return subTasksForEpic;
    }

    private void setEpicStatus(Epic epic) {
        if (epic.getLinkedSubTask() == null || epic.getLinkedSubTask().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epicStorage.put(epic.getId(), epic);
            return;
        }

        ArrayList<Integer> linkedSubTask = (ArrayList<Integer>) epic.getLinkedSubTask();
        int FirstSubTaskId = linkedSubTask.get(0);
        TaskStatus FirstSubTaskStatus = subTaskStorage.get(FirstSubTaskId).getTaskStatus();

        for (int i = 1; i < linkedSubTask.size(); i++) {
            int subTaskId = linkedSubTask.get(i);
            if (FirstSubTaskStatus != subTaskStorage.get(subTaskId).getTaskStatus()) {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                epicStorage.put(epic.getId(), epic);
                return;
            }
        }

        epic.setTaskStatus(FirstSubTaskStatus);
        epicStorage.put(epic.getId(), epic);
    }
}
