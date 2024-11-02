package ru.practicum.kanban.servers;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.kanban.interfaces.TaskManager;
import ru.practicum.kanban.managers.Managers;
import ru.practicum.kanban.servers.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8090;
    private static TaskManager taskManager;

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        taskManager = Managers.getDefault();

        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        //httpServer.createContext("/history", new HistoryHandler(taskManager));
        //httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));

        httpServer.start(); // запускаем сервер
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }
}
