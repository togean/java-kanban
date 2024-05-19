package controller;

import models.Epic;
import models.SubTask;
import models.Task;
import models.TaskStatus;
import models.StandardTask;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements Manager{

    private final HashMap<Integer, StandardTask> listOfStandardTasks = new HashMap<>();
    private final HashMap<Integer, SubTask> listOfSubtasks = new HashMap<>();
    private final HashMap<Integer, Epic> listOfEpics = new HashMap<>();

    Managers manager = new Managers();
    InMemoryHistoryManager managerForHistory = (InMemoryHistoryManager) manager.getDefaultHistory();

    private Integer taskID = 1;

    public InMemoryTaskManager(Integer taskID) {
        this.taskID = taskID;
    }

    public Integer getTaskID() {
        return taskID;
    }

    public void setTaskID(Integer taskID) {
        this.taskID = taskID;
    }

    @Override
    public <T extends Task> Integer create(T taskToBeCreated) {
        int createdTaskID = 0;
        if(taskToBeCreated instanceof StandardTask){
            taskToBeCreated.setId(taskID);
            listOfStandardTasks.put(taskID, (StandardTask)taskToBeCreated);
            createdTaskID = taskID;
            taskID++;
        }else if(taskToBeCreated instanceof SubTask){
            Integer subtaskID = taskID;
            taskToBeCreated.setId(subtaskID);
            int parentID = ((SubTask) taskToBeCreated).getParentID();
            if(!listOfSubtasks.containsKey(parentID)){
                listOfSubtasks.put(subtaskID, (SubTask) taskToBeCreated);
                //Сначала смотрим являетяс ли родитель просто задачей,а не эпиком и если да, то создаём эпик на основе задачи с сохранением ID, а задачу удаляем
                StandardTask standardtaskToBeConvertedToEpic = listOfStandardTasks.get(((SubTask) taskToBeCreated).getParentID());
                if (standardtaskToBeConvertedToEpic != null) {
                    Epic newEpicToBeCteated = new Epic(standardtaskToBeConvertedToEpic.getDescription(), standardtaskToBeConvertedToEpic.getDetails());
                    listOfEpics.put(standardtaskToBeConvertedToEpic.getId(), newEpicToBeCteated);
                    listOfStandardTasks.remove(standardtaskToBeConvertedToEpic.getId());
                }
                //Теперь проверяем эпик и связываем его с создаваемой подзадачей
                Epic epicToBeLinkedWithSubtask = listOfEpics.get(((SubTask) taskToBeCreated).getParentID());
                if (epicToBeLinkedWithSubtask != null) {
                    System.out.println("Эпик нашёлся");
                    if (epicToBeLinkedWithSubtask.getListOfSubtasks() != null) {//Если у эпика уже были подзадачи - надо список обновить
                        ArrayList<Integer> listOfEpicsSubtasksToBeUpdated = epicToBeLinkedWithSubtask.getListOfSubtasks();
                        listOfEpicsSubtasksToBeUpdated.add(subtaskID);
                        epicToBeLinkedWithSubtask.setListOfTasks(listOfEpicsSubtasksToBeUpdated);
                        listOfEpics.put(((SubTask) taskToBeCreated).getParentID(), epicToBeLinkedWithSubtask);

                    } else {//Если у эпика ещё нет подзадач, создаём новый список подзадач
                        System.out.println("У эпика ещё нет подзадач, добавляю");
                        ArrayList<Integer> newListOfSubtasksForEpic = new ArrayList<>();
                        newListOfSubtasksForEpic.add(subtaskID);
                        epicToBeLinkedWithSubtask.setListOfTasks(newListOfSubtasksForEpic);
                        listOfEpics.put(((SubTask) taskToBeCreated).getParentID(), epicToBeLinkedWithSubtask);
                    }
                }
                recalculateOrUpdateTaskStatus();
                createdTaskID = taskID;
                taskID++;
            }
        }else if(taskToBeCreated instanceof Epic){
            taskToBeCreated.setId(taskID);
            listOfEpics.put(taskID, (Epic)taskToBeCreated);
            createdTaskID = taskID;
            taskID++;
        }
        return createdTaskID;
    }

    @Override
    public void delete(Integer taskToBeDeleted) {
        SubTask subtaskToBeDeleted = listOfSubtasks.get(taskToBeDeleted);
        StandardTask standardtaskToBeDeleted = listOfStandardTasks.get(taskToBeDeleted);
        Epic epicToBeDeleted = listOfEpics.get(taskToBeDeleted);
        if(standardtaskToBeDeleted!=null){
            listOfStandardTasks.remove(taskToBeDeleted);
        }else if(subtaskToBeDeleted!=null){
            Epic epicToBeUnLinkedWithDeletedSubtask = listOfEpics.get(subtaskToBeDeleted.getParentID());
            ArrayList<Integer> listOfEpicsSubtasks = epicToBeUnLinkedWithDeletedSubtask.getListOfSubtasks();
            listOfEpicsSubtasks.remove(subtaskToBeDeleted.getId());
            epicToBeUnLinkedWithDeletedSubtask.setListOfTasks(listOfEpicsSubtasks);
            listOfEpics.put(epicToBeUnLinkedWithDeletedSubtask.getId(), epicToBeUnLinkedWithDeletedSubtask);
            listOfSubtasks.remove(taskToBeDeleted);
        }else if(epicToBeDeleted!=null){
            ArrayList<Integer> listOfSubtasksToBeDeleted;
            listOfSubtasksToBeDeleted = epicToBeDeleted.getListOfSubtasks();
            for(int i: listOfSubtasksToBeDeleted){
                listOfSubtasks.remove(i);
            }
            listOfEpics.remove(taskToBeDeleted);
        }
    }
    @Override
    public void deleteAll(){
        for (int i = 0; i < listOfEpics.size(); i++) {
            delete(i);
        }
        for (int i = 0; i < listOfStandardTasks.size(); i++) {
            delete(i);
        }
    }

    @Override
    public void update(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus) {
        StandardTask standardtaskToBeUpdated = listOfStandardTasks.get(taskID);
        Epic epicToBeUpdated = listOfEpics.get(taskID);
        SubTask subtaskToBeUpdated = listOfSubtasks.get(taskID);
        if(standardtaskToBeUpdated!=null){
            standardtaskToBeUpdated.setDetails(taskNewDetails);
            standardtaskToBeUpdated.setTaskStatus(taskNewStatus);
            listOfStandardTasks.put(taskID, standardtaskToBeUpdated);
        }else if(subtaskToBeUpdated!=null){
            subtaskToBeUpdated.setDetails(taskNewDetails);
            subtaskToBeUpdated.setTaskStatus(taskNewStatus);
            listOfSubtasks.put(taskID, subtaskToBeUpdated);
            recalculateOrUpdateTaskStatus();
        }else if(epicToBeUpdated!=null){
            epicToBeUpdated.setDetails(taskNewDetails);
            listOfEpics.put(taskID, epicToBeUpdated);
        }
    }

    @Override
    public Task getTask(Integer taskToBeDisplayedByID) {
        Task task = listOfStandardTasks.get(taskToBeDisplayedByID);
        if(task==null){
            task = listOfEpics.get(taskToBeDisplayedByID);
            if(task==null){
                task = listOfSubtasks.get(taskToBeDisplayedByID);
            }
        }
        if(task != null) {
            managerForHistory.add(task);
        }
        return task;
    }
    @Override
    public ArrayList<SubTask> getSubTasksOfEpic(int epicID) {
        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        ArrayList<Integer> listOfSubtasksIDs;
        Epic epicToGetList = listOfEpics.get(epicID);//Получаем данные выбранного эпика
        listOfSubtasksIDs = epicToGetList.getListOfSubtasks();//Забираем из эпика список его сабтасков
        if(listOfSubtasksIDs!=null) {
            for (Integer i : listOfSubtasksIDs) {//Идём по списку сабтасков выбранного эпика
                SubTask subtaskToCheckTheirParentId = listOfSubtasks.get(i);
                listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);//Наполняем список сабтасков
            }
        }
        return listOfSubtasksForEPIC;
    }

    @Override
    public ArrayList<Task> getHistory(){
        return managerForHistory.getHistory();
    }
    @Override
    public ArrayList<Task> getAll(String typeToList){
        ArrayList<Task> result = new ArrayList<>();
        switch(typeToList){
            case "Tasks":
                for(Task task: listOfStandardTasks.values()){
                    result.add(task);
                }
                break;
            case "Epics":
                for(Task task: listOfEpics.values()){
                    result.add(task);
                }
                break;
            case "Subtasks":
                for(Task task: listOfSubtasks.values()){
                    result.add(task);
                }
                break;
            case "All":
                for(Task task: listOfStandardTasks.values()){
                    result.add(task);
                }
                for(Task task: listOfEpics.values()){
                    result.add(task);
                }
                for(Task task: listOfSubtasks.values()){
                    result.add(task);
                }
                break;
        }
        return result;
    }
    private void recalculateOrUpdateTaskStatus() {
        for (Integer i : listOfEpics.keySet()) {//Проходим по каждому эпику
            Epic currentRecalculatedEpic = listOfEpics.get(i);
            boolean isTaskDone = true;
            boolean isTaskNew = true;
            ArrayList<Integer> listOfEpicSubtasks = listOfEpics.get(i).getListOfSubtasks();//Тут будут ID-шники подзадач текущего эпика
            ArrayList<TaskStatus> idsOfSubtasksForCurrentEpic = new ArrayList<>();//Тут будут статусы подзадач текущего эпика
            for (Integer j : listOfEpicSubtasks) {//В цикле читаем статусы подзадач текущей родительской задачи для расчёта статуса родительской задачи
                SubTask currentSubtaskToCalculateStatus = listOfSubtasks.get(j);
                idsOfSubtasksForCurrentEpic.add(currentSubtaskToCalculateStatus.getTaskStatus());//Добавляем в список значения статуса у очередной подзадачи
            }
            int numberOfNew = 0; //Кол-во подзадач статуса New
            int numberOfDone = 0; //Кол-во подзадач статуса DONE
            for (int k = 0; k < listOfEpicSubtasks.size(); k++) {//Идём по циклу выявленных подзадач нашей родительской задачи и сравниваем статусы, одновременно их подсчитывая
                if (idsOfSubtasksForCurrentEpic.get(k).equals(TaskStatus.NEW)) {
                    numberOfNew++;
                }
                if (idsOfSubtasksForCurrentEpic.get(k).equals(TaskStatus.DONE)) {
                    numberOfDone++;
                }
            }
            if (numberOfNew == listOfEpicSubtasks.size()) {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.NEW);
                listOfEpics.put(i, currentRecalculatedEpic);
            } else {
                isTaskNew = false;
            }
            if (numberOfDone == listOfEpicSubtasks.size()) {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.DONE);
                listOfEpics.put(i, currentRecalculatedEpic);
            } else {
                isTaskDone = false;
            }
            if (!isTaskNew && !isTaskDone) {//Если статус не NEW и не DONE, значит задача пока в состоянии IN_PROGRESS
                currentRecalculatedEpic.setTaskStatus(TaskStatus.IN_PROGRESS);
                listOfEpics.put(i, currentRecalculatedEpic);//Сохраняем вычисленное значение родительской задачи
            }
        }
    }
}
