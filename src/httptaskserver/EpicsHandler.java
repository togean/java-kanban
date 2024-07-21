package httptaskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.InMemoryTaskManager;
import controller.TaskManager;
import models.Epic;
import models.SubTask;
import models.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

public class EpicsHandler extends BasicRequestHandler implements HttpHandler {
    TaskManager taskManager;
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(TaskStatus.class, new TaskStatusAdapter())
            .create();

    public EpicsHandler(InMemoryTaskManager manager) {
        this.taskManager = manager;
    }

    static class UserTypeToken extends TypeToken<Epic> {
        // здесь ничего не нужно реализовывать
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            Optional<Integer> epicId = getTaskId(exchange);
            //Проверяем, запрашивают ли эпик по ID или идёт запрос всех эпиков
            if (epicId.isPresent()) {
                //Запрашивают по ID
                Epic requestedEpic = taskManager.getEpic(epicId.get());
                if (requestedEpic == null) {
                    sendNotFound(exchange, "Эпика с таким ID нету");
                } else {
                    //Парсим path для поиска в конце подстроки "subtasks", если пользователь запрашивает подзадачи эпика
                    String path = exchange.getRequestURI().getPath();
                    boolean subtasksRequested = path.endsWith("subtasks");
                    if (subtasksRequested) {
                        //Выводим подзадачи эпика
                        ArrayList<SubTask> epicSubtasks = taskManager.getSubTasksOfEpic(epicId.get());
                        String epicSubtasksSerialized = gson.toJson(epicSubtasks);
                        sendText(exchange, epicSubtasksSerialized);
                    } else {
                        String epicSerialized = gson.toJson(requestedEpic);
                        sendText(exchange, epicSerialized);
                    }
                }
            } else {
                //Если нет ID, то выводим весь список
                ArrayList<Epic> listOfEpics = taskManager.getAllEpics();
                String epicSerialized = gson.toJson(listOfEpics);
                sendText(exchange, epicSerialized);
            }

        } else if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> epicId = getTaskId(exchange);
            Epic epicToBeCreated = gson.fromJson(requestBody, new UserTypeToken().getType());
            if (epicId.isPresent()) {
                //Если ID указан, то обновляем эпик с указанным ID
                try {
                    taskManager.updateEpic(epicId.get(), epicToBeCreated.getDetails(), epicToBeCreated.getTaskStatus());
                } catch (Exception ex) {
                    sendServerError(exchange, "Произошла ошибка на сервере. Возможно, вы ошиблись в списке параметров в запросе.");
                }
                sendTextUpdated(exchange, "Существующий эпик успешно обновлён");
            } else {
                //Если ID не указан, то создаём новый эпик
                Integer createdEpicID = 0;
                try {
                    createdEpicID = taskManager.createEpic(epicToBeCreated);
                } catch (Exception ex) {
                    sendServerError(exchange, "Произошла ошибка на сервере. Возможно, вы ошиблись в списке параметров в запросе.");
                }
                if (createdEpicID == 0) {
                    sendHasInteraction(exchange, "Создаваемый эпик пересекается с уже существующей задачей");
                } else {
                    sendText(exchange, "Новый эпик успешно создан");
                }
            }
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            Optional<Integer> epicId = getTaskId(exchange);
            if (epicId.isPresent()) {
                //Запрашивают удаление по ID
                taskManager.deleteEpic(epicId.get());
                sendText(exchange, "Операция удаления отдельного эпика выполнена успешно");
            } else {
                //Хотят удалить все задачи
                sendText(exchange, "Операция удаления всех эпиков сразу не предусмотрена");
            }
        } else {
            sendText(exchange, "Такого метода для работы с эпиками нету");
        }

    }

}
