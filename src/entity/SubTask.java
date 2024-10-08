package entity;

public class SubTask extends Task {

    private final int linkedEpicId;

    public SubTask(int id, String name, String description, TaskStatus taskStatus, int linkedEpicId) {
        super(id, name, description, taskStatus);
        this.linkedEpicId = linkedEpicId;
    }

    public int getLinkedEpicId() {
        return linkedEpicId;
    }

}
