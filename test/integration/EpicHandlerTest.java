package integration;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.entity.Epic;
import ru.practicum.kanban.entity.SubTask;
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

class EpicHandlerTest {
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
    void epicAddShouldReturn201() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Epic1", "Testing epic 1",
                TaskStatus.NEW);
        String epicJson = gson.toJson(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8090/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> response;

        try (client) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        assertEquals(201, response.statusCode());
    }

    @Test
    void epicHasCorrectTimeIfLinkedSubtaskUpdated() throws IOException, InterruptedException {
        Epic epic = new Epic(0, "Epic1", "Testing epic 1", TaskStatus.NEW);
        SubTask subTask1 = new SubTask(1, "St1", "dSt1", TaskStatus.NEW, 0);
        SubTask subTask2 = new SubTask(2, "St2", "dSt2", TaskStatus.NEW, 0, LocalDateTime.parse("2028-10-18T15:00"), Duration.parse("PT1H50M"));
        SubTask subTask1Update = new SubTask(1, "St1Update", "dSt1Update", TaskStatus.DONE, 0, LocalDateTime.parse("2026-10-18T15:00"), Duration.parse("PT12H50M"));
        String epicJson = gson.toJson(epic);
        String subTask1Json = gson.toJson(subTask1);
        String subTask2Json = gson.toJson(subTask2);
        String subTask1UpdateJson = gson.toJson(subTask1Update);
        HttpClient client = HttpClient.newHttpClient();
        URI epicCreateUrl = URI.create("http://localhost:8090/epics");
        URI subTaskCreateUrl = URI.create("http://localhost:8090/subtasks");
        URI subTask1UpdateUrl = URI.create("http://localhost:8090/subtasks/1");
        URI getEpic0Url = URI.create("http://localhost:8090/epics/0");
        HttpRequest epicAddRequest = HttpRequest.newBuilder()
                .uri(epicCreateUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        HttpResponse<String> epicAddResponse;
        HttpRequest subTask1AddRequest = HttpRequest.newBuilder()
                .uri(subTaskCreateUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1Json))
                .build();
        HttpResponse<String> subTask1CreateResponse;
        HttpRequest subTask2AddRequest = HttpRequest.newBuilder()
                .uri(subTaskCreateUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subTask2Json))
                .build();
        HttpResponse<String> subTask2CreateResponse;
        HttpRequest subTask1UpdateRequest = HttpRequest.newBuilder()
                .uri(subTask1UpdateUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1UpdateJson))
                .build();
        HttpResponse<String> subTask1UpdateResponse;
        HttpRequest getEpic0Request = HttpRequest.newBuilder()
                .uri(getEpic0Url)
                .GET()
                .build();
        HttpResponse<String> getEpic0Response;
        try (client) {
            epicAddResponse = client.send(epicAddRequest, HttpResponse.BodyHandlers.ofString());
            subTask1CreateResponse = client.send(subTask1AddRequest, HttpResponse.BodyHandlers.ofString());
            subTask2CreateResponse = client.send(subTask2AddRequest, HttpResponse.BodyHandlers.ofString());

            subTask1UpdateResponse = client.send(subTask1UpdateRequest, HttpResponse.BodyHandlers.ofString());

            getEpic0Response = client.send(getEpic0Request, HttpResponse.BodyHandlers.ofString());
        }
        Epic epic0FromResponse = gson.fromJson(getEpic0Response.body(), Epic.class);
        assertEquals(201, epicAddResponse.statusCode());
        assertEquals(201, subTask1CreateResponse.statusCode());
        assertEquals(201, subTask2CreateResponse.statusCode());
        assertEquals(201, subTask1UpdateResponse.statusCode());
        assertEquals(200, getEpic0Response.statusCode());
        assertEquals(LocalDateTime.parse("2026-10-18T15:00"), epic0FromResponse.getStartTime());
        assertEquals(TaskStatus.IN_PROGRESS, epic0FromResponse.getTaskStatus());
        assertEquals(subTask1Update.getDuration().plus(subTask2.getDuration()), epic0FromResponse.getDuration());
    }
}
