package controller;

import models.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private final ArrayList<Task> historyListOfTasks = new ArrayList<>();
    private static final int MAX_HISTORY_TASKS = 10;
    public InMemoryHistoryManager() {

    }
    @Override
    public ArrayList<Task> getHistory(){
        return historyListOfTasks;
    }
    @Override
    public void add(Task task){
        if(historyListOfTasks.size()<MAX_HISTORY_TASKS){
            historyListOfTasks.add(task);
        }else{
            historyListOfTasks.remove(historyListOfTasks.size()-1);
            historyListOfTasks.add(0,task);
        }
    }
}
