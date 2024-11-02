package ru.practicum.kanban.servers.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.kanban.interfaces.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected final TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    protected void sendText(HttpExchange h, String text, int responseCode) {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        try {
            h.sendResponseHeaders(responseCode, resp.length);
            if (!text.isEmpty()) {
                h.getResponseBody().write(resp);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        h.close();
    }
}