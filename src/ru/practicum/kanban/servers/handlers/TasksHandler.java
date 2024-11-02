package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.exceptions.TimeCollisionException;
import ru.practicum.kanban.interfaces.TaskManager;
import ru.practicum.kanban.servers.handlers.adapters.DurationAdapter;
import ru.practicum.kanban.servers.handlers.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
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
                handleGetTasksById(exchange);
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
            default: 
                sendText(exchange, "This endpoint is not supported.", 400);
        }
        
    }

    private void handleGetTasks(HttpExchange exchange) {
        List<Task> taskList = taskManager.getAllTask();

        Gson gson = getPreparedGson();
        String json = gson.toJson(taskList);
        System.out.println(json);
        sendText(exchange, json, 200);
    }

    private void handleDeleteTask(HttpExchange exchange) {
        Integer taskId = getIdIfTaskExists(exchange);
        if (taskId == null)
            return;
        taskManager.removeTask(taskId);
        sendText(exchange, "Task successfully deleted.", 200);
    }

    private void handleUpdateTask(HttpExchange exchange) {
        Integer taskId = getIdIfTaskExists(exchange);
        if (taskId == null)
            return;
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
            sendText(exchange, e.getMessage(), 400);
        }
        sendText(exchange, "Task successfully updated.", 201);
    }

    private void handleCreateTask(HttpExchange exchange) {
        String requestBody = getRequestBodyAsString(exchange);
        Gson gson = getPreparedGson();

        Task task = gson.fromJson(requestBody, Task.class);
        if (taskManager.getTask(task.getId()) != null) {
            sendText(exchange, "Task with same id already exists! Try another ID", 400);
            return;
        }
        try {
            taskManager.createTask(task);
        } catch (TimeCollisionException e) {
            sendText(exchange, e.getMessage(), 400);
        }

        sendText(exchange, "", 201);
    }

    private static String getRequestBodyAsString(HttpExchange exchange) {
        String requestBody;
        try {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return requestBody;
    }

    private void handleGetTasksById(HttpExchange exchange) {
        Integer taskId = getIdIfTaskExists(exchange);
        if (taskId == null)
            return;
        Gson gson = getPreparedGson();

        String json = gson.toJson(taskManager.getTask(taskId));
        sendText(exchange, json, 200);

    }

    private static Gson getPreparedGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        return gsonBuilder.create();
    }

    private Integer getIdIfTaskExists(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        String[] pathParts = requestPath.split("/");
        int taskId = Integer.parseInt(pathParts[pathParts.length - 1]);
        if (taskManager.getTask(taskId) == null) {
            sendText(exchange, "Task not found.", 400);
            return null;
        }
        return taskId;
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
