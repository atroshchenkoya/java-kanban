import java.util.ArrayList;

public class Epic extends Task {

    ArrayList<Integer> subTasksId;
    public Epic(int id, String name, String description, TaskStatus taskStatus, ArrayList<Integer> subTasksId) {
        super(id, name, description, taskStatus);

        if (subTasksId != null && !subTasksId.isEmpty())
            this.subTasksId = new ArrayList<>(subTasksId);
    }
}
