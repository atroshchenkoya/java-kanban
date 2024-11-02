package ru.practicum.kanban.servers.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.interfaces.TaskManager;

import java.io.IOException;

public class SubTasksHandler implements HttpHandler {

    public SubTasksHandler(TaskManager taskManager) {
    }

    @Override
    public void handle(HttpExchange exchange) {

    }
}
