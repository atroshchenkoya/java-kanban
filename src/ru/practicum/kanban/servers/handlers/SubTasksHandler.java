package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.exceptions.TimeCollisionException;
import ru.practicum.kanban.exceptions.not_found_exceptions.SubTaskNotFoundException;
import ru.practicum.kanban.interfaces.TaskManager;

import java.util.List;


public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {

    public SubTasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        EndPoint endpoint = getEndPoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_SUBTASKS:
                handleGetSubTasks(exchange);
                break;
            case GET_SUBTASK_BY_ID:
                handleGetSubTaskById(exchange);
                break;
            case CREATE_SUBTASK:
                handleCreateSubTask(exchange);
                break;
            case UPDATE_SUBTASK:
                handleUpdateSubTask(exchange);
                break;
            case DELETE_SUBTASK:
                handleDeleteSubTask(exchange);
                break;
            default:
                sendText(exchange, "This endpoint is not supported.", 404);
        }

    }

    private void handleDeleteSubTask(HttpExchange exchange) {
        int subTaskId = getSubTaskIdFromPath(exchange);
        try {
            taskManager.removeSubTask(subTaskId);
        } catch (SubTaskNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        sendText(exchange, "SubTask successfully deleted.", 200);
    }

    private void handleUpdateSubTask(HttpExchange exchange) {
        int subTaskId = getSubTaskIdFromPath(exchange);
        Gson gson = getPreparedGson();
        String requestBody = getRequestBodyAsString(exchange);
        SubTask subTask = gson.fromJson(requestBody, SubTask.class);
        SubTask subTaskToUpdate = new SubTask(
                subTaskId,
                subTask.getName(),
                subTask.getDescription(),
                subTask.getTaskStatus(),
                subTask.getLinkedEpicId(),
                subTask.getStartTime(),
                subTask.getDuration()
        );
        try {
            taskManager.updateSubTask(subTaskToUpdate);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 406);
            return;
        } catch (SubTaskNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        sendText(exchange, "SubTask successfully updated.", 201);
    }

    private void handleCreateSubTask(HttpExchange exchange) {
        String requestBody = getRequestBodyAsString(exchange);
        Gson gson = getPreparedGson();

        SubTask subTask = gson.fromJson(requestBody, SubTask.class);
        try {
            taskManager.createSubTask(subTask);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 406);
            return;
        }
        sendText(exchange, "", 201);
    }

    private void handleGetSubTaskById(HttpExchange exchange) {
        int subTaskId = getSubTaskIdFromPath(exchange);
        Gson gson = getPreparedGson();
        SubTask subTask;
        try {
            subTask = taskManager.getSubTask(subTaskId);
        } catch (SubTaskNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        String json = gson.toJson(subTask);
        sendText(exchange, json, 200);
    }

    private void handleGetSubTasks(HttpExchange exchange) {
        List<SubTask> subTaskList = taskManager.getAllSubTask();

        Gson gson = getPreparedGson();
        String json = gson.toJson(subTaskList);
        sendText(exchange, json, 200);
    }

    private int getSubTaskIdFromPath(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        return Integer.parseInt(pathParts[pathParts.length - 1]);
    }

    private EndPoint getEndPoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (requestMethod.equals("GET") && pathParts.length == 2) {
            return EndPoint.GET_SUBTASKS;
        }
        if (requestMethod.equals("GET") && pathParts.length == 3)  {
            return EndPoint.GET_SUBTASK_BY_ID;
        }
        if (requestMethod.equals("POST") && pathParts.length == 2) {
            return EndPoint.CREATE_SUBTASK;
        }
        if (requestMethod.equals("POST") && pathParts.length == 3)  {
            return EndPoint.UPDATE_SUBTASK;
        }
        if (requestMethod.equals("DELETE") && pathParts.length == 3)  {
            return EndPoint.DELETE_SUBTASK;
        }
        return EndPoint.UNKNOWN;
    }
}
