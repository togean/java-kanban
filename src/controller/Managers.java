package controller;

public class Managers {
    public static Manager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Manager getFileBackedTaskManager() {
        return new FileBackedTaskManager("tasks.csv");
    }
}
