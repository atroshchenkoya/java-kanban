package tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subTasksId;

    public List<Integer> getSubTasksId() {
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
