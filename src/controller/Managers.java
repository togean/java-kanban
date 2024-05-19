package controller;

public class Managers {
    private Manager newManager;

    public Manager getDefault(){
        return new InMemoryTaskManager(0);
    }
    public HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }

    public Manager getNewManager() {
        return newManager;
    }

    public void setNewManager(Manager newManager) {
        this.newManager = newManager;
    }
}
