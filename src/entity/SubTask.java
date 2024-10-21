package entity;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private final int linkedEpicId;

    public SubTask(int id, String name, String description, TaskStatus taskStatus, int linkedEpicId) {
        super(id, name, description, taskStatus);
        this.linkedEpicId = linkedEpicId;
    }

    public SubTask(int id, String name, String description, TaskStatus taskStatus, int linkedEpicId,
                   LocalDateTime startTime, Duration duration) {
        super(id, name, description, taskStatus, startTime, duration);
        this.linkedEpicId = linkedEpicId;
    }

    public int getLinkedEpicId() {
        return linkedEpicId;
    }

}
