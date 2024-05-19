package controller;

import models.SubTask;
import models.Task;
import models.TaskStatus;

import java.util.ArrayList;

public interface Manager {
    <T extends Task> void create(T newTask);
    void delete(Integer taskToBeDeleted);
    void update(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus);
    Task getTask(Integer taskToBeDisplayedByID);
    void deleteAll();
    ArrayList<SubTask> getSubTasksOfEpic(int epicID);
    ArrayList<Task> getHistory();
    ArrayList<Task> getAll(String typeOfTasksToList);
}
