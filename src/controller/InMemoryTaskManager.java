package controller;

import models.Epic;
import models.SubTask;
import models.Task;
import models.TaskStatus;
import models.StandardTask;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements Manager {

    private final HashMap<Integer, StandardTask> listOfStandardTasks = new HashMap<>();
    private final HashMap<Integer, SubTask> listOfSubtasks = new HashMap<>();
    private final HashMap<Integer, Epic> listOfEpics = new HashMap<>();
    private Integer taskID = 1;

    HistoryManager managerForHistory = Managers.getDefaultHistory();

    public HistoryManager getManagerForHistory() {
        return managerForHistory;
    }

    public Integer getTaskID() {
        return taskID;
    }

    @Override
    public Integer createTask(StandardTask taskToBeCreated) {
        int createdTaskID;
        taskToBeCreated.setId(taskID);
        listOfStandardTasks.put(taskID, taskToBeCreated);
        createdTaskID = taskID;
        taskID++;
        return createdTaskID;
    }

    @Override
    public Integer createSubtask(SubTask taskToBeCreated) {
        int createdTaskID = 0;
        Integer subtaskID = taskID;
        taskToBeCreated.setId(subtaskID);
        int parentID = (taskToBeCreated).getParentID();
        if (!listOfSubtasks.containsKey(parentID)) {//Проверяем, если родителем указан не эпик а подзадача
            listOfSubtasks.put(subtaskID, taskToBeCreated);
            //Проверяем эпик и связываем его с создаваемой подзадачей
            Epic epicToBeLinkedWithSubtask = listOfEpics.get(taskToBeCreated.getParentID());
            if (epicToBeLinkedWithSubtask != null) {
                ArrayList<Integer> listOfEpicsSubtasksToBeUpdated = epicToBeLinkedWithSubtask.getListOfSubtasks();
                listOfEpicsSubtasksToBeUpdated.add(subtaskID);
                epicToBeLinkedWithSubtask.setListOfTasks(listOfEpicsSubtasksToBeUpdated);
                listOfEpics.put(taskToBeCreated.getParentID(), epicToBeLinkedWithSubtask);
            }
            recalculateOrUpdateTaskStatus();
            createdTaskID = taskID;
            taskID++;
        }
        return createdTaskID;
    }

    @Override
    public Integer createEpic(Epic taskToBeCreated) {
        int createdTaskID;
        taskToBeCreated.setId(taskID);
        listOfEpics.put(taskID, taskToBeCreated);
        createdTaskID = taskID;
        taskID++;
        return createdTaskID;
    }

    @Override
    public void deleteSubtask(Integer taskToBeDeleted) {
        SubTask subtaskToBeDeleted = listOfSubtasks.get(taskToBeDeleted);
        Epic epicToBeUnLinkedWithDeletedSubtask = listOfEpics.get(subtaskToBeDeleted.getParentID());
        ArrayList<Integer> listOfEpicsSubtasks = epicToBeUnLinkedWithDeletedSubtask.getListOfSubtasks();
        listOfEpicsSubtasks.remove(subtaskToBeDeleted.getId());
        epicToBeUnLinkedWithDeletedSubtask.setListOfTasks(listOfEpicsSubtasks);
        listOfEpics.put(epicToBeUnLinkedWithDeletedSubtask.getId(), epicToBeUnLinkedWithDeletedSubtask);
        listOfSubtasks.remove(taskToBeDeleted);
    }

    @Override
    public void deleteTask(Integer taskToBeDeleted) {
        listOfStandardTasks.remove(taskToBeDeleted);
    }

    @Override
    public void deleteEpic(Integer taskToBeDeleted) {
        Epic epicToBeDeleted = listOfEpics.get(taskToBeDeleted);
        ArrayList<Integer> listOfSubtasksToBeDeleted;
        listOfSubtasksToBeDeleted = epicToBeDeleted.getListOfSubtasks();
        if (listOfSubtasksToBeDeleted != null) {
            for (int i : listOfSubtasksToBeDeleted) {
                listOfSubtasks.remove(i);
            }
        }
        listOfEpics.remove(taskToBeDeleted);
    }

    @Override
    public void deleteAll() {
        for (int i = 0; i < listOfEpics.size(); i++) {
            deleteEpic(i);
        }
        for (int i = 0; i < listOfStandardTasks.size(); i++) {
            deleteTask(i);
        }
    }

    @Override
    public void updateTask(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus) {
        StandardTask standardtaskToBeUpdated = listOfStandardTasks.get(taskID);
        standardtaskToBeUpdated.setDetails(taskNewDetails);
        standardtaskToBeUpdated.setTaskStatus(taskNewStatus);
        listOfStandardTasks.put(taskID, standardtaskToBeUpdated);
    }

    @Override
    public void updateSubtask(Integer subtaskID, String subtaskNewDetails, TaskStatus subtaskNewStatus) {
        SubTask subtaskToBeUpdated = listOfSubtasks.get(subtaskID);
        subtaskToBeUpdated.setDetails(subtaskNewDetails);
        subtaskToBeUpdated.setTaskStatus(subtaskNewStatus);
        listOfSubtasks.put(subtaskID, subtaskToBeUpdated);
        recalculateOrUpdateTaskStatus();
    }

    @Override
    public void updateEpic(Integer epicID, String epicNewDetails, TaskStatus epicNewStatus) {
        Epic epicToBeUpdated = listOfEpics.get(epicID);
        epicToBeUpdated.setDetails(epicNewDetails);
        listOfEpics.put(epicID, epicToBeUpdated);
    }

    @Override
    public StandardTask getTask(Integer taskToBeDisplayedByID) {
        StandardTask task = listOfStandardTasks.get(taskToBeDisplayedByID);
        managerForHistory.add(task);
        return task;
    }

    @Override
    public SubTask getSubTask(Integer taskToBeDisplayedByID) {
        SubTask task = listOfSubtasks.get(taskToBeDisplayedByID);
        managerForHistory.add(task);
        return task;
    }

    @Override
    public Epic getEpic(Integer taskToBeDisplayedByID) {
        Epic task = listOfEpics.get(taskToBeDisplayedByID);
        managerForHistory.add(task);
        return task;
    }

    @Override
    public ArrayList<SubTask> getSubTasksOfEpic(int epicID) {
        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        ArrayList<Integer> listOfSubtasksIDs;
        Epic epicToGetList = listOfEpics.get(epicID);//Получаем данные выбранного эпика
        listOfSubtasksIDs = epicToGetList.getListOfSubtasks();//Забираем из эпика список его сабтасков
        if (listOfSubtasksIDs != null) {
            for (Integer i : listOfSubtasksIDs) {//Идём по списку сабтасков выбранного эпика
                SubTask subtaskToCheckTheirParentId = listOfSubtasks.get(i);
                listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);//Наполняем список сабтасков
            }
        }
        return listOfSubtasksForEPIC;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return managerForHistory.getHistory();
    }

    @Override
    public ArrayList<SubTask> getAllSubtasks() {
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask task : listOfSubtasks.values()) {
            result.add(task);
        }
        return result;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> result = new ArrayList<>();
        for (Epic epic : listOfEpics.values()) {
            result.add(epic);
        }
        return result;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> result = new ArrayList<>();
        for (Task task : listOfStandardTasks.values()) {
            result.add(task);
        }
        return result;
    }

    private void recalculateOrUpdateTaskStatus() {
        for (Integer i : listOfEpics.keySet()) {//Проходим по каждому эпику
            Epic currentRecalculatedEpic = listOfEpics.get(i);
            int numberOfSubtsaksInEpic = currentRecalculatedEpic.getListOfSubtasks().size();
            int numberOfNew = 0; //Кол-во подзадач статуса New
            int numberOfDone = 0; //Кол-во подзадач статуса DONE
            ArrayList<Integer> listOfEpicSubtasks = listOfEpics.get(i).getListOfSubtasks();//Тут будут ID-шники подзадач текущего эпика
            for (int j = 1; j < numberOfSubtsaksInEpic; j++) { //В цикле читаем статусы подзадач текущей родительской задачи для расчёта статуса родительской задачи
                SubTask currentSubtaskToCalculateStatus = listOfSubtasks.get(j);
                if(currentSubtaskToCalculateStatus!=null) {
                    if ((currentSubtaskToCalculateStatus.getTaskStatus()).equals(TaskStatus.NEW)) {
                        numberOfNew++;
                    }
                    if ((currentSubtaskToCalculateStatus.getTaskStatus()).equals(TaskStatus.DONE)) {
                        numberOfDone++;
                    }
                }
            }
            if (numberOfNew == listOfEpicSubtasks.size()-1) { //Тут -1 т.к. при инициализации эпика перое значение в списке подзадач 0 (но 0 не используется, все ID начинаются с 1)
                currentRecalculatedEpic.setTaskStatus(TaskStatus.NEW);
                listOfEpics.put(i, currentRecalculatedEpic);
            } else if (numberOfDone == listOfEpicSubtasks.size()-1) {//Тут -1 т.к. при инициализации эпика перое значение в списке подзадач 0 (но 0 не используется, все ID начинаются с 1)
                currentRecalculatedEpic.setTaskStatus(TaskStatus.DONE);
                listOfEpics.put(i, currentRecalculatedEpic);
            } else {//Если статус не NEW и не DONE, значит задача пока в состоянии IN_PROGRESS
                currentRecalculatedEpic.setTaskStatus(TaskStatus.IN_PROGRESS);
                listOfEpics.put(i, currentRecalculatedEpic);//Сохраняем вычисленное значение родительской задачи
            }
        }
    }
}
