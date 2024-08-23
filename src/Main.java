import managers.TrackerManager;
import tasks.SubTask;
import tasks.Task;
import tasks.Epic;
import tasks.TaskStatus;

import java.util.List;


public class Main {
    public static void main(String[] args) {

        TrackerManager trackerManager = new TrackerManager();
        trackerManager.getAllTask();

        System.out.println("Поехали!");

        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 0
        Task task2 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 1
        Epic epic1 = new Epic(2, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 2
        SubTask subtask1 = new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, 2); // 3
        SubTask subtask2 = new SubTask(0, "Pop", "Pop", TaskStatus.DONE, 2); // 4
        Epic epic2 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 5
        SubTask subtask3 = new SubTask(0, "Pop", "Pop", TaskStatus.NEW, 5); // 6
        trackerManager.createTask(task1);
        trackerManager.createTask(task2);
        trackerManager.createEpic(epic1);
        trackerManager.createSubTask(subtask1);
        trackerManager.createSubTask(subtask2);
        trackerManager.createEpic(epic2);
        trackerManager.createSubTask(subtask3);
        Task task3 = new Task(0, "Popaaa", "Popaaa", TaskStatus.DONE);
        SubTask subTask4 = new SubTask(3, "Popdd", "Popdd", TaskStatus.DONE, 12);

        List<SubTask> epicList = trackerManager.getAllSubTask(epic1);

        trackerManager.updateTask(task3);
        trackerManager.updateSubTask(subTask4);

        Epic epic3 = new Epic(5, "Poeep", "Peeeop", TaskStatus.IN_PROGRESS);
        trackerManager.updateEpic(epic3);

        trackerManager.removeSubTask(3);
        trackerManager.removeSubTask(4);
        trackerManager.removeTask(0);

        trackerManager.removeEpic(2);
        trackerManager.removeEpic(5);

        trackerManager.getAllTask();
        trackerManager.getAllEpic();
        trackerManager.getAllSubTask();
        trackerManager.getSubTask(3);
        trackerManager.getEpic(6);
        trackerManager.getTask(0);
        trackerManager.deleteAllEpic();
        trackerManager.deleteAllTask();
        trackerManager.deleteAllSubTask();

    }
}
