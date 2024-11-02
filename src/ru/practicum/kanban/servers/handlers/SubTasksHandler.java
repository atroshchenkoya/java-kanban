package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.exceptions.TimeCollisionException;
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
                sendText(exchange, "This endpoint is not supported.", 400);
        }

    }

    private void handleDeleteSubTask(HttpExchange exchange) {
        Integer subTaskId = getIdIfSubTaskExists(exchange);
        if (subTaskId == null)
            return;
        taskManager.removeSubTask(subTaskId);
        sendText(exchange, "SubTask successfully deleted.", 200);
    }

    private Integer getIdIfSubTaskExists(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        int subTaskId = Integer.parseInt(pathParts[pathParts.length - 1]);
        if (taskManager.getSubTask(subTaskId) == null) {
            sendText(exchange, "Task not found.", 400);
            return null;
        }
        return subTaskId;
    }

    private void handleUpdateSubTask(HttpExchange exchange) {
        Integer subTaskId = getIdIfSubTaskExists(exchange);
        if (subTaskId == null)
            return;
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
        Integer subTaskId = getIdIfSubTaskExists(exchange);
        if (subTaskId == null)
            return;
        Gson gson = getPreparedGson();

        String json = gson.toJson(taskManager.getSubTask(subTaskId));
        sendText(exchange, json, 200);
    }

    private void handleGetSubTasks(HttpExchange exchange) {
        List<SubTask> subTaskList = taskManager.getAllSubTask();

        Gson gson = getPreparedGson();
        String json = gson.toJson(subTaskList);
        sendText(exchange, json, 200);
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