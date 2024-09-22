package entity.util;

import entity.Task;

public class CustomNode {
    private final Task task;
    private CustomNode nextNode;
    private CustomNode prevNode;

    public CustomNode(Task task) {
        this.task = task;
    }

    public void setNextNode(CustomNode nextNode) {
        this.nextNode = nextNode;
    }

    public CustomNode getNextNode() {
        return nextNode;
    }

    public CustomNode getPrevNode() {
        return prevNode;
    }

    public void setPrevNode(CustomNode prevNode) {
        this.prevNode = prevNode;
    }

    public Task getTask() {
        return task;
    }
}
