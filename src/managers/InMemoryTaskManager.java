package managers;

import interfaces.HistoryManager;
import interfaces.TaskManager;
import entity.Epic;
import entity.SubTask;
import entity.Task;
import entity.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskStorage = new HashMap<>();
    protected final Map<Integer, SubTask> subTaskStorage = new HashMap<>();
    protected final Map<Integer, Epic> epicStorage = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private int idCounter = 0;

    @Override
    public List<Task> getAllTask() {
        return new ArrayList<>(taskStorage.values());
    }

    @Override
    public List<SubTask> getAllSubTask() {
        return new ArrayList<>(subTaskStorage.values());
    }

    @Override
    public List<Epic> getAllEpic() {
        return new ArrayList<>(epicStorage.values());
    }

    @Override
    public void deleteAllTask() {
        taskStorage.clear();
    }

    @Override
    public void deleteAllSubTask() {
        subTaskStorage.clear();
        for (Epic epic : epicStorage.values()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epic.unLinkAllSubTask();
        }
    }

    @Override
    public void deleteAllEpic() {
        epicStorage.values().stream()
                .flatMap(epic -> epic.getLinkedSubTask().stream())
                .forEach(subTaskStorage::remove);
        epicStorage.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = taskStorage.get(id);
        if (task == null)
            return null;
        historyManager.addTask(task);
        return task;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = subTaskStorage.get(id);
        if (subTask == null)
            return null;
        historyManager.addTask(subTask);
        return subTask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epicStorage.get(id);
        if (epic == null)
            return null;
        historyManager.addTask(epic);
        return epic;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void updateTask(Task task) {
        taskStorage.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic incomingEpic) {

        Epic forUpdateEpic = new Epic(
                incomingEpic.getId(),
                incomingEpic.getName(),
                incomingEpic.getDescription(),
                TaskStatus.NEW
        );

        forUpdateEpic.linkSubTask(epicStorage.get(incomingEpic.getId()).getLinkedSubTask());
        epicStorage.put(forUpdateEpic.getId(), forUpdateEpic);
        setEpicStatus(forUpdateEpic);
    }

    @Override
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

    @Override
    public void removeTask(int id) {
        taskStorage.remove(id);
    }

    @Override
    public void removeSubTask(int id) {
        int epicId = subTaskStorage.get(id).getLinkedEpicId();
        Epic linkedEpic = epicStorage.get(epicId);
        linkedEpic.unLinkSubTask(id);
        setEpicStatus(linkedEpic);
        subTaskStorage.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        epicStorage.get(id).getLinkedSubTask().forEach(subTaskStorage::remove);
        epicStorage.remove(id);
    }

    @Override
    public List<SubTask> getAllSubTask(Epic epic) {
        List<SubTask> subTasksForEpic = new ArrayList<>();
        Epic storagedEpic = epicStorage.get(epic.getId());
        if (storagedEpic.getLinkedSubTask() == null || storagedEpic.getLinkedSubTask().isEmpty())
            return subTasksForEpic;
        storagedEpic.getLinkedSubTask().forEach(x -> subTasksForEpic.add(subTaskStorage.get(x)));
        return subTasksForEpic;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public void setEpicStatus(Epic epic) {
        if (epic.getLinkedSubTask() == null || epic.getLinkedSubTask().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epicStorage.put(epic.getId(), epic);
            return;
        }

        List<Integer> linkedSubTask = epic.getLinkedSubTask();
        int firstSubTaskId = linkedSubTask.get(0);
        TaskStatus firstSubTaskStatus = subTaskStorage.get(firstSubTaskId).getTaskStatus();

        for (int i = 1; i < linkedSubTask.size(); i++) {
            int subTaskId = linkedSubTask.get(i);
            if (firstSubTaskStatus != subTaskStorage.get(subTaskId).getTaskStatus()) {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                epicStorage.put(epic.getId(), epic);
                return;
            }
        }
        epic.setTaskStatus(firstSubTaskStatus);
        epicStorage.put(epic.getId(), epic);
    }
}
