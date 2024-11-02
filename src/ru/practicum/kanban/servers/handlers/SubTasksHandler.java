package ru.practicum.kanban.servers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.interfaces.TaskManager;


public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {

    public SubTasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {

    }

    private void handleDeleteSubTask(HttpExchange exchange) {
    }

    private void handleUpdateSubTask(HttpExchange exchange) {
    }

    private void handleCreateSubTask(HttpExchange exchange) {
    }

    private void handleGetSubTaskById(HttpExchange exchange) {
    }

    private void handleGetSubTasks(HttpExchange exchange) {
    }

    private EndPoint getEndPoint(String path, String requestMethod) {
        return null;
    }
}
