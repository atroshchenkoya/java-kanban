package entity;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> linkedSubTask;

    public List<Integer> getLinkedSubTask() {
        return linkedSubTask;
    }

    public void linkSubTask(List<Integer> subTasksId) {
        this.linkedSubTask.clear();
        this.linkedSubTask.addAll(subTasksId);
    }

    public Epic(int id, String name, String description, TaskStatus taskStatus) {
        super(id, name, description, taskStatus);
        this.linkedSubTask = new ArrayList<>();
    }

    public void unLinkAllSubTask() {
        this.linkedSubTask.clear();
    }

    public void linkSubTask(int subTaskId) {
        linkedSubTask.add(subTaskId);
    }

    public void unLinkSubTask(int subTaskId) {
        linkedSubTask.removeIf(x -> x == subTaskId);
    }
}
