package controller;

public class Managers {
    public static TaskManager getDefault(String methodType, String filename) {
        if (methodType.equals("InFile")) {
            return new FileBackedTaskManager(filename);
        }
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
