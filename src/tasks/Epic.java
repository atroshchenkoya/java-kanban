package tasks;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subTasksId;

    public ArrayList<Integer> getSubTasksId() {
        return subTasksId;
    }

    public void setSubTasksId(ArrayList<Integer> subTasksId) {
        this.subTasksId.clear();
        this.subTasksId.addAll(subTasksId);

    }

    public Epic(int id, String name, String description, TaskStatus taskStatus) {
        super(id, name, description, taskStatus);

            this.subTasksId = new ArrayList<>();
    }
}
