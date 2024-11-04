package ru.practicum.kanban.managers;

import ru.practicum.kanban.exceptions.TimeCollisionException;
import ru.practicum.kanban.exceptions.not_found_exceptions.EpicNotFoundException;
import ru.practicum.kanban.exceptions.not_found_exceptions.SubTaskNotFoundException;
import ru.practicum.kanban.exceptions.not_found_exceptions.TaskNotFoundException;
import ru.practicum.kanban.interfaces.HistoryManager;
import ru.practicum.kanban.interfaces.TaskManager;
import ru.practicum.kanban.entity.Epic;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.entity.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> taskStorage = new HashMap<>();
    protected final Map<Integer, SubTask> subTaskStorage = new HashMap<>();
    protected final Map<Integer, Epic> epicStorage = new HashMap<>();
    protected final TreeSet<Task> sortedTasksAndSubTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int idCounter = 0;
    private static final String NOT_FOUND_STRING = " not found";

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
            setEpicCalculableAttributesAndSaveEpicInStorage(epic);
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
        Task task = checkIfTaskExists(id);
        historyManager.addTask(task);
        return task;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = checkIfSubTaskExists(id);
        historyManager.addTask(subTask);
        return subTask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = checkIfEpicExists(id);
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
        checkCollisionAndPutInSortedSetForCreate(taskToCreate);
        taskStorage.put(taskToCreate.getId(), taskToCreate);
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
        checkCollisionAndPutInSortedSetForCreate(subTaskToCreate);
        subTaskStorage.put(subTaskToCreate.getId(), subTaskToCreate);
        int epicId = subTaskToCreate.getLinkedEpicId();
        Epic epic = epicStorage.get(epicId);
        epic.linkSubTask(subTaskToCreate.getId());
        setEpicCalculableAttributesAndSaveEpicInStorage(epic);
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
        checkIfTaskExists(task.getId());
        checkCollisionAndPutInSortedSetForUpdateTask(task);
        taskStorage.put(task.getId(), task);
    }

    @Override
    public void updateEpic(Epic incomingEpic) {
        checkIfEpicExists(incomingEpic.getId());
        Epic forUpdateEpic = new Epic(
                incomingEpic.getId(),
                incomingEpic.getName(),
                incomingEpic.getDescription(),
                TaskStatus.NEW
        );

        forUpdateEpic.linkSubTask(epicStorage.get(incomingEpic.getId()).getLinkedSubTask());
        epicStorage.put(forUpdateEpic.getId(), forUpdateEpic);
        setEpicCalculableAttributesAndSaveEpicInStorage(forUpdateEpic);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        checkIfSubTaskExists(subTask.getId());
        SubTask forUpdateSubTask = new SubTask(
                subTask.getId(),
                subTask.getName(),
                subTask.getDescription(),
                subTask.getTaskStatus(),
                subTaskStorage.get(subTask.getId()).getLinkedEpicId(),
                subTask.getStartTime(),
                subTask.getDuration()
        );
        checkCollisionAndPutInSortedSetForUpdateSubTask(forUpdateSubTask);
        putInTaskStorageAndSetLinkedEpicAttributes(forUpdateSubTask);
    }

    @Override
    public void removeTask(int id) {
        checkIfTaskExists(id);
        sortedTasksAndSubTasks.remove(taskStorage.get(id));
        taskStorage.remove(id);
    }

    @Override
    public void removeSubTask(int id) {
        checkIfSubTaskExists(id);
        sortedTasksAndSubTasks.remove(subTaskStorage.get(id));
        int epicId = subTaskStorage.get(id).getLinkedEpicId();
        Epic linkedEpic = epicStorage.get(epicId);
        linkedEpic.unLinkSubTask(id);
        setEpicCalculableAttributesAndSaveEpicInStorage(linkedEpic);
        subTaskStorage.remove(id);
    }

    @Override
    public void removeEpic(int id) {
        checkIfEpicExists(id);
        epicStorage.get(id).getLinkedSubTask().forEach(x -> sortedTasksAndSubTasks.remove(subTaskStorage.get(x)));
        epicStorage.get(id).getLinkedSubTask().forEach(subTaskStorage::remove);
        epicStorage.remove(id);
    }

    @Override
    public List<SubTask> getAllSubTask(int epicId) {
        Epic storagedEpic = checkIfEpicExists(epicId);
        List<SubTask> subTasksForEpic = new ArrayList<>();
        if (storagedEpic.getLinkedSubTask() == null || storagedEpic.getLinkedSubTask().isEmpty())
            return subTasksForEpic;
        storagedEpic.getLinkedSubTask().forEach(x -> subTasksForEpic.add(subTaskStorage.get(x)));
        return subTasksForEpic;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void putInTaskStorageAndSetLinkedEpicAttributes(SubTask forUpdateSubTask) {
        subTaskStorage.put(forUpdateSubTask.getId(), forUpdateSubTask);
        int epicId = forUpdateSubTask.getLinkedEpicId();
        Epic epic = epicStorage.get(epicId);
        setEpicCalculableAttributesAndSaveEpicInStorage(epic);
    }

    protected void setEpicCalculableAttributesAndSaveEpicInStorage(Epic epic) {
        Epic epicWithSetTimeAttributes = setEpicStartTimeAndEndTimeAndDuration(epic);
        Epic epicWithSetTimeAttributesAndStatus = setEpicStatus(epicWithSetTimeAttributes);
        epicStorage.put(epic.getId(), epicWithSetTimeAttributesAndStatus);
    }

    private Epic setEpicStatus(Epic epic) {
        if (epic.getLinkedSubTask() == null || epic.getLinkedSubTask().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else {
            List<Integer> linkedSubTask = epic.getLinkedSubTask();
            int firstSubTaskId = linkedSubTask.getFirst();
            TaskStatus firstSubTaskStatus = subTaskStorage.get(firstSubTaskId).getTaskStatus();
            if (linkedSubTask.stream().anyMatch(x -> subTaskStorage.get(x).getTaskStatus() != firstSubTaskStatus))
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
            else
                epic.setTaskStatus(firstSubTaskStatus);
        }
        return epic;
    }

    private Epic setEpicStartTimeAndEndTimeAndDuration(Epic epic) {
        calculateAndSetStartEpicTime(epic);
        calculateAndSetEndEpicTime(epic);
        calculateAndSetEpicDuration(epic);
        return epic;
    }

    private void calculateAndSetEpicDuration(Epic epic) {
        long epicDurationInMinutes = epic.getLinkedSubTask().stream()
                .map(this::getSubTask)
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();
        Duration epicDuration = Duration.ofMinutes(epicDurationInMinutes);
        epic.setDuration(epicDuration);
    }

    private void calculateAndSetEndEpicTime(Epic epic) {
        LocalDateTime endEpicTime = epic.getLinkedSubTask().stream()
                .map(this::getSubTask)
                .map(Task::getEndTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        epic.setEndTime(endEpicTime);
    }

    private void calculateAndSetStartEpicTime(Epic epic) {
        LocalDateTime startEpicTime = epic.getLinkedSubTask().stream()
                .map(this::getSubTask)
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
        epic.setStartTime(startEpicTime);
    }

    private void removeFromSortedSetById(Task task) {
        sortedTasksAndSubTasks.stream().filter(x -> x.equals(task)).findFirst().ifPresent(sortedTasksAndSubTasks::remove);
    }

    private void checkCollisionAndPutInSortedSetForCreate(Task taskToCreate) {
        if (taskToCreate.getStartTime() == null)
            return;
        if (sortedTasksAndSubTasks.stream().anyMatch(x -> x.getStartTime().equals(taskToCreate.getStartTime()))) {
            throw new TimeCollisionException("Time collision on create - equal start time!!!");
        }
        if (checkTimeCollision(taskToCreate))
            throw new TimeCollisionException("Time collision on create!!!");
        sortedTasksAndSubTasks.add(taskToCreate);
    }

    private void checkCollisionAndPutInSortedSetForUpdateSubTask(SubTask subTaskToUpdate) {
        SubTask oldSubTask = subTaskStorage.get(subTaskToUpdate.getId());
        checkCollisionAndPutInSortedSetForUpdateCommon(subTaskToUpdate, oldSubTask);
    }

    private void checkCollisionAndPutInSortedSetForUpdateTask(Task taskToUpdate) {
        Task oldTask = taskStorage.get(taskToUpdate.getId());
        checkCollisionAndPutInSortedSetForUpdateCommon(taskToUpdate, oldTask);
    }

    private void checkCollisionAndPutInSortedSetForUpdateCommon(Task taskToUpdate, Task oldTask) {
        removeFromSortedSetById(oldTask);
        if (taskToUpdate.getStartTime() == null)
            return;
        checkOnEqualStartTime(taskToUpdate, oldTask);
        sortedTasksAndSubTasks.add(taskToUpdate);
        if (checkTimeCollision(taskToUpdate)) {
            sortedTasksAndSubTasks.remove(taskToUpdate);
            sortedTasksAndSubTasks.add(oldTask);
            throw new TimeCollisionException("Time collision on update!!!");
        }
    }

    private void checkOnEqualStartTime(Task taskToUpdate, Task oldTask) {
        if (sortedTasksAndSubTasks.stream().anyMatch(x -> x.getStartTime().equals(taskToUpdate.getStartTime()))) {
            sortedTasksAndSubTasks.add(oldTask);
            throw new TimeCollisionException("Time collision on update - equal start time!!!");
        }
    }

    private boolean checkTimeCollision(Task task) {
        var closestEndTime = Optional.ofNullable(sortedTasksAndSubTasks.lower(task))
                .map(Task::getEndTime)
                .orElse(LocalDateTime.MIN);
        var closestStartTime = Optional.ofNullable(sortedTasksAndSubTasks.higher(task))
                .map(Task::getStartTime)
                .orElse(LocalDateTime.MAX);
        return closestEndTime.isAfter(task.getStartTime()) || task.getEndTime().isAfter(closestStartTime);
    }

    private Task checkIfTaskExists(int id) {
        Task task = taskStorage.get(id);
        if (task == null) {
            throw new TaskNotFoundException("Task with id: " + id + NOT_FOUND_STRING);
        }
        return task;
    }

    private SubTask checkIfSubTaskExists(int id) {
        SubTask subTask = subTaskStorage.get(id);
        if (subTask == null) {
            throw new SubTaskNotFoundException("SubTask with id: " + id + NOT_FOUND_STRING);
        }
        return subTask;
    }

    private Epic checkIfEpicExists(int id) {
        Epic epic = epicStorage.get(id);
        if (epic == null) {
            throw new EpicNotFoundException("Epic with id: " + id + NOT_FOUND_STRING);
        }
        return epic;
    }
}
