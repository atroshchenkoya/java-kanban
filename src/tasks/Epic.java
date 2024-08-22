package tasks;

import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Integer> subTasksId;

    public ArrayList<Integer> getSubTasksId() {
        return subTasksId;
    }

    public void setSubTasksId(ArrayList<Integer> subTasksId) {
        this.subTasksId = subTasksId;
    }

    public Epic(int id, String name, String description, TaskStatus taskStatus, ArrayList<Integer> subTasksId) {
        super(id, name, description, taskStatus);

        if (subTasksId != null && !subTasksId.isEmpty())
            this.subTasksId = new ArrayList<>(subTasksId);
    }
}
