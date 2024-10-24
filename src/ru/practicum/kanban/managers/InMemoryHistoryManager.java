package ru.practicum.kanban.managers;

import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.entity.util.CustomNode;
import ru.practicum.kanban.interfaces.HistoryManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Task, CustomNode> historyMap = new HashMap<>();
    private final CustomLinkedList historyList = new CustomLinkedList();

    @Override
    public void addTask(Task task) {
        historyMap.computeIfPresent(task, (key, val) -> {
            historyList.remove(task);
            return historyList.linkLast(task);
        });
        historyMap.computeIfAbsent(task, k -> historyList.linkLast(task));
    }

    @Override
    public List<Task> getHistory() {

        List<Task> viewedTasks = new ArrayList<>();
        CustomNode node = historyList.getHead();

        while (node != null) {
            viewedTasks.add(node.getTask());
            node = node.getNextNode();
        }

        return viewedTasks;
    }

    @Override
    public void removeTask(Task task) {
        if (historyMap.containsKey(task)) {
            historyList.remove(task);
            historyMap.remove(task);
        }
    }

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

        public void remove(Task task) {

            CustomNode node = historyMap.get(task);

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
}
