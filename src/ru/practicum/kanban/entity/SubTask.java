package ru.practicum.kanban.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SubTask subTask)) {
            return false;
        }
        if (this.getClass() != o.getClass())
            return false;
        return this.getId() == subTask.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
