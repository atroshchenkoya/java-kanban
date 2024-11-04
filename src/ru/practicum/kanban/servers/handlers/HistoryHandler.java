package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.interfaces.TaskManager;

import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        EndPoint endpoint = getEndPoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());
        if (endpoint == EndPoint.GET_HISTORY)
            handleGetHistory(exchange);
        else
            sendText(exchange, "This endpoint is not supported.", 404);
    }

    private void handleGetHistory(HttpExchange exchange) {
        List<Task> taskHistoryList = taskManager.getHistory();
        Gson gson = getPreparedGson();
        String json = gson.toJson(taskHistoryList);
        sendText(exchange, json, 200);
    }

    private EndPoint getEndPoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (requestMethod.equals("GET") && pathParts.length == 2) {
            return EndPoint.GET_HISTORY;
        }
        return EndPoint.UNKNOWN;
    }
}
