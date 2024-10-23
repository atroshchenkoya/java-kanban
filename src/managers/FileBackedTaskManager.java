package managers;

import entity.Epic;
import entity.SubTask;
import entity.Task;
import entity.TaskStatus;
import entity.TaskType;
import exceptions.ManagerLoadFromFileException;
import exceptions.ManagerSaveToFileException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final String fileLocation;

    public FileBackedTaskManager(String fileLocation) {
        super();
        this.fileLocation = fileLocation;
        loadFromFile();
        setCounterAfterLoadingFromFile();
    }

    private void setCounterAfterLoadingFromFile() {
        this.idCounter = Stream.of(
                taskStorage.keySet().stream().max(Integer::compareTo).orElse(0),
                epicStorage.keySet().stream().max(Integer::compareTo).orElse(0),
                subTaskStorage.keySet().stream().max(Integer::compareTo).orElse(0)
        ).max(Integer::compareTo).orElse(0) + 1;
    }

    private void loadFromFile() {
        try (InputStream inputStream = new FileInputStream(fileLocation);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {
            while (reader.ready()) {
                String a = reader.readLine();
                if (a.startsWith("id"))
                    continue;
                String[] element = a.split(",");
                loadInMemory(element);
            }
            linkLoadedSubTasksToTheirEpics();
            setLoadedEpicStatusAndTime();
        } catch (IOException e) {
            throw new ManagerLoadFromFileException(e);
        }
    }

    private void setLoadedEpicStatusAndTime() {
        epicStorage.values().forEach(this::setEpicCalculableAttributes);
    }

    private void linkLoadedSubTasksToTheirEpics() {
        subTaskStorage.keySet()
                .forEach(id -> epicStorage.get(subTaskStorage.get(id).getLinkedEpicId()).linkSubTask(id));
    }

    private void loadInMemory(String[] element) {
        if (element[1].equals(TaskType.TASK.name())) {
            Task task = new Task(
                    Integer.parseInt(element[0]),
                    element[2],
                    element[4],
                    TaskStatus.valueOf(element[3]),
                    LocalDateTime.parse(element[6]),
                    Duration.parse(element[7])
            );
            taskStorage.put(task.getId(), task);
            return;
        }
        if (element[1].equals(TaskType.SUBTASK.name())) {
            SubTask subTask = new SubTask(
                    Integer.parseInt(element[0]),
                    element[2],
                    element[4],
                    TaskStatus.valueOf(element[3]),
                    Integer.parseInt(element[5]),
                    LocalDateTime.parse(element[6]),
                    Duration.parse(element[7])
            );
            subTaskStorage.put(subTask.getId(), subTask);
            return;
        }
        if (element[1].equals(TaskType.EPIC.name())) {
            Epic epic = new Epic(
                    Integer.parseInt(element[0]),
                    element[2],
                    element[4],
                    TaskStatus.valueOf(element[3])
            );
            epicStorage.put(epic.getId(), epic);
        }

    }

    private void saveToFile() {
        try (OutputStream outputStream = new FileOutputStream(fileLocation);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(outputStreamWriter)) {

            writer.write("id,type,name,status,description,epic,startOn,duration");
            writer.newLine();

            List<String> lines = getAllLinesForWriteInFile();

            for (String line: lines) {
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveToFileException(e);
        }
    }

    private List<String> getAllLinesForWriteInFile() {

        List<String> allLinesList = new ArrayList<>();

        List<String> taskLines = getTasksLines();
        List<String> subTaskLines = getSubTasksLines();
        List<String> epicLines = getEpicLines();

        allLinesList.addAll(taskLines);
        allLinesList.addAll(subTaskLines);
        allLinesList.addAll(epicLines);

        return allLinesList;
    }

    private List<String> getEpicLines() {
        List<String> epicLines = new ArrayList<>();
        for (Integer id: epicStorage.keySet()) {
            String[] line = new String[5];
            Epic epic = epicStorage.get(id);
            line[0] = String.valueOf(epic.getId());
            line[1] = "EPIC";
            line[2] = epic.getName();
            line[3] = epic.getTaskStatus().name();
            line[4] = epic.getDescription();
            String joinedLine = String.join(",", line);
            epicLines.add(joinedLine);
        }
        return epicLines;
    }

    private List<String> getSubTasksLines() {
        List<String> subTaskLines = new ArrayList<>();
        for (Integer id: subTaskStorage.keySet()) {
            String[] line = new String[8];
            SubTask subTask = subTaskStorage.get(id);
            line[0] = String.valueOf(subTask.getId());
            line[1] = "SUBTASK";
            line[2] = subTask.getName();
            line[3] = subTask.getTaskStatus().name();
            line[4] = subTask.getDescription();
            line[5] = String.valueOf(subTask.getLinkedEpicId());
            setTimeParams(subTaskLines, line, subTask.getStartTime(), subTask.getDuration());
        }
        return subTaskLines;
    }

    private List<String> getTasksLines() {
        List<String> taskLines = new ArrayList<>();
        for (Integer id: taskStorage.keySet()) {
            String[] line = new String[8];
            Task task = taskStorage.get(id);
            line[0] = String.valueOf(task.getId());
            line[1] = "TASK";
            line[2] = task.getName();
            line[3] = task.getTaskStatus().name();
            line[4] = task.getDescription();
            line[5] = "";
            setTimeParams(taskLines, line, task.getStartTime(), task.getDuration());
        }
        return taskLines;
    }

    private void setTimeParams(List<String> subTaskLines, String[] line, LocalDateTime startTimeToWrite, Duration durationToWrite) {
        String startTime = "";
        if (startTimeToWrite != null) {
            startTime = startTimeToWrite.toString();
        }
        line[6] = startTime;
        String duration = "";
        if (durationToWrite != null) {
            duration = durationToWrite.toString();
        }
        line[7] = duration;
        String joinedLine = String.join(",", line);
        subTaskLines.add(joinedLine);
    }

    @Override
    public void deleteAllTask() {
        super.deleteAllTask();
        saveToFile();
    }

    @Override
    public void deleteAllSubTask() {
        super.deleteAllSubTask();
        saveToFile();
    }

    @Override
    public void deleteAllEpic() {
        super.deleteAllEpic();
        saveToFile();
    }

    @Override
    public void createTask(Task incomingTask) {
        super.createTask(incomingTask);
        saveToFile();
    }

    @Override
    public void createSubTask(SubTask incomingSubTask) {
        super.createSubTask(incomingSubTask);
        saveToFile();
    }

    @Override
    public void createEpic(Epic incomingEpic) {
        super.createEpic(incomingEpic);
        saveToFile();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        saveToFile();
    }

    @Override
    public void updateEpic(Epic incomingEpic) {
        super.updateEpic(incomingEpic);
        saveToFile();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        saveToFile();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        saveToFile();
    }

    @Override
    public void removeSubTask(int id) {
        super.removeSubTask(id);
        saveToFile();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        saveToFile();
    }
}
