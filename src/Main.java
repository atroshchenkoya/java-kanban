import interfaces.TaskManager;
import entity.SubTask;
import entity.Task;
import entity.Epic;
import entity.TaskStatus;
import managers.FileBackedTaskManager;

import java.util.List;


public class Main {
    public static void main(String[] args) {

        String fileLocation = "C:\\test\\test.csv";
        TaskManager inMemoryTaskManager = new FileBackedTaskManager(fileLocation);

//        TaskManager inMemoryTaskManager = Managers.getDefault();
        inMemoryTaskManager.getAllTask();

        System.out.println("Поехали!");

        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 0
        Task task2 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 1
        Epic epic1 = new Epic(2, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 2
        SubTask subtask1 = new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, 2); // 3
        SubTask subtask2 = new SubTask(0, "Pop", "Pop", TaskStatus.DONE, 2); // 4
        Epic epic2 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS); // 5
        SubTask subtask3 = new SubTask(0, "Pop", "Pop", TaskStatus.NEW, 5); // 6
        inMemoryTaskManager.createTask(task1);
        inMemoryTaskManager.createTask(task2);
        inMemoryTaskManager.createEpic(epic1);
        inMemoryTaskManager.createSubTask(subtask1);
        inMemoryTaskManager.createSubTask(subtask2);
        inMemoryTaskManager.createEpic(epic2);
        inMemoryTaskManager.createSubTask(subtask3);
        Task task3 = new Task(0, "Popaaa", "Popaaa", TaskStatus.DONE);
        SubTask subTask4 = new SubTask(3, "Popdd", "Popdd", TaskStatus.DONE, 12);

        List<SubTask> epicList = inMemoryTaskManager.getAllSubTask(epic1);

        inMemoryTaskManager.updateTask(task3);
        inMemoryTaskManager.updateSubTask(subTask4);

        Epic epic3 = new Epic(5, "Poeep", "Peeeop", TaskStatus.IN_PROGRESS);
        inMemoryTaskManager.updateEpic(epic3);

        //inMemoryTaskManager.removeSubTask(3);
        //inMemoryTaskManager.removeSubTask(4);
        //inMemoryTaskManager.removeTask(0);

        //inMemoryTaskManager.removeEpic(2);
        //inMemoryTaskManager.removeEpic(5);

        inMemoryTaskManager.getAllTask();

        inMemoryTaskManager.getAllEpic();
        inMemoryTaskManager.getAllSubTask();
        inMemoryTaskManager.getSubTask(3);
        //inMemoryTaskManager.getEpic(6);
        inMemoryTaskManager.getEpic(2);
        inMemoryTaskManager.getEpic(5);
        inMemoryTaskManager.getTask(0);
        inMemoryTaskManager.getEpic(5);
        List<Task> t = inMemoryTaskManager.getHistory();
        inMemoryTaskManager.deleteAllEpic();
        inMemoryTaskManager.deleteAllTask();
        inMemoryTaskManager.deleteAllSubTask();

    }
}
