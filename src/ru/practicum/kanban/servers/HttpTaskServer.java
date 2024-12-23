package ru.practicum.kanban.servers;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.kanban.interfaces.TaskManager;
import ru.practicum.kanban.managers.Managers;
import ru.practicum.kanban.servers.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8090;
    HttpServer httpServer;

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer();
        server.start(taskManager);
    }

    public void start(TaskManager taskManager) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        createServerContext(taskManager);
        httpServer.start();
    }

    private void createServerContext(TaskManager taskManager) {
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void stop() {
        httpServer.stop(0);
    }
}
