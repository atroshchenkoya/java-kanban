import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("Поехали!");
        ArrayList<SubTask> subTasks = new ArrayList<>();
        subTasks.add(new SubTask(0, "Pop", "Pop", TaskStatus.IN_PROGRESS));
        subTasks.add(new SubTask(1, "Pop", "Pop", TaskStatus.IN_PROGRESS));
        subTasks.add(new SubTask(2, "Pop", "Pop", TaskStatus.IN_PROGRESS));

        Epic epic = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, null);

        subTasks.add(new SubTask(2, "Pop", "Pop", TaskStatus.IN_PROGRESS));
        subTasks.add(new SubTask(2, "Pop", "Pop", TaskStatus.IN_PROGRESS));
        Epic epic2 = new Epic(0, "Pop", "Pop", TaskStatus.IN_PROGRESS, null);
        return;

    }
}
