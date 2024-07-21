package httptaskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.InMemoryTaskManager;
import controller.TaskManager;
import models.StandardTask;
import models.Task;
import models.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;


public class TasksHandler extends BasicRequestHandler implements HttpHandler {
    TaskManager taskManager;
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
            .create();

    public TasksHandler(InMemoryTaskManager manager) {
        this.taskManager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if ("GET".equals(exchange.getRequestMethod())) {
            Optional<Integer> taskId = getTaskId(exchange);
            //Проверяем, запрашивают ли задачу по ID или идёт запрос всех задач
            if (taskId.isPresent()) {
                //Запрашивают по ID
                StandardTask requestedTask = taskManager.getTask(taskId.get());
                if (requestedTask == null) {
                    sendNotFound(exchange, "Задачи с таким ID нету");
                } else {
                    String taskSerialized = gson.toJson(requestedTask);
                    sendText(exchange, taskSerialized);
                }
            } else {
                //Если нет ID, то выводим весь список
                ArrayList<Task> listOfStandardTasks = taskManager.getAllTasks();
                String taskSerialized = gson.toJson(listOfStandardTasks);
                sendText(exchange, taskSerialized);
            }

        } else if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> taskId = getTaskId(exchange);
            StandardTask taskToBeCreated = gson.fromJson(requestBody, new UserTypeToken().getType());
            if (taskId.isPresent()) {
                //Если ID указан, то обновляем задачу с указанным ID
                try {
                    taskManager.updateTask(taskId.get(), taskToBeCreated.getDetails(), taskToBeCreated.getTaskStatus());
                } catch (Exception ex) {
                    sendServerError(exchange, "Произошла ошибка на сервере. Возможно, вы ошиблись в списке параметров в запросе.");
                }
                sendTextUpdated(exchange, "Существующая задача успешно обновлена");
            } else {
                //Если ID не указан, то создаём новую задачу
                Integer createdTaskID = 0;
                try {
                    createdTaskID = taskManager.createTask(taskToBeCreated);
                } catch (Exception ex) {
                    sendServerError(exchange, "Произошла ошибка на сервере. Возможно, вы ошиблись в списке параметров в запросе.");
                }
                if (createdTaskID == 0) {
                    sendHasInteraction(exchange, "Создаваемая задача пересекается с уже существующей");
                } else {
                    sendText(exchange, "Новая задача успешно создана");
                }
            }
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            Optional<Integer> taskId = getTaskId(exchange);
            if (taskId.isPresent()) {
                //Запрашивают удаление по ID
                taskManager.deleteTask(taskId.get());
                sendText(exchange, "Операция удаления отдельной задачи выполнена успешно");
            } else {
                //Хотят удалить все задачи
                sendText(exchange, "Операция удаления всех задач сразу не предусмотрена");
            }
        } else {
            sendText(exchange, "Такого метода нет на сервере");
        }

    }

    static class UserTypeToken extends TypeToken<StandardTask> {
        // здесь ничего не нужно реализовывать
    }
}
