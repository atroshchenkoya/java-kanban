package managers;

import ru.practicum.kanban.entity.Epic;
import ru.practicum.kanban.entity.SubTask;
import ru.practicum.kanban.entity.Task;
import ru.practicum.kanban.entity.TaskStatus;
import ru.practicum.kanban.exceptions.ManagerLoadFromFileException;
import ru.practicum.kanban.exceptions.ManagerSaveToFileException;
import ru.practicum.kanban.interfaces.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.kanban.managers.FileBackedTaskManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class FileBackedTaskManagerTest {

    static TaskManager fileBackedTaskManager;

    static File tmpFile;

    static String fileCanonicalPath;

    @BeforeEach
    void beforeEach() {
        try {
            //tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp", new File("C:\\test\\"));
            tmpFile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp").toFile();
            fileCanonicalPath = tmpFile.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileBackedTaskManager = new FileBackedTaskManager(fileCanonicalPath);
    }

    @AfterEach
    void afterEach() {
        if (!tmpFile.delete()) {
            throw new ManagerLoadFromFileException(new Throwable("Something goes wrong"));
        }
    }

    @Test
    void createNewFileAndSaveEmptyManagerShouldBeOk() {
        Task task1 = new Task(1, "Pop", "Pop", TaskStatus.IN_PROGRESS);

        fileBackedTaskManager.createTask(task1);
        fileBackedTaskManager.removeTask(task1.getId());

        String firstRowFromFile = "";
        String lastRowFromFile = "";
        try (InputStream inputStream = new FileInputStream(fileCanonicalPath);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            while (reader.ready()) {
                String currentRow = reader.readLine();
                if (firstRowFromFile.isEmpty())
                    firstRowFromFile = currentRow;
                else
                    lastRowFromFile = currentRow;
            }
        } catch (IOException e) {
            throw new ManagerLoadFromFileException(e);
        }
        Assertions.assertEquals("id,type,name,status,description,epic,startOn,duration", firstRowFromFile);
        Assertions.assertEquals("", lastRowFromFile);
    }

    @Test
    void loadFromEmptyFileShouldBeOk() {
        fileBackedTaskManager = new FileBackedTaskManager(fileCanonicalPath);

        Assertions.assertTrue(fileBackedTaskManager.getAllTask().isEmpty());
        Assertions.assertTrue(fileBackedTaskManager.getAllSubTask().isEmpty());
        Assertions.assertTrue(fileBackedTaskManager.getAllEpic().isEmpty());
    }

    @Test
    void loadTasksFromFileShouldBeOkWithTimeElementsInFile() {
        try (OutputStream outputStream = new FileOutputStream(fileCanonicalPath);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {
            writer.write("id,type,name,status,description,epic,startOn,duration");
            writer.newLine();
            List<String> lines = List.of(
                    "id,type,name,status,description,epic,startOn,duration",
                    "1,TASK,Task1,NEW,Description task1,,2024-10-18T15:00,PT1H10M",
                    "3,SUBTASK,Sub Task2,DONE,Description sub task3,2,2024-10-19T13:20,PT40M",
                    "2,EPIC,Epic2,NEW,Description epic2");
            for (String line: lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveToFileException(e);
        }

        fileBackedTaskManager = new FileBackedTaskManager(fileCanonicalPath);

        Assertions.assertEquals(LocalDateTime.parse("2024-10-18T15:00") ,fileBackedTaskManager.getTask(1).getStartTime());
        Assertions.assertEquals(LocalDateTime.parse("2024-10-19T13:20") ,fileBackedTaskManager.getSubTask(3).getStartTime());
        Assertions.assertEquals("DONE", fileBackedTaskManager.getEpic(2).getTaskStatus().toString());
        Assertions.assertEquals("Description task1" ,fileBackedTaskManager.getTask(1).getDescription());
        Assertions.assertEquals("Description epic2" ,fileBackedTaskManager.getEpic(2).getDescription());
        Assertions.assertEquals("Description sub task3" ,fileBackedTaskManager
                .getSubTask(3).getDescription());
        Assertions.assertEquals(2, fileBackedTaskManager.getSubTask(3).getLinkedEpicId());
        Assertions.assertEquals("Epic2", fileBackedTaskManager.getEpic(2).getName());
    }

    @Test
    void loadTasksToFileShouldBeOk() {
        Task task1 = new Task(1, "Task1", "Description task1", TaskStatus.NEW);
        Epic epic2 = new Epic(2, "Epic2", "Description epic2", TaskStatus.DONE);
        SubTask subTask3 = new SubTask(3, "Sub Task2", "Description sub task3", TaskStatus.DONE, 2);
        List<String> expectedLines = List.of(
                "id,type,name,status,description,epic,startOn,duration",
                "1,TASK,Task1,NEW,Description task1,,,",
                "3,SUBTASK,Sub Task2,DONE,Description sub task3,2,,",
                "2,EPIC,Epic2,DONE,Description epic2");

        fileBackedTaskManager.createTask(task1);
        fileBackedTaskManager.createEpic(epic2);
        fileBackedTaskManager.createSubTask(subTask3);

        List<String> actualLines;
        try {
            actualLines = Files.readAllLines(Paths.get(fileCanonicalPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(expectedLines, actualLines);
    }

    @Test
    void loadTasksToFileWithTimeElementsShouldBeOk() {
        Task task1 = new Task(1, "Task1", "Description task1",
                TaskStatus.NEW, LocalDateTime.parse("2024-10-18T15:00"), Duration.parse("PT1H10M"));
        Epic epic2 = new Epic(2, "Epic2", "Description epic2", TaskStatus.DONE);
        SubTask subTask3 = new SubTask(3, "Sub Task2", "Description sub task3",
                TaskStatus.DONE, 2, LocalDateTime.parse("2024-10-19T13:20"),
                Duration.parse("PT40M"));
        List<String> expectedLines = List.of(
                "id,type,name,status,description,epic,startOn,duration",
                "1,TASK,Task1,NEW,Description task1,,2024-10-18T15:00,PT1H10M",
                "3,SUBTASK,Sub Task2,DONE,Description sub task3,2,2024-10-19T13:20,PT40M",
                "2,EPIC,Epic2,DONE,Description epic2");

        fileBackedTaskManager.createTask(task1);
        fileBackedTaskManager.createEpic(epic2);
        fileBackedTaskManager.createSubTask(subTask3);

        List<String> actualLines;
        try {
            actualLines = Files.readAllLines(Paths.get(fileCanonicalPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(expectedLines, actualLines);
    }
}
