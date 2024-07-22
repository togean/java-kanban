package httptaskserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.InMemoryTaskManager;
import models.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TreeSet;

public class PrioritizedTasksHandler extends BasicRequestHandler implements HttpHandler {
    InMemoryTaskManager taskManager;
    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    public PrioritizedTasksHandler(InMemoryTaskManager manager) {
        this.taskManager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            TreeSet<Task> listOfPrioritizedTasks = taskManager.getPrioritizedTasks();
            String taskSerialized = gson.toJson(listOfPrioritizedTasks);
            sendText(exchange, taskSerialized);
        } else {
            sendText(exchange, "Такого метода для вывода задач в порядке приоритета нету");
        }
    }
}
