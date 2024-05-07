package controller;

import models.Task;
import models.SubTask;
import models.Epic;
import models.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
public class TaskManager {
    private HashMap<Integer, Task> Tasks;
    private HashMap<Integer, SubTask> SubTasks;
    private HashMap<Integer, Epic> Epics;

    public TaskManager() {
        Tasks = new HashMap<>();
        SubTasks = new HashMap<>();
        Epics = new HashMap<>();
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(Tasks.values());
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(SubTasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(Epics.values());
    }

    public Integer createTask(Task taskToBeCreated) {
        int taskID = taskToBeCreated.hashCode();
        taskToBeCreated.setTaskIndex(taskID);
        taskToBeCreated.setTaskStatus(TaskStatus.NEW);
        Tasks.put(taskToBeCreated.getTaskIndex(), taskToBeCreated);
        return taskToBeCreated.getTaskIndex();
    }

    public Integer createEpic(Epic epicToBeCreated) {
        int epicID = epicToBeCreated.hashCode();
        epicToBeCreated.setTaskIndex(epicID);
        epicToBeCreated.setTaskStatus(TaskStatus.NEW);
        Epics.put(epicToBeCreated.getTaskIndex(), epicToBeCreated);
        return epicToBeCreated.getTaskIndex();
    }

    public Integer createSubTask(SubTask taskToBeCreated) {
        int subTaskID = taskToBeCreated.hashCode();
        taskToBeCreated.setTaskIndex(subTaskID);
        taskToBeCreated.setTaskStatus(TaskStatus.NEW);
        SubTasks.put(taskToBeCreated.getTaskIndex(), taskToBeCreated);
        //Добавляем подзадачу в список подзадач эпика (В MAIN сечас родитель УКАЗАН РУКАМИ!!!!), если ID родителя в подзадаче не равен 0
        if(taskToBeCreated.getParentTaskID()>0) {
            ArrayList<Integer> listOfEpicsSubTasks = new ArrayList<>();
            Epic epicToBeUpdated = Epics.get(taskToBeCreated.getParentTaskID());
            if(Epics.get(taskToBeCreated.getParentTaskID()).getEpicSubtasks().size()==0){//Если у эпика ещё небыло подзадач (что бы не попасть на NullPointerException)
                listOfEpicsSubTasks.add(taskToBeCreated.getTaskIndex());
            }else{
                listOfEpicsSubTasks = Epics.get(taskToBeCreated.getParentTaskID()).getEpicSubtasks();//У эпика уже были подзадачи, то расширяем список
                listOfEpicsSubTasks.add(taskToBeCreated.getTaskIndex());
            }
            epicToBeUpdated.setEpicSubtasks(listOfEpicsSubTasks);
            updateEpic(epicToBeUpdated);//Обновляем список подзадаач для эпика
            recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков, так как мы изменили эпик
        }
        return taskToBeCreated.getTaskIndex();
    }

    public void updateTask(Task task) {
        int idOfTaskToBeUpdated = task.getTaskIndex();
        task.setTaskDescription(Tasks.get(idOfTaskToBeUpdated).getTaskDescription());//т.к. по сути перезаписываем таск, то сохраняем старый дескрипшен
        Tasks.put(idOfTaskToBeUpdated, task);
    }

    public void updateEpic(Epic epicToUpdate) {
        int idOfEpicToBeUpdated = epicToUpdate.getTaskIndex();
        epicToUpdate.setTaskDescription(Epics.get(idOfEpicToBeUpdated).getTaskDescription());//т.к. по сути перезаписываем эпик, то сохраняем старый дескрипшен
        if(epicToUpdate.getEpicSubtasks().size()<Epics.get(idOfEpicToBeUpdated).getEpicSubtasks().size()){
            //Если список подзадач в обновляемом эпике меньше или равен, чем то, что было в эпике ранее
            // то берём список подзадач таким, как он был ранее в эпике (означает, что либо для пустого эпика создаётся подзадача, либо просто обновляется эпик (его details)
            // Если же входящий размер больше, значит список подзадач обновлён и старый список подзадач мы должны обновить списком, взятым из epicToUpdate
            epicToUpdate.setEpicSubtasks(Epics.get(idOfEpicToBeUpdated).getEpicSubtasks());//т.к. по сути перезаписываем эпик, то сохраняем связи с подзадачами
         }
        Epics.put(idOfEpicToBeUpdated, epicToUpdate);
    }

    public void updateSubTask(SubTask subTask) {
        int idOfSubtaskToBeUpdated = subTask.getTaskIndex();
        subTask.setTaskDescription(SubTasks.get(idOfSubtaskToBeUpdated).getTaskDescription());//т.к. по сути перезаписываем подзадачу, то сохраняем старый дескрипшен
        subTask.setParentTaskID(SubTasks.get(idOfSubtaskToBeUpdated).getParentTaskID());//т.к. по сути перезаписываем подзадачу, то сохраняем старый индекс родителя
        SubTasks.put(idOfSubtaskToBeUpdated, subTask);
        recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков, так как мы изменили подзадачу
    }

    public void deleteTasks() {
        Tasks.clear();
    }
    public void deleteSubTasks() {
        for(Epic epicToClearSubtasks : Epics.values()){
            ArrayList<Integer> listOfEpicSubtasks = new ArrayList<>();
            epicToClearSubtasks.setEpicSubtasks(listOfEpicSubtasks);
            System.out.println("Теперь список подзадач в эпике: "+epicToClearSubtasks.getEpicSubtasks());
            updateEpic(epicToClearSubtasks);
        }

        SubTasks.clear();
    }
    public void deleteEpics() {
        Epics.clear();
        SubTasks.clear();
    }
    public void deleteTask(Integer taskIDToDelete) {
        Tasks.remove(taskIDToDelete);
    }

    public void deleteEpic(Integer epicIDToDelete) {
        ArrayList<Integer> listOfSubtasksToBeDeleted;
        Epic epicToBeDeleted = Epics.get(epicIDToDelete);
        listOfSubtasksToBeDeleted = epicToBeDeleted.getEpicSubtasks();
        for(int i: listOfSubtasksToBeDeleted){
            SubTasks.remove(i);
        }
        Epics.remove(epicIDToDelete);
    }
    public void deleteSubTask(Integer subtaskIDToDelete) {
        ArrayList<Integer> listOfSubtasksInEpic;
        SubTask subtaskToBeDeleted = SubTasks.get(subtaskIDToDelete);
        Epic epicToUpdate = Epics.get(subtaskToBeDeleted.getParentTaskID());
        listOfSubtasksInEpic = epicToUpdate.getEpicSubtasks();
        for(Integer i: listOfSubtasksInEpic){
            if(i.equals(subtaskIDToDelete)){
                listOfSubtasksInEpic.remove(i);
                break;
            }
        }
        epicToUpdate.setEpicSubtasks(listOfSubtasksInEpic);
        Epics.put(epicToUpdate.getTaskIndex(), epicToUpdate);//Сохраняем в эпике обновлённый список подзадач
        SubTasks.remove(subtaskIDToDelete);
        recalculateOrUpdateTaskStatus();//Обновляем статусы эпиков
    }
    public ArrayList<SubTask> getSubTasksOfEpic(int epicID) {
        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        ArrayList<Integer> listOfSubtasksIDs;
        Epic epicToGetList = Epics.get(epicID);//Получаем данные выбранного эпика
        listOfSubtasksIDs = epicToGetList.getEpicSubtasks();//Забираем из эпика список его сабтасков
        for (Integer i : listOfSubtasksIDs) {//Идём по списку сабтасков выбранного эпика
            SubTask subtaskToCheckTheirParentId = SubTasks.get(i);
            listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);//Наполняем список сабтасков
        }
        return listOfSubtasksForEPIC;
    }

    private void recalculateOrUpdateTaskStatus() {
        for (Integer i : Epics.keySet()) {//Проходим по каждой задаче
            Epic currentRecalculatedEpic = Epics.get(i);
            boolean isTaskDone = true;
            boolean isTaskNew = true;
            ArrayList<Integer> listOfEpicSubtasks = Epics.get(i).getEpicSubtasks();//Тут будут ID-шники подзадач текущего эпика
            ArrayList<TaskStatus> idsOfSubtasksForCurrentEpic = new ArrayList<>();//Тут будут статусы подзадач текущего эпика
            for (Integer j : listOfEpicSubtasks) {//В цикле читаем статусы подзадач текущей родительской задачи для расчёта статуса родительской задачи
                SubTask currentSubtaskToCalculateStatus = SubTasks.get(j);
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
                Epics.put(i, currentRecalculatedEpic);
            } else {
                isTaskNew = false;
            }
            if (numberOfDone == listOfEpicSubtasks.size()) {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.DONE);
                Epics.put(i, currentRecalculatedEpic);
            } else {
                isTaskDone = false;
            }
            if (!isTaskNew && !isTaskDone) {//Если статус не NEW и не DONE, значит задача пока в состоянии IN_PROGRESS
                currentRecalculatedEpic.setTaskStatus(TaskStatus.IN_PROGRESS);
                Epics.put(i, currentRecalculatedEpic);//Сохраняем вычисленное значение родительской задачи
            }
        }
    }
}
