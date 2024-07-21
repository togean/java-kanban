package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpServer;
import controller.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static httptaskserver.HttpTaskServer.start;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {
    InMemoryHistoryManager managerForHistory;
    TaskManager managerForInMemoryTasks;
    LocalDateTime taskStartDate;
    Duration taskDuration;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");

        @Override
        public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
            jsonWriter.value(localDate.format(DATE_TIME_FORMATTER));
        }

        @Override
        public LocalDateTime read(final JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), DATE_TIME_FORMATTER);
        }
    }

    static class DurationAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
            jsonWriter.value(duration.toMinutes());
        }

        @Override
        public Duration read(final JsonReader jsonReader) throws IOException {
            return Duration.ofMinutes(Long.parseLong(jsonReader.nextString()));
        }
    }

    static class TaskStatusAdapter extends TypeAdapter<TaskStatus> {

        @Override
        public void write(final JsonWriter jsonWriter, final TaskStatus taskStatus) throws IOException {
            jsonWriter.value(taskStatus.name());
        }

        @Override
        public TaskStatus read(final JsonReader jsonReader) throws IOException {
            String s = jsonReader.nextString();
            return TaskStatus.valueOf(s);
            // return TaskStatus.valueOf(jsonReader.nextString());
        }
    }

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
            .create();

    HttpServer httpServer = null;

    @BeforeEach
    public void BeforeEach() throws IOException {
        managerForInMemoryTasks = Managers.getDefault(null);
        managerForHistory = (InMemoryHistoryManager) Managers.getDefaultHistory();
        httpServer = HttpServer.create();
        start(httpServer, (InMemoryTaskManager) managerForInMemoryTasks);
    }

    @AfterEach
    public void AfterEach() {
        httpServer.stop(0);
    }

    @Test
    void testToGetAllTasksFromServer() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newTask = new StandardTask("task1", "task1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createTask(newTask);
        //Сохраним созданную задачу в переменной для сравнения с тем, что выдаст нам сервер
        List<Task> createdTasks = managerForInMemoryTasks.getAllTasks();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromServerResponse = null;
        if (response.statusCode() == 200) {
            tasksFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<StandardTask>>() {
            }.getType());
        }
        assertEquals(createdTasks, tasksFromServerResponse);
    }

    @Test
    void testToGetAllEpicsFromServer() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("epic1", "epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        //Сохраним созданную задачу в переменной для сравнения с тем, что выдаст нам сервер
        List<Epic> createdEpics = managerForInMemoryTasks.getAllEpics();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epicsFromServerResponse = null;
        if (response.statusCode() == 200) {
            epicsFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
            }.getType());
        }
        assertEquals(createdEpics, epicsFromServerResponse);
    }

    @Test
    void testToGetAllSubtasksFromServer() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("epic1", "epic1 details", taskStartDate, taskDuration);
        SubTask newSubTask = new SubTask("subtask1", "subtask1 details", 1, taskStartDate, taskDuration);
        //Создаём эпик для подзадачи
        managerForInMemoryTasks.createEpic(newEpic);
        //Создаём подзадачу
        managerForInMemoryTasks.createSubtask(newSubTask);
        //Сохраним созданную задачу в переменной для сравнения с тем, что выдаст нам сервер
        List<SubTask> createdSubTasks = managerForInMemoryTasks.getAllSubtasks();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subtasksFromServerResponse = null;
        if (response.statusCode() == 200) {
            subtasksFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
            }.getType());
        }
        assertEquals(createdSubTasks, subtasksFromServerResponse);
    }

    @Test
    void testToPOSTTaskToServer() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.25", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newTask = new StandardTask("task1", "task1 details", taskStartDate, taskDuration);
        newTask.setId(1);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании задачи, а у нас тут его выставляем руками
        String gsonForPOSTTaskRequest = gson.toJson(newTask, StandardTask.class);
        //Для проверки обнуляем список задач
        managerForInMemoryTasks.deleteAll();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gsonForPOSTTaskRequest))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Теперь делаю запрос на сервер, что бы получить список задач
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> tasksFromServerResponse = null;
        if (response.statusCode() == 200) {
            tasksFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<StandardTask>>() {
            }.getType());
        }
        assertEquals(newTask, tasksFromServerResponse.get(0));
    }

    @Test
    void testToPOSTEpicToServer() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.25", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("task1", "task1 details", taskStartDate, taskDuration);
        newEpic.setId(1);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании эпика, а у нас тут его выставляем руками
        String gsonForPOSTEpicRequest = gson.toJson(newEpic, Epic.class);
        //Для проверки обнуляем список задач
        managerForInMemoryTasks.deleteAll();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gsonForPOSTEpicRequest))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Теперь делаю запрос на сервер, что бы получить список эпиков
        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Epic> epicsFromServerResponse = null;
        if (response.statusCode() == 200) {
            epicsFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
            }.getType());
        }
        assertEquals(newEpic, epicsFromServerResponse.get(0));
    }

    @Test
    void testToPOSTSubtaskToServer() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("epic1", "epic1 details", taskStartDate, taskDuration);
        newEpic.setId(1);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании эпика, а у нас тут его выставляем руками
        SubTask newSubTask = new SubTask("subtask1", "subtask1 details", 1, taskStartDate, taskDuration);
        newSubTask.setId(2);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании подзадачи, а у нас тут его выставляем руками
        String gsonForPOSTEpicRequest = gson.toJson(newEpic, Epic.class);
        String gsonForSubtaskPOSTRequest = gson.toJson(newSubTask, SubTask.class);
        //Создаём эпик для подзадачи
        managerForInMemoryTasks.createEpic(newEpic);
        //Создаём подзадачу
        managerForInMemoryTasks.createSubtask(newSubTask);
        //Сохраним созданную задачу в переменной для сравнения с тем, что выдаст нам сервер
        List<SubTask> createdSubTasks = managerForInMemoryTasks.getAllSubtasks();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gsonForPOSTEpicRequest))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Теперь делаю запрос на создание подзадачи созданного эпика
        client = HttpClient.newHttpClient();
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gsonForSubtaskPOSTRequest))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Теперь делаю запрос на сервер, что бы получить список задач
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<SubTask> subtasksFromServerResponse = null;
        if (response.statusCode() == 200) {
            subtasksFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
            }.getType());
        }
        assertEquals(createdSubTasks.get(0), subtasksFromServerResponse.get(0));
    }

    @Test
    void historyShouldReturnRequestedTask() throws IOException, InterruptedException {
        //Создаю тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 20.07.25", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newTask = new StandardTask("task1", "task1 details", taskStartDate, taskDuration);
        newTask.setId(1);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании задачи, а у нас тут его выставляем руками
        String gsonForPOSTTaskRequest = gson.toJson(newTask, StandardTask.class);
        //Для проверки обнуляем список задач
        managerForInMemoryTasks.deleteAll();
        //Создаю клиента и формирую запрос к серверу
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(gsonForPOSTTaskRequest))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        //Теперь надо запросить задачу по ID
        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        StandardTask tasksFromServerResponse = null;
        if (response.statusCode() == 200) {
            tasksFromServerResponse = gson.fromJson(response.body(), new TypeToken<StandardTask>() {
            }.getType());
        }
        //Теперь можно запросить историю
        url = URI.create("http://localhost:8080/history");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<StandardTask> historyTasksFromServerResponse = null;
        if (response.statusCode() == 200) {
            historyTasksFromServerResponse = gson.fromJson(response.body(), new TypeToken<List<StandardTask>>() {
            }.getType());
        }
        assertEquals(tasksFromServerResponse, historyTasksFromServerResponse.get(0));
    }

    @Test
    void tasksShouldBeInPrioritizedOrder() throws IOException, InterruptedException {
        //Создаём файловый таск-менеджер, т.к. функция вывода по приоритету реализована в нём
        FileBackedTaskManager taskManager = FileBackedTaskManager.loadFromFile("test.txt");
        //Создаю первую тестовую задачу
        taskStartDate = LocalDateTime.parse("10:00 21.07.25", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newTask1 = new StandardTask("task1", "task1 details", taskStartDate, taskDuration);
        newTask1.setId(1);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании задачи, а у нас тут его выставляем руками
        String gsonForPOSTTask2Request = gson.toJson(newTask1, StandardTask.class);
        //Создаю вторую тестовую задачу
        taskStartDate = LocalDateTime.parse("09:00 20.07.25", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newTask2 = new StandardTask("task2", "task2 details", taskStartDate, taskDuration);
        newTask2.setId(2);//Устанавливаю ID, т.к. он используется при сравнении и таск-менеджер его выставит при создании задачи, а у нас тут его выставляем руками
        String gsonForPOSTTask1Request = gson.toJson(newTask2, StandardTask.class);
        //Для корректности проверки обнуляем список задач со стороны сервера
        taskManager.deleteAll();
        //Создаю клиента и формирую запрос к серверу на создание первой задачи (она хоть и создаётся раньше, но дата у неё позднее)
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gsonForPOSTTask1Request)).build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        //Формирую запрос к серверу на создание второй задачи (она хоть и создаётся позднее, но дата у неё раньше)
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(gsonForPOSTTask2Request)).build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        //Создаю запрос на получение сортированного списка
        url = URI.create("http://localhost:8080/prioritized");
        request1 = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response3 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        List<StandardTask> prioritizedTasksFromServerResponse = null;
        System.out.println("вторая задача должна быть на первом месте: " + response3.body());
        if (response3.statusCode() == 200) {
            prioritizedTasksFromServerResponse = gson.fromJson(response3.body(), new TypeToken<List<StandardTask>>() {
            }.getType());
        }
        assertTrue(prioritizedTasksFromServerResponse.get(0).getStartDateTime().isBefore(prioritizedTasksFromServerResponse.get(1).getStartDateTime()), "Задачи в списке приоритета находятся в неправильном порядке");
    }

    @Test
    void twoTasksIsEqualsIfItHasSameID() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newTask = new StandardTask("task1", "task1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createTask(newTask);
        StandardTask newTask1, newTask2;
        newTask1 = managerForInMemoryTasks.getTask(1);
        newTask2 = managerForInMemoryTasks.getTask(1);
        assertTrue(newTask1.equals(newTask2), "Таски " + newTask1 + " и " + newTask2 + " не равны друг другу");

    }

    @Test
    void canSaveAndReadTaskFromFile() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        TaskManager taskManager1 = Managers.getDefault("test.txt");//Создаём менеджер через конструктор
        taskManager1.createTask(newStandardtask);//Создаём вторым менеджером таск с записью в тестовый файл

        FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile("test.txt");
        StandardTask task1 = taskManager1.getTask(1);
        StandardTask task2 = taskManager2.getTask(1);
        taskManager1.deleteAll();//Удаляем ненужную уже задачку из файла

        assertEquals(task2.getDescription(), task1.getDescription(), "Записанный в файл таск не соответствует прочитанному из этого файла");

    }

    @Test
    void twoInstancesOfStandardTaskIsEqualsIfItHasSameID() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createTask(newStandardtask);
        Task newTask1, newTask2;
        newTask1 = managerForInMemoryTasks.getTask(1);
        newTask2 = managerForInMemoryTasks.getTask(1);
        assertTrue(newTask1.equals(newTask2), "Таски " + newTask1 + " и " + newTask2 + " не равны друг другу");
    }

    @Test
    void twoInstancesOfEpicsIsEqualsIfItHasSameID() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        Epic newEpic1, newEpic2;
        newEpic1 = managerForInMemoryTasks.getEpic(1);
        newEpic2 = managerForInMemoryTasks.getEpic(1);
        assertTrue(newEpic1.equals(newEpic2), "Таски " + newEpic1 + " и " + newEpic2 + " не равны друг другу");
    }


    @Test
    void twoInstancesOfSubTasksIsEqualsIfItHasSameID() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);//Сначала создаём эпик для связи с подзадачей
        managerForInMemoryTasks.createEpic(newEpic);
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        SubTask newSubTask1, newSubTask2;
        newSubTask1 = managerForInMemoryTasks.getSubTask(2);//Запрашиваем одинаковый ID подзадачи
        newSubTask2 = managerForInMemoryTasks.getSubTask(2);
        assertTrue(newSubTask1.equals(newSubTask2), "Таски " + newSubTask1 + " и " + newSubTask2 + " не равны друг другу");
    }

    @Test
    void subtaskCanNotBeEpicForOtherSubtask() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);//Сначала создаём эпик для связи с подзадачей
        managerForInMemoryTasks.createEpic(newEpic);
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask2 = new SubTask("SubTask2", "SubTask2 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask2);
        taskStartDate = LocalDateTime.parse("10:30 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask3 = new SubTask("SubTask1", "SubTask1 details", 3, taskStartDate, taskDuration);//Вторая подзадача создаётся с ID=2, по этому его тут и пробуем
        int result = managerForInMemoryTasks.createSubtask(newSubTask3);
        assertTrue(result == 0, "Созданная подзадача пытается сослаться на подзадачу как на эпик");
    }

    @Test
    void canFindCreatedTaskByID() {
        int plannedTaskID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        int createdtaskID = managerForInMemoryTasks.createTask(newStandardtask);
        newStandardtask = managerForInMemoryTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.getId() == createdtaskID, "Созданная задача некорректно создаётся и не находится под ожидаемым ID");

        int plannedEpicID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        int createdEpicID = managerForInMemoryTasks.createEpic(newEpic);
        newEpic = managerForInMemoryTasks.getEpic(plannedEpicID);
        assertTrue(newEpic.getId() == createdEpicID, "Созданный эпик не корректно создаётся и не находится под ожидаемым ID");

        int plannedSubTaskID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2, taskStartDate, taskDuration);
        int createdSubTaskID = managerForInMemoryTasks.createSubtask(newSubTask);
        newSubTask = managerForInMemoryTasks.getSubTask(plannedSubTaskID);
        assertTrue(newSubTask.getId() == createdSubTaskID, "Созданная подзадача не корректно создаётся и не находится под ожидаемым ID");

    }

    @Test
    void managerDoesnotChangeTaskWhenCreatesIt() {
        int plannedTaskID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createTask(newStandardtask);
        StandardTask standardtaskToCheck = managerForInMemoryTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.equals(standardtaskToCheck), "Созданная задача изменяется при работе менеджера задач");

        int plannedEpicID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        Epic epicToCheck = managerForInMemoryTasks.getEpic(plannedEpicID);
        assertTrue(newEpic.equals(epicToCheck), "Созданный эпик изменяется при работе менеджера задач");

        int plannedSubTaskID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        SubTask subTaskToCheck = managerForInMemoryTasks.getSubTask(plannedSubTaskID);
        assertTrue(newSubTask.equals(subTaskToCheck), "Созданная подзадача изменяется при работе менеджера задач");
    }

    @Test
    void canUpdateAnyTaskAsExpected() {
        int plannedTaskID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createTask(newStandardtask);
        String oldDetails = (managerForInMemoryTasks.getTask(plannedTaskID)).getDetails();
        TaskStatus oldStatus = (managerForInMemoryTasks.getTask(plannedTaskID)).getTaskStatus();
        managerForInMemoryTasks.updateTask(plannedTaskID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedDetails = (managerForInMemoryTasks.getTask(plannedTaskID)).getDetails();
        TaskStatus updatedStatus = (managerForInMemoryTasks.getTask(plannedTaskID)).getTaskStatus();
        assertTrue(!updatedDetails.equals(oldDetails), "Обновлённое описание задачи не равно ожидаемому при обновлении");
        assertTrue(!updatedStatus.equals(oldStatus), "Обновлённый статус задачи не равен ожидаемому при обновлении");

        int plannedEpicID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        String oldEpicDetails = (managerForInMemoryTasks.getEpic(plannedEpicID)).getDetails();
        managerForInMemoryTasks.updateEpic(plannedEpicID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedEpicDetails = (managerForInMemoryTasks.getEpic(plannedEpicID)).getDetails();
        assertTrue(!updatedEpicDetails.equals(oldEpicDetails), "Обновлённое описание эпика не равно ожидаемому при обновлении");

        int plannedSubTaskID = managerForInMemoryTasks.getTaskID();
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        String oldSubtaskDetails = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getDetails();
        TaskStatus oldSubtaskStatus = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getTaskStatus();
        managerForInMemoryTasks.updateSubtask(plannedSubTaskID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedSubTaskDetails = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getDetails();
        TaskStatus updatedSubTaskStatus = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getTaskStatus();
        assertTrue(!updatedSubTaskDetails.equals(oldSubtaskDetails), "Обновлённое описание подзадачи не равно ожидаемому при обновлении");
        assertTrue(!updatedSubTaskStatus.equals(oldSubtaskStatus), "Обновлённый статус подзадачи не равен ожидаемому при обновлении");
    }

    @Test
    void canDeleteTask() {
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.deleteTask(1);
        assertNull(managerForInMemoryTasks.getTask(1), "Задача не удалилась");
    }

    @Test
    void canDeleteEpic() {
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        managerForInMemoryTasks.deleteEpic(1);
        assertNull(managerForInMemoryTasks.getTask(1), "Эпик не удалился");

    }

    @Test
    void canDeleteSubTask() {
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        managerForInMemoryTasks.deleteSubtask(2);
        assertNull(managerForInMemoryTasks.getTask(2), "Задача не удалилась");
    }

    @Test
    void canSaveHistory() {
        List<Task> listOfHistory;
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        int id = managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.getTask(id);
        listOfHistory = managerForInMemoryTasks.getHistory();
        assertTrue(listOfHistory.size() > 0, "задача не помещена в историю");
    }

    @Test
    void compareTaskInListAndTaskInHistory() {
        List<Task> listOfHistory;
        boolean taskIsTheSame = false;
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        int id = managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.getTask(id);
        listOfHistory = managerForInMemoryTasks.getHistory();
        for (Task task : listOfHistory) {
            taskIsTheSame = newStandardtask.getDescription().equals(task.getDescription());
        }
        assertTrue(taskIsTheSame, "задача в истории не соответствует созданной проверочной задачи");
    }

    @Test
    void canDeleteItemInHistory() {
        int numberOfTasksInHistoryBeforeDeletion;
        int numberOfTasksInHistoryAfterDeletion;
        taskStartDate = LocalDateTime.parse("10:00 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        int id = managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.getTask(id);
        numberOfTasksInHistoryBeforeDeletion = managerForInMemoryTasks.getHistory().size();
        managerForHistory.remove(id);
        numberOfTasksInHistoryAfterDeletion = managerForInMemoryTasks.getHistory().size();
        assertTrue((numberOfTasksInHistoryBeforeDeletion - numberOfTasksInHistoryAfterDeletion) == 0, "задача не удалена в истории");
    }

    @Test
    void managerShouldReturnRealInstancesOfManagers() {
        TaskManager newTaskManagerForTest = Managers.getDefault(null);
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        int EpicID = newTaskManagerForTest.createEpic(newEpic);
        assertNotNull(EpicID, "Новый taskMeneger неправильно реализовал эпик");

        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1, taskStartDate, taskDuration);
        int SubTaskID = newTaskManagerForTest.createSubtask(newSubTask);
        assertNotNull(SubTaskID, "Новый taskMeneger неправильно реализовал эпик");


        HistoryManager newHistoryManager = Managers.getDefaultHistory();
        newHistoryManager.add(newEpic);
        assertNotNull(newHistoryManager.getHistory(), "HistoryManager неправильно вернул getHistory");
    }

    @Test
    void managerShouldReturnSubtasksOfEpic() {
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        ArrayList<SubTask> listOfSubtasks = managerForInMemoryTasks.getSubTasksOfEpic(1);
        assertNotNull(listOfSubtasks, "Проблемы с получением подзадач эпика");
    }

    @Test
    void twoTasksAreOverlapped() {
        taskStartDate = LocalDateTime.parse("10:00 20.08.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(30);
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details", taskStartDate, taskDuration);
        LocalDateTime task2StartDate = LocalDateTime.parse("10:15 20.08.24", DATE_TIME_FORMATTER);
        Duration task2Duration = Duration.ofMinutes(10);
        StandardTask newStandardtask2 = new StandardTask("StandardTask2", "StandardTask2 details", task2StartDate, task2Duration);
        FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile("test.txt");
        taskManager2.createTask(newStandardtask);
        taskManager2.getPrioritizedTasks();
        boolean result = taskManager2.checkTasksOverlapping(newStandardtask2);
        assertTrue(result, "Менеджер неверно определил наложение задач");
        taskManager2.deleteAll();
    }

    @Test
    void epicWillChangeStatusDependingOnSubtasksStatuses() {
        taskStartDate = LocalDateTime.parse("10:10 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        Epic newEpic = new Epic("Epic1", "Epic1 details", taskStartDate, taskDuration);
        managerForInMemoryTasks.createEpic(newEpic);
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask);
        taskStartDate = LocalDateTime.parse("10:20 20.07.24", DATE_TIME_FORMATTER);
        taskDuration = Duration.ofMinutes(5);
        SubTask newSubTask2 = new SubTask("SubTask2", "SubTask2 details", 1, taskStartDate, taskDuration);
        managerForInMemoryTasks.createSubtask(newSubTask2);

        managerForInMemoryTasks.updateSubtask(2, "Updated details", TaskStatus.DONE);
        managerForInMemoryTasks.updateSubtask(3, "Updated details", TaskStatus.DONE);
        newEpic = managerForInMemoryTasks.getEpic(1);

        assertTrue(newEpic.getTaskStatus().equals(TaskStatus.DONE), "Статус эпика не обновляется на DONE, когда у всех его подзадач статус DONE");

        managerForInMemoryTasks.updateSubtask(2, "Updated details", TaskStatus.NEW);
        managerForInMemoryTasks.updateSubtask(3, "Updated details", TaskStatus.NEW);
        newEpic = managerForInMemoryTasks.getEpic(1);

        assertTrue(newEpic.getTaskStatus().equals(TaskStatus.NEW), "Статус эпика не обновляется на NEW, когда у всех его подзадач статус NEW");

        managerForInMemoryTasks.updateSubtask(2, "Updated details", TaskStatus.IN_PROGRESS);
        managerForInMemoryTasks.updateSubtask(3, "Updated details", TaskStatus.DONE);
        newEpic = managerForInMemoryTasks.getEpic(1);

        assertTrue(newEpic.getTaskStatus().equals(TaskStatus.IN_PROGRESS), "Статус эпика не обновляется на IN_PROGRESS, когда у какой-то из его подзадач статус IN_PROGRESS");

    }

}