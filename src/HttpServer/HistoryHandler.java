package HttpServer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.InMemoryTaskManager;
import controller.TaskManager;
import models.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HistoryHandler extends BasicRequestHandler implements HttpHandler {
    TaskManager taskManager;
    private Gson gson= new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();
    public HistoryHandler(InMemoryTaskManager manager) {
        this.taskManager=manager;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if("GET".equals(exchange.getRequestMethod())){
            List<Task> tasksInHistory = taskManager.getHistory();
            String tasksInHistorySerialized = gson.toJson(tasksInHistory);
            sendText(exchange, tasksInHistorySerialized);
        }else{
            sendText(exchange,"Такого метода для вывода истории обращения задач нету");
        }
    }
}
