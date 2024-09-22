package managers;

import entity.util.CustomLinkedList;
import entity.util.CustomNode;
import entity.Task;
import interfaces.HistoryManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Task, CustomNode> historyMap = new HashMap<>();
    private final CustomLinkedList historyList = new CustomLinkedList();

    @Override
    public void addTask(Task task) {
        if (historyMap.containsKey(task)) {
            historyList.remove(historyMap.get(task));
            historyMap.put(task, historyList.linkLast(task));
            return;
        }
        historyMap.put(task, historyList.linkLast(task));
    }

    @Override
    public List<Task> getHistory() {

        if (historyList.getHead() == null) {
            return new ArrayList<>();
        }

        List<Task> viewedTasks = new ArrayList<>();
        CustomNode node = historyList.getHead();

        while (true) {
            viewedTasks.add(node.getTask());
            if (node.getNextNode() == null)
                break;
            node = node.getNextNode();
        }
        return viewedTasks;
    }
}
