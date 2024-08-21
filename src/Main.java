import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");
        TrackerManager trackerManager = new TrackerManager();

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.

        Task task1 = new Task(0, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task task2 = new Task(1, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task subtask1 = new SubTask(2, "Pop", "Pop", TaskStatus.IN_PROGRESS);
        Task subtask2 = new SubTask(3, "Pop", "Pop", TaskStatus.DONE);
        ArrayList<Integer> subsId = new ArrayList<>();
        subsId.add(2);
        subsId.add(3);
        Epic epic1 = new Epic(4, "Pop", "Pop", TaskStatus.IN_PROGRESS, subsId);
        trackerManager.addTaskInTaskStorage(task1);
        trackerManager.addTaskInTaskStorage(task2);
        trackerManager.addTaskInTaskStorage(subtask1);
        trackerManager.addTaskInTaskStorage(subtask2);
        trackerManager.addTaskInTaskStorage(epic1);
        Task subtask3 = new SubTask(5, "Pop", "Pop", TaskStatus.NEW);
        subsId = new ArrayList<>();
        subsId.add(5);
        Epic epic2 = new Epic(6, "Pop", "Pop", TaskStatus.IN_PROGRESS, subsId);
        trackerManager.addTaskInTaskStorage(subtask3);
        trackerManager.addTaskInTaskStorage(epic2);
        System.out.println(trackerManager.getTaskStorage());

        trackerManager.updateTaskInTaskStorage(new SubTask(2, "Pop", "Pop", TaskStatus.DONE));

        trackerManager.removeTaskFromTaskStorageById(2);

        HashMap<Integer, SubTask> pop = trackerManager.getAllSubTusksForEpic(epic1);
        HashMap<Integer, SubTask> pop2 = trackerManager.getAllSubTusksForEpic(epic2);

        int d = 4;

    }
}
