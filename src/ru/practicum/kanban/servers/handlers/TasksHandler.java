package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.exceptions.TimeCollisionException;
import ru.practicum.kanban.exceptions.not_found_exceptions.TaskNotFoundException;
import ru.practicum.kanban.interfaces.TaskManager;

import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) {
        EndPoint endpoint = getEndPoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS:
                handleGetTasks(exchange);
                break;
            case GET_TASK_BY_ID:
                handleGetTaskById(exchange);
                break;
            case CREATE_TASK:
                handleCreateTask(exchange);
                break;
            case UPDATE_TASK:
                handleUpdateTask(exchange);
                break;
            case DELETE_TASK:
                handleDeleteTask(exchange);
                break;
            default: sendText(exchange, "This endpoint is not supported.", 404);
        }
    }

    private void handleGetTasks(HttpExchange exchange) {
        List<Task> taskList = taskManager.getAllTask();

        Gson gson = getPreparedGson();
        String json = gson.toJson(taskList);
        sendText(exchange, json, 200);
    }

    private void handleDeleteTask(HttpExchange exchange) {
        int taskId = getTaskIdFromPath(exchange);
        try {
            taskManager.removeTask(taskId);
        } catch (TaskNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        sendText(exchange, "Task successfully deleted.", 200);
    }

    private void handleUpdateTask(HttpExchange exchange) {
        int taskId = getTaskIdFromPath(exchange);
        Gson gson = getPreparedGson();

        String requestBody = getRequestBodyAsString(exchange);
        Task task = gson.fromJson(requestBody, Task.class);
        Task taskToUpdate = new Task(
                taskId,
                task.getName(),
                task.getDescription(),
                task.getTaskStatus(),
                task.getStartTime(),
                task.getDuration()
        );
        try {
            taskManager.updateTask(taskToUpdate);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 406);
            return;
        } catch (TaskNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        sendText(exchange, "Task successfully updated.", 201);
    }

    private void handleCreateTask(HttpExchange exchange) {
        String requestBody = getRequestBodyAsString(exchange);
        Gson gson = getPreparedGson();

        Task task = gson.fromJson(requestBody, Task.class);
        try {
            taskManager.createTask(task);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 406);
            return;
        }
        sendText(exchange, "", 201);
    }

    private void handleGetTaskById(HttpExchange exchange) {
        int taskId = getTaskIdFromPath(exchange);
        Gson gson = getPreparedGson();
        Task task;
        try {
            task = taskManager.getTask(taskId);
        } catch (TaskNotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
            return;
        }
        String json = gson.toJson(task);
        sendText(exchange, json, 200);
    }

    private int getTaskIdFromPath(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        return Integer.parseInt(pathParts[pathParts.length - 1]);
    }

    private EndPoint getEndPoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (requestMethod.equals("GET") && pathParts.length == 2) {
            return EndPoint.GET_TASKS;
        }
        if (requestMethod.equals("GET") && pathParts.length == 3)  {
            return EndPoint.GET_TASK_BY_ID;
        }
        if (requestMethod.equals("POST") && pathParts.length == 2) {
            return EndPoint.CREATE_TASK;
        }
        if (requestMethod.equals("POST") && pathParts.length == 3)  {
            return EndPoint.UPDATE_TASK;
        }
        if (requestMethod.equals("DELETE") && pathParts.length == 3)  {
            return EndPoint.DELETE_TASK;
        }
        return EndPoint.UNKNOWN;
    }
}
