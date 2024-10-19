package managers;

import exceptions.TimeCollisionException;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import entity.Epic;
import entity.SubTask;
import entity.Task;
import entity.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskStorage = new HashMap<>();
    protected final Map<Integer, SubTask> subTaskStorage = new HashMap<>();
    protected final Map<Integer, Epic> epicStorage = new HashMap<>();
    protected final TreeSet<Task> sortedTasksAndSubTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idCounter = 0;

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
    public List<Task> getPrioritizedTasks() {
        return sortedTasksAndSubTasks.stream().toList();
    }

    @Override
    public void deleteAllTask() {
        taskStorage.values().forEach(sortedTasksAndSubTasks::remove);
        taskStorage.clear();
    }

    @Override
    public void deleteAllSubTask() {
        subTaskStorage.values().forEach(sortedTasksAndSubTasks::remove);
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
                .forEach(subTaskId -> {
                    sortedTasksAndSubTasks.remove(subTaskStorage.get(subTaskId));
                    subTaskStorage.remove(subTaskId);
                });
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
                incomingTask.getTaskStatus(),
                incomingTask.getStartTime(),
                incomingTask.getDuration()
        );

        idCounter++;
        taskStorage.put(taskToCreate.getId(), taskToCreate);
        if (taskToCreate.getStartTime() != null) {
            sortedTasksAndSubTasks.add(taskToCreate);
            checkTimeCollision(taskToCreate);
        }
    }

    @Override
    public void createSubTask(SubTask incomingSubTask) {

        SubTask subTaskToCreate = new SubTask(
                idCounter,
                incomingSubTask.getName(),
                incomingSubTask.getDescription(),
                incomingSubTask.getTaskStatus(),
                incomingSubTask.getLinkedEpicId(),
                incomingSubTask.getStartTime(),
                incomingSubTask.getDuration()
        );

        idCounter++;
        subTaskStorage.put(subTaskToCreate.getId(), subTaskToCreate);
        int epicId = subTaskToCreate.getLinkedEpicId();
        Epic epic = epicStorage.get(epicId);
        epic.linkSubTask(subTaskToCreate.getId());
        setEpicCalculableAttributes(epic);
        if (subTaskToCreate.getStartTime() != null) {
            sortedTasksAndSubTasks.add(subTaskToCreate);
            checkTimeCollision(subTaskToCreate);
        }
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
        sortedTasksAndSubTasks.remove(task);
        if (task.getStartTime() != null) {
            sortedTasksAndSubTasks.add(task);
            checkTimeCollision(task);
        }
    }

    private void checkTimeCollision(Task task) {
        if (sortedTasksAndSubTasks.size() <= 1)
            return;
        Task prevTask = sortedTasksAndSubTasks.lower(task);
        Task nextTask = sortedTasksAndSubTasks.higher(task);
        String exceptionMessage = "Time collision!!!";
        checkCollisionWithPrevAndNextNodes(task, prevTask, nextTask, exceptionMessage);
    }

    private static void checkCollisionWithPrevAndNextNodes(Task task, Task prevTask, Task nextTask, String exceptionMessage) {
        if (prevTask == null) {
            whenOneOfTwoNodesIsNullCheckCollision(task, nextTask, exceptionMessage);
            return;
        }
        if (nextTask == null) {
            whenOneOfTwoNodesIsNullCheckCollision(prevTask, task, exceptionMessage);
            return;
        }
        if (prevTask.getEndTime().isAfter(task.getStartTime()) || task.getEndTime().isAfter(nextTask.getStartTime())) {
            throw new TimeCollisionException(exceptionMessage);
        }
    }

    private static void whenOneOfTwoNodesIsNullCheckCollision(Task task, Task nextTask, String exceptionMessage) {
        if (task.getEndTime().isAfter(nextTask.getStartTime())) {
            throw new TimeCollisionException(exceptionMessage);
        }
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
        setEpicCalculableAttributes(forUpdateEpic);
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
        setEpicCalculableAttributes(epic);
        sortedTasksAndSubTasks.remove(forUpdateSubTask);
        if (forUpdateSubTask.getStartTime() != null) {
            sortedTasksAndSubTasks.add(forUpdateSubTask);
            checkTimeCollision(forUpdateSubTask);
        }
    }

    @Override
    public void removeTask(int id) {
        sortedTasksAndSubTasks.remove(taskStorage.get(id));
        taskStorage.remove(id);
    }

    @Override
    public void removeSubTask(int id) {
        sortedTasksAndSubTasks.remove(subTaskStorage.get(id));
        int epicId = subTaskStorage.get(id).getLinkedEpicId();
        Epic linkedEpic = epicStorage.get(epicId);
        linkedEpic.unLinkSubTask(id);
        setEpicCalculableAttributes(linkedEpic);
        subTaskStorage.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        epicStorage.get(id).getLinkedSubTask().forEach(x -> sortedTasksAndSubTasks.remove(subTaskStorage.get(x)));
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

    protected void setEpicCalculableAttributes(Epic epic) {
        setEpicStartTimeAndEndTimeAndDuration(epic);
        setEpicStatus(epic);
    }

    private void setEpicStatus(Epic epic) {
        if (epic.getLinkedSubTask() == null || epic.getLinkedSubTask().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
            epicStorage.put(epic.getId(), epic);
            return;
        }

        List<Integer> linkedSubTask = epic.getLinkedSubTask();
        int firstSubTaskId = linkedSubTask.getFirst();
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

    private void setEpicStartTimeAndEndTimeAndDuration(Epic epic) {
        LocalDateTime startEpicTime = epic.getLinkedSubTask().stream()
                .map(this::getSubTask)
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
        epic.setStartTime(startEpicTime);

        LocalDateTime endEpicTime = epic.getLinkedSubTask().stream()
                .map(this::getSubTask)
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        epic.setEndTime(endEpicTime);

        long epicDurationInMinutes = epic.getLinkedSubTask().stream()
                .map(this::getSubTask)
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        Duration epicDuration = Duration.ofMinutes(epicDurationInMinutes);
        epic.setDuration(epicDuration);

        epicStorage.put(epic.getId(), epic);
    }
}
