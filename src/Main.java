import managers.TrackerManager;
import tasks.SubTask;
import tasks.Task;
import tasks.Epic;
import tasks.TaskStatus;

import java.util.List;


public class Main {
    public static void main(String[] args) {

        TrackerManager trackerManager = new TrackerManager();

        System.out.println("Поехали!");

        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 0
        Task task2 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 1
        Epic epic1 = new Epic(2, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 2
        SubTask subtask1 = new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, 2); // 3
        SubTask subtask2 = new SubTask(0, "Pop", "Pop", TaskStatus.DONE, 2); // 4
        Epic epic2 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 5
        SubTask subtask3 = new SubTask(0, "Pop", "Pop", TaskStatus.NEW, 5); // 6
        trackerManager.createTaskInTaskStorage(task1);
        trackerManager.createTaskInTaskStorage(task2);
        trackerManager.createEpicInEpicStorage(epic1);
        trackerManager.createSubTaskInSubTaskStorage(subtask1);
        trackerManager.createSubTaskInSubTaskStorage(subtask2);
        trackerManager.createEpicInEpicStorage(epic2);
        trackerManager.createSubTaskInSubTaskStorage(subtask3);
        Task task3 = new Task(0, "Popaaa", "Popaaa", TaskStatus.DONE);
        SubTask subTask4 = new SubTask(3, "Popdd", "Popdd", TaskStatus.DONE, 12);

        List<SubTask> epicList = trackerManager.getAllSubTusksForEpicByEpic(epic1);

        trackerManager.updateTaskInTaskStorage(task3);
        trackerManager.updateSubTaskInSubTaskStorage(subTask4);

        Epic epic3 = new Epic(5, "Poeep", "Peeeop", TaskStatus.IN_PROGRESS);
        trackerManager.updateEpicInEpicStorage(epic3);

        trackerManager.removeSubTaskFromSubTaskStorageById(3);
        trackerManager.removeSubTaskFromSubTaskStorageById(4);
        trackerManager.removeTaskFromTaskStorageById(0);

        trackerManager.removeEpicFromEpicStorageById(2);
        trackerManager.removeEpicFromEpicStorageById(5);

        trackerManager.getTaskStorage();
        trackerManager.getEpicStorage();
        trackerManager.getSubTaskStorage();
        trackerManager.getSubTaskById(3);
        trackerManager.getEpicById(6);
        trackerManager.getTaskById(0);
        trackerManager.clearEpicStorage();
        trackerManager.clearTaskStorage();
        trackerManager.clearSubTuskStorage();

    }
}
