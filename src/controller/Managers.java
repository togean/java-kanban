package controller;

public class Managers {
    public static TaskManager getDefault(String filename) {
        if (filename != null) {
            return new FileBackedTaskManager(filename);
        }
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
