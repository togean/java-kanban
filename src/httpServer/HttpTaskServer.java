package httpServer;

import com.sun.net.httpserver.HttpServer;
import controller.FileBackedTaskManager;
import controller.InMemoryTaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;

    public static void start(HttpServer server, InMemoryTaskManager tasksManager) throws IOException {
        server.createContext("/tasks", new TasksHandler(tasksManager));
        server.createContext("/epics", new EpicsHandler(tasksManager));
        server.createContext("/subtasks", new SubTasksHandler(tasksManager));
        server.createContext("/history", new HistoryHandler(tasksManager));
        server.createContext("/prioritized", new PrioritizedTasksHandler(tasksManager));
        server.bind(new InetSocketAddress(PORT), 0);//Порт и кол-во соединенй. 0 - значит берём от операционной системы
        server.start();
        System.out.println("HTTP-server запущен на " + PORT + " порту");
    }

    public static void stop(HttpServer server) {
        server.stop(1);
    }

    public static void main(String[] args) throws IOException {
        FileBackedTaskManager tasksManager = FileBackedTaskManager.loadFromFile("tasks.csv");
        HttpServer httpServer = HttpServer.create();
        start(httpServer, tasksManager);

    }
}
