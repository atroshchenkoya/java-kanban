import java.util.ArrayList;
import java.util.Arrays;

public class Epic extends Task {

    ArrayList<Integer> subTasksId;
    public Epic(int id, String name, String description, TaskStatus taskStatus, ArrayList<Integer> subTasksId) {
        super(id, name, description, taskStatus);

        if (subTasksId != null && !subTasksId.isEmpty())
            this.subTasksId = (ArrayList<Integer>) subTasksId.clone();
    }
}
