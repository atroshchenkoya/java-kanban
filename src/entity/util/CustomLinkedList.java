package entity.util;

import entity.Task;

public class CustomLinkedList {
    CustomNode head;
    CustomNode last;

    public CustomNode linkLast(Task task) {

        CustomNode node = new CustomNode(task);

        if (head == null) {
            this.head = node;
            this.last = node;
            return node;
        }

        last.setNextNode(node);
        node.setPrevNode(last);
        this.last = node;
        return node;
    }

    public void remove(CustomNode node) {
        if (head == last && head == node) {
            head = null;
            last = null;
            return;
        }
        if (head == node) {
            head = head.getNextNode();
            head.setPrevNode(null);
            return;
        }
        if (last == node) {
            last = last.getPrevNode();
            last.setNextNode(null);
            return;
        }
        node.getPrevNode().setNextNode(node.getNextNode());
        node.getNextNode().setPrevNode(node.getPrevNode());
    }

    public CustomNode getHead() {
        return head;
    }
}