package controller;

import models.*;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    Integer createTask(StandardTask newTask);

    Integer createEpic(Epic newTask);

    Integer createSubtask(SubTask newTask);

    void deleteTask(Integer taskToBeDeleted);

    void deleteEpic(Integer taskToBeDeleted);

    void deleteSubtask(Integer taskToBeDeleted);

    void updateTask(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus);

    void updateSubtask(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus);

    void updateEpic(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus);

    SubTask getSubTask(Integer taskToBeDisplayedByID);

    StandardTask getTask(Integer taskToBeDisplayedByID);

    Integer getTaskID();

    Epic getEpic(Integer taskToBeDisplayedByID);

    void deleteAll();

    ArrayList<SubTask> getSubTasksOfEpic(int epicID);

    List<Task> getHistory();

    ArrayList<SubTask> getAllSubtasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<Task> getAllTasks();
}
