package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.entity.Epic;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.exceptions.TimeCollisionException;
import ru.practicum.kanban.exceptions.not_found_exceptions.EpicNotFoundException;
import ru.practicum.kanban.interfaces.TaskManager;

import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        EndPoint endpoint = getEndPoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_EPICS:
                handleGetEpics(exchange);
                break;
            case GET_EPIC_BY_ID:
                handleGetEpicById(exchange);
                break;
            case CREATE_EPIC:
                handleCreateEpic(exchange);
                break;
            case UPDATE_EPIC:
                handleUpdateEpic(exchange);
                break;
            case DELETE_EPIC:
                handleDeleteEpic(exchange);
                break;
            case GET_SUBTASKS_BY_EPIC_ID:
                handleGetSubTasksByEpicId(exchange);
                break;
            default:
                sendText(exchange, "This endpoint is not supported.", 404);
        }
    }

    private void handleGetSubTasksByEpicId(HttpExchange exchange) {
        int epicId = getEpicIdFromPathForGetSubTasks(exchange);
        List<SubTask> subTasks;
        try {
            subTasks = taskManager.getAllSubTask(epicId);
        } catch (EpicNotFoundException e) {
            sendText(exchange, "No such epic exists", 404);
            return;
        }
        Gson gson = getPreparedGson();
        String json = gson.toJson(subTasks);
        sendText(exchange, json, 200);
    }

    private int getEpicIdFromPathForGetSubTasks(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        return Integer.parseInt(pathParts[pathParts.length - 2]);
    }

    private void handleDeleteEpic(HttpExchange exchange) {
        int epicId = getEpicIdFromPath(exchange);
        try {
            taskManager.removeEpic(epicId);
        } catch (EpicNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        sendText(exchange, "Epic successfully deleted.", 200);
    }

    private void handleUpdateEpic(HttpExchange exchange) {
        int epicId = getEpicIdFromPath(exchange);
        Gson gson = getPreparedGson();

        String requestBody = getRequestBodyAsString(exchange);
        Epic epic = gson.fromJson(requestBody, Epic.class);
        Epic epicToUpdate = new Epic(
                epicId,
                epic.getName(),
                epic.getDescription(),
                epic.getTaskStatus()
        );
        try {
            taskManager.updateEpic(epicToUpdate);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 406);
            return;
        } catch (EpicNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        sendText(exchange, "Epic successfully updated.", 201);
    }

    private void handleCreateEpic(HttpExchange exchange) {
        String requestBody = getRequestBodyAsString(exchange);
        Gson gson = getPreparedGson();

        Epic epic = gson.fromJson(requestBody, Epic.class);
        try {
            taskManager.createEpic(epic);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 406);
            return;
        }
        sendText(exchange, "", 201);
    }

    private void handleGetEpicById(HttpExchange exchange) {
        int epicId = getEpicIdFromPath(exchange);
        Gson gson = getPreparedGson();
        Epic epic;
        try {
            epic = taskManager.getEpic(epicId);
        } catch (EpicNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        String json = gson.toJson(epic);
        sendText(exchange, json, 200);
    }

    private void handleGetEpics(HttpExchange exchange) {
        List<Epic> epicList = taskManager.getAllEpic();
        Gson gson = getPreparedGson();
        String json = gson.toJson(epicList);
        sendText(exchange, json, 200);
    }

    private int getEpicIdFromPath(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        return Integer.parseInt(pathParts[pathParts.length - 1]);
    }

    private EndPoint getEndPoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (requestMethod.equals("GET") && pathParts.length == 2) {
            return EndPoint.GET_EPICS;
        }
        if (requestMethod.equals("GET") && pathParts.length == 3)  {
            return EndPoint.GET_EPIC_BY_ID;
        }
        if (requestMethod.equals("GET") && pathParts.length == 4)  {
            return EndPoint.GET_SUBTASKS_BY_EPIC_ID;
        }
        if (requestMethod.equals("POST") && pathParts.length == 2) {
            return EndPoint.CREATE_EPIC;
        }
        if (requestMethod.equals("POST") && pathParts.length == 3)  {
            return EndPoint.UPDATE_EPIC;
        }
        if (requestMethod.equals("DELETE") && pathParts.length == 3)  {
            return EndPoint.DELETE_EPIC;
        }
        return EndPoint.UNKNOWN;
    }
}
