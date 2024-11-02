package ru.practicum.kanban.servers.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.kanban.exceptions.HttpExchangeException;
import ru.practicum.kanban.interfaces.TaskManager;
import ru.practicum.kanban.servers.handlers.adapters.DurationAdapter;
import ru.practicum.kanban.servers.handlers.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

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
            throw new HttpExchangeException(e);
        }
        h.close();
    }

    protected static Gson getPreparedGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Duration.class, new DurationAdapter());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        return gsonBuilder.create();
    }

    protected String getRequestBodyAsString(HttpExchange exchange) {
        String requestBody;
        try {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new HttpExchangeException(e);
        }
        return requestBody;
    }

}