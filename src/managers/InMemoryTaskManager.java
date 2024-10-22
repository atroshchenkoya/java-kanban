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
import java.util.*;

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
        setEpicCalculableAttributes(epic);
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
        if (task.getStartTime() == null) {
            taskStorage.put(task.getId(), task);
            removeFromSortedSetById(task);
            return;
        }
        checkCollisionAndPutInSortedSetForUpdate(task);
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
        if (subTask.getStartTime() == null) {
            removeFromSortedSetById(subTask);
            putInTaskStorageAndSetLinkedEpicAttributes(forUpdateSubTask);
            return;
        }
        checkCollisionAndPutInSortedSetForUpdate(forUpdateSubTask);
        putInTaskStorageAndSetLinkedEpicAttributes(forUpdateSubTask);
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

    private void putInTaskStorageAndSetLinkedEpicAttributes(SubTask forUpdateSubTask) {
        subTaskStorage.put(forUpdateSubTask.getId(), forUpdateSubTask);
        int epicId = forUpdateSubTask.getLinkedEpicId();
        Epic epic = epicStorage.get(epicId);
        setEpicCalculableAttributes(epic);
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

    private void removeFromSortedSetById(Task task) {
        sortedTasksAndSubTasks.stream().filter(x -> x.equals(task)).findFirst().ifPresent(sortedTasksAndSubTasks::remove);
    }

    private void checkCollisionAndPutInSortedSetForCreate(Task taskToCreate) {
        if (taskToCreate.getStartTime() == null)
            return;
        sortedTasksAndSubTasks.add(taskToCreate);
        if (checkTimeCollision(taskToCreate)) {
            sortedTasksAndSubTasks.remove(taskToCreate);
            throw new TimeCollisionException("Time collision on create!!!");
        }
    }

    private void checkCollisionAndPutInSortedSetForUpdate(Task taskToCreate) {
        if (sortedTasksAndSubTasks.contains(taskToCreate)) {
            checkCollisionAndDoTransactionalLogicOnEqualTime(taskToCreate);
            return;
        }
        if (sortedTasksAndSubTasks.stream().anyMatch(x -> x.equals(taskToCreate))) {
            checkCollisionAndDoTransactionalLogicOnEqualTaskId(taskToCreate);
            return;
        }
        sortedTasksAndSubTasks.add(taskToCreate);
        if (checkTimeCollision(taskToCreate)) {
            sortedTasksAndSubTasks.remove(taskToCreate);
            throw new TimeCollisionException("Time collision on update!!!");
        }
    }

    private void checkCollisionAndDoTransactionalLogicOnEqualTaskId(Task taskToCreate) {
        Task previousTask = sortedTasksAndSubTasks.stream()
                .filter(x -> x.equals(taskToCreate))
                .findFirst()
                .orElse(null);
        sortedTasksAndSubTasks.remove(previousTask);
        sortedTasksAndSubTasks.add(taskToCreate);
        if (checkTimeCollision(taskToCreate)) {
            sortedTasksAndSubTasks.remove(taskToCreate);
            sortedTasksAndSubTasks.add(previousTask);
            throw new TimeCollisionException("Time collision on update!!!");
        }
    }

    private void checkCollisionAndDoTransactionalLogicOnEqualTime(Task taskToCreate) {
        Task previousTask = sortedTasksAndSubTasks.stream()
                .filter(x -> x.getStartTime().equals(taskToCreate.getStartTime()))
                .findFirst()
                .orElse(null);
        sortedTasksAndSubTasks.add(taskToCreate);
        if (checkTimeCollision(taskToCreate)) {
            sortedTasksAndSubTasks.add(previousTask);
            throw new TimeCollisionException("Time collision on update (equal start time)!!!");
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
}
