package httptaskserver;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import models.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BasicRequestHandler {

    private static final int OK_CODE = 200;
    private static final int OK_WITHOUT_ANSWER_CODE = 201;

    private static final int SERVER_SIDE_ERROR_CODE = 500;
    private static final int NOT_FOUND_CODE = 404;
    private static final int TASKS_INTERACTION_CODE = 406;

    protected void sendText(HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(OK_CODE, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    protected void sendTextUpdated(HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(OK_WITHOUT_ANSWER_CODE, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(SERVER_SIDE_ERROR_CODE, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(NOT_FOUND_CODE, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    protected void sendHasInteraction(HttpExchange exchange, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(TASKS_INTERACTION_CODE, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    protected Optional<Integer> getTaskId(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String idAsString;
        try {
            idAsString = path.split("/")[2];
        } catch (Exception ex) {
            //Если пользователь ввёл что-то неправильное, вернём Optional.empty()
            return Optional.empty();
        }
        if (idAsString.isEmpty()) {
            System.out.println("id пустой");
            return Optional.empty();
        }
        Integer id = Integer.parseInt(idAsString);
        return Optional.of(id);
    }

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
        }
    }
}
