package integration;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.entity.TaskStatus;
import ru.practicum.kanban.interfaces.TaskManager;
import ru.practicum.kanban.managers.InMemoryTaskManager;
import ru.practicum.kanban.servers.HttpTaskServer;
import ru.practicum.kanban.servers.handlers.BaseHttpHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TasksHandlerTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer();
    Gson gson = BaseHttpHandler.getPreparedGson();

    @BeforeEach
    public void setUp() throws IOException {
        manager.deleteAllTask();
        manager.deleteAllEpic();
        manager.deleteAllSubTask();
        taskServer.start(manager);
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    void correctAddTaskShouldReturn201() throws IOException, InterruptedException {
        Task task = new Task(0, "Test 2", "Testing task 2",
                TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response;
        try (client) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        client.close();
        assertEquals(201, response.statusCode());
    }
}
