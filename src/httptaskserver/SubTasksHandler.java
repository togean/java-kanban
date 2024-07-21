package httptaskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.InMemoryTaskManager;
import controller.TaskManager;
import models.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class SubTasksHandler extends BasicRequestHandler implements HttpHandler {
    TaskManager taskManager;
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
            .create();

    public SubTasksHandler(InMemoryTaskManager manager) {
        this.taskManager = manager;
    }

    static class UserTypeToken extends TypeToken<SubTask> {
        // здесь ничего не нужно реализовывать
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Optional<Integer> subtaskId = getTaskId(exchange);
            //Проверяем, запрашивают ли задачу по ID или идёт запрос всех задач
            if (subtaskId.isPresent()) {
                //Запрашивают по ID
                SubTask requestedSubTask = taskManager.getSubTask(subtaskId.get());
                if (requestedSubTask == null) {
                    sendNotFound(exchange, "Подзадачи с таким ID нету");
                } else {
                    String requestedSubTaskSerialized = gson.toJson(requestedSubTask);
                    sendText(exchange, requestedSubTaskSerialized);
                }
            } else {
                //Если нет ID, то выводим весь список
                ArrayList<SubTask> listOfSubTasks = taskManager.getAllSubtasks();
                String listOfSubTasksSerialized = gson.toJson(listOfSubTasks);
                sendText(exchange, listOfSubTasksSerialized);
            }

        } else if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> subtaskId = getTaskId(exchange);
            SubTask subtaskToBeCreated = gson.fromJson(requestBody, new UserTypeToken().getType());
            if (subtaskId.isPresent()) {
                //Если ID указан, то обновляем задачу с указанным ID
                try {
                    taskManager.updateSubtask(subtaskId.get(), subtaskToBeCreated.getDetails(), subtaskToBeCreated.getTaskStatus());
                } catch (Exception ex) {
                    sendServerError(exchange, "Произошла ошибка на сервере. Возможно, вы ошиблись в списке параметров в запросе.");
                }
                sendTextUpdated(exchange, "Существующая подзадача успешно обновлена");
            } else {
                //Если ID не указан, то создаём новую задачу
                Integer createdSubTaskID = 0;
                try {
                    createdSubTaskID = taskManager.createSubtask(subtaskToBeCreated);
                } catch (Exception ex) {
                    sendServerError(exchange, "Произошла ошибка на сервере. Возможно, вы ошиблись в списке параметров в запросе.");
                }
                if (createdSubTaskID == 0) {
                    sendHasInteraction(exchange, "Создаваемая подзадача пересекается с уже существующей");
                } else {
                    sendText(exchange, "Новая подзадача успешно создана");
                }
            }
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            Optional<Integer> subtaskId = getTaskId(exchange);
            if (subtaskId.isPresent()) {
                //Запрашивают удаление по ID
                taskManager.deleteSubtask(subtaskId.get());
                sendText(exchange, "Операция удаления отдельной подзадачи выполнена успешно");
            } else {
                //Хотят удалить все задачи
                sendText(exchange, "Операция удаления всех подзадач сразу не предусмотрена");
            }
        } else {
            sendText(exchange, "Такого метода для работы с подзадачами нету");
        }
    }
}
