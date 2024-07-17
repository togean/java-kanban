package controller;

import models.Epic;
import models.SubTask;
import models.Task;
import models.TaskStatus;
import models.StandardTask;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, StandardTask> listOfStandardTasks = new HashMap<>();
    private final HashMap<Integer, SubTask> listOfSubtasks = new HashMap<>();
    private final HashMap<Integer, Epic> listOfEpics = new HashMap<>();
    private Integer taskID = 1;

    HistoryManager managerForHistory = Managers.getDefaultHistory();

    taskComparator taskComporator = new taskComparator();//Компоратор для сравнения задачек по времени их начала
    private final TreeSet<Task> sortedListOfTasksByDateTime = new TreeSet<>(taskComporator);//Создаём сортированный список всех задачек

    public TreeSet<Task> getPrioritizedTasks(){
        sortedListOfTasksByDateTime.clear();//Что бы небыло дубляжа при создании отсортированного списка, очищаем старое наполнение
        sortedListOfTasksByDateTime.addAll(listOfStandardTasks.values());
        sortedListOfTasksByDateTime.addAll(listOfSubtasks.values());
        sortedListOfTasksByDateTime.addAll(listOfEpics.values());
        return this.sortedListOfTasksByDateTime;
    }

    public List<SubTask> getListOfSubTasks() {
        return new ArrayList<>(listOfSubtasks.values());
    }

    public List<Epic> getListOfEpics() {
        return new ArrayList<>(listOfEpics.values());
    }

    public List<StandardTask> getListOfStandardTasks() {
        return new ArrayList<>(listOfStandardTasks.values());
    }

    public Integer getTaskID() {
        return taskID;
    }

    @Override
    public Integer createTask(StandardTask taskToBeCreated) {
        int createdTaskID = 0;
        if(!checkTasksOverlapping(taskToBeCreated)) {
            taskToBeCreated.setId(taskID);
            listOfStandardTasks.put(taskID, taskToBeCreated);
            createdTaskID = taskID;
            taskID++;
        }else{
            System.out.println("Обнаружено пересечение с другой задачей");
        }
        return createdTaskID;
    }

    @Override
    public Integer createSubtask(SubTask taskToBeCreated) {
        int createdTaskID = 0;
        if(!checkTasksOverlapping(taskToBeCreated)) {
            Integer subtaskID = taskID;
            taskToBeCreated.setId(subtaskID);
            int parentID = taskToBeCreated.getParentID();
            if (!listOfSubtasks.containsKey(parentID)) {
                Epic epicToBeLinkedWithSubtask = listOfEpics.get(taskToBeCreated.getParentID());
                if (epicToBeLinkedWithSubtask != null) {
                    listOfSubtasks.put(subtaskID, taskToBeCreated);
                    ArrayList<Integer> listOfEpicsSubtasksToBeUpdated = epicToBeLinkedWithSubtask.getListOfSubtasks();
                    listOfEpicsSubtasksToBeUpdated.add(subtaskID);
                    epicToBeLinkedWithSubtask.setListOfTasks(listOfEpicsSubtasksToBeUpdated);
                    listOfEpics.put(taskToBeCreated.getParentID(), epicToBeLinkedWithSubtask);
                    createdTaskID = taskID;
                    taskID++;
                }
                recalculateOrUpdateTaskStatus();
            }
        }else{
            System.out.println("Обнаружено пересечение с другой задачей");
        }
        return createdTaskID;
    }

    @Override
    public Integer createEpic(Epic taskToBeCreated) {
        int createdTaskID =0;
        if(!checkTasksOverlapping(taskToBeCreated)) {
            taskToBeCreated.setId(taskID);
            listOfEpics.put(taskID, taskToBeCreated);
            createdTaskID = taskID;
            taskID++;
        }else{
            System.out.println("Обнаружено пересечение с другой задачей");
        }
        return createdTaskID;
    }

    @Override
    public void deleteSubtask(Integer taskToBeDeleted) {
        SubTask subtaskToBeDeleted = listOfSubtasks.get(taskToBeDeleted);
        if (subtaskToBeDeleted != null) {
            Epic epicToBeUnLinkedWithDeletedSubtask = listOfEpics.get(subtaskToBeDeleted.getParentID());
            ArrayList<Integer> listOfEpicsSubtasks = epicToBeUnLinkedWithDeletedSubtask.getListOfSubtasks();
            listOfEpicsSubtasks.remove(subtaskToBeDeleted.getId());
            epicToBeUnLinkedWithDeletedSubtask.setListOfTasks(listOfEpicsSubtasks);
            listOfEpics.put(epicToBeUnLinkedWithDeletedSubtask.getId(), epicToBeUnLinkedWithDeletedSubtask);
            listOfSubtasks.remove(taskToBeDeleted);
            recalculateOrUpdateTaskStatus();
            managerForHistory.remove(taskToBeDeleted);
            sortedListOfTasksByDateTime.remove(subtaskToBeDeleted);
        }
    }

    @Override
    public void deleteTask(Integer taskToBeDeleted) {
        StandardTask standardtaskToBeDeleted = listOfStandardTasks.get(taskToBeDeleted);
        if (standardtaskToBeDeleted != null) {
            listOfStandardTasks.remove(taskToBeDeleted);
            managerForHistory.remove(taskToBeDeleted);
            sortedListOfTasksByDateTime.remove(standardtaskToBeDeleted);
        }
    }

    @Override
    public void deleteEpic(Integer taskToBeDeleted) {
        Epic epicToBeDeleted = listOfEpics.get(taskToBeDeleted);
        if (epicToBeDeleted != null) {
            ArrayList<Integer> listOfSubtasksToBeDeleted;
            listOfSubtasksToBeDeleted = epicToBeDeleted.getListOfSubtasks();
            if (listOfSubtasksToBeDeleted != null) {
                for (int i : listOfSubtasksToBeDeleted) {
                    sortedListOfTasksByDateTime.remove(listOfSubtasks.get(i));
                    listOfSubtasks.remove(i);
                    managerForHistory.remove(taskToBeDeleted);
                }
            }
            listOfEpics.remove(taskToBeDeleted);
            managerForHistory.remove(taskToBeDeleted);
            sortedListOfTasksByDateTime.remove(epicToBeDeleted);
        }
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
        if (standardtaskToBeUpdated != null) {
            standardtaskToBeUpdated.setDetails(taskNewDetails);
            standardtaskToBeUpdated.setTaskStatus(taskNewStatus);
            listOfStandardTasks.put(taskID, standardtaskToBeUpdated);
        }
    }

    @Override
    public void updateSubtask(Integer subtaskID, String subtaskNewDetails, TaskStatus subtaskNewStatus) {
        SubTask subtaskToBeUpdated = listOfSubtasks.get(subtaskID);
        if (subtaskToBeUpdated != null) {
            subtaskToBeUpdated.setDetails(subtaskNewDetails);
            subtaskToBeUpdated.setTaskStatus(subtaskNewStatus);
            listOfSubtasks.put(subtaskID, subtaskToBeUpdated);
            recalculateOrUpdateTaskStatus();
        }
    }

    @Override
    public void updateEpic(Integer epicID, String epicNewDetails, TaskStatus epicNewStatus) {
        Epic epicToBeUpdated = listOfEpics.get(epicID);
        if (epicToBeUpdated != null) {
            epicToBeUpdated.setDetails(epicNewDetails);
            listOfEpics.put(epicID, epicToBeUpdated);
        }
    }

    @Override
    public StandardTask getTask(Integer taskToBeDisplayedByID) {
        StandardTask task = listOfStandardTasks.get(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public SubTask getSubTask(Integer taskToBeDisplayedByID) {
        SubTask task = listOfSubtasks.get(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(Integer taskToBeDisplayedByID) {
        Epic task = listOfEpics.get(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public ArrayList<SubTask> getSubTasksOfEpic(int epicID) {
        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        ArrayList<Integer> listOfSubtasksIDs;
        Epic epicToGetList = listOfEpics.get(epicID);
        if (epicToGetList != null) {
            listOfSubtasksIDs = epicToGetList.getListOfSubtasks();
            if (listOfSubtasksIDs != null) {
                for (Integer i : listOfSubtasksIDs) {
                    SubTask subtaskToCheckTheirParentId = listOfSubtasks.get(i);
                    listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);
                }

            }
        }
        return listOfSubtasksForEPIC;
    }

    @Override
    public List<Task> getHistory() {
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
        for (Integer i : listOfEpics.keySet()) {
            Epic currentRecalculatedEpic = listOfEpics.get(i);
            int numberOfNew = 0; //Кол-во подзадач статуса New
            int numberOfDone = 0; //Кол-во подзадач статуса DONE
            ArrayList<Integer> listOfEpicSubtasks = currentRecalculatedEpic.getListOfSubtasks();//Тут будут ID-шники подзадач текущего эпика
            LocalDateTime epicStartDateTime = currentRecalculatedEpic.getStartDateTime(); //на основе подзадач будем определять время старта эпика - это будет время самой ранней его подзадачи
            LocalDateTime epicEndDateTime = currentRecalculatedEpic.calculateTaskEndDateTime();
            Duration epicDuration = Duration.ofMinutes(0);//Начинаем с нуля, на основе длительностей подзадач будем считать длительность всего эпика
            boolean firstStartOfLoop = true;//временная переменная, что бы в начале цикла эпику назначить дату его старта датой первой обрабатываемой задачи. Для других уже будет сравнение
            for (int j : currentRecalculatedEpic.getListOfSubtasks()) {
                SubTask currentSubtaskToCalculateStatus = listOfSubtasks.get(j);
                LocalDateTime subtaskStartDateTime = currentSubtaskToCalculateStatus.getStartDateTime();
                LocalDateTime subtaskEndDateTime = currentSubtaskToCalculateStatus.calculateTaskEndDateTime();
                Duration subTaskDuration = currentSubtaskToCalculateStatus.getDuration();

                if (firstStartOfLoop) {//При первой итерации присваеваем эпику дату его начала равной дате начала первой взятой его задачи. Для всех остальных подзадач будет уже сравнение по дате
                    epicStartDateTime = subtaskStartDateTime;
                    epicEndDateTime = subtaskEndDateTime;
                    firstStartOfLoop = false;
                }
                if(subtaskEndDateTime.isAfter(epicEndDateTime)){
                    epicEndDateTime = subtaskEndDateTime;//Если подзадача оканчивается позже конечнодаты эпика, то меняем дату окончания эпика на дату окончания подзадачи
                }
                if (subtaskStartDateTime.isBefore(epicStartDateTime)) {//Если подзадача начинается ранее, чем имеющееся ачало у эпика, то обновляем время начала
                    epicStartDateTime = subtaskStartDateTime;
                }
                epicDuration = epicDuration.plus(subTaskDuration);//Увеличиваем длительность эпика на длительность входящей в его состав подзадачи

                if ((currentSubtaskToCalculateStatus.getTaskStatus()).equals(TaskStatus.NEW)) {
                    numberOfNew++;
                }
                if ((currentSubtaskToCalculateStatus.getTaskStatus()).equals(TaskStatus.DONE)) {
                    numberOfDone++;
                }

            }
            currentRecalculatedEpic.setStartDateTime(epicStartDateTime);//Записываем в эпик дату старта
            currentRecalculatedEpic.setDuration(epicDuration);//Записываем в эпик длительность
            currentRecalculatedEpic.setEndTime(epicEndDateTime);//Записываем в эпик время окончания
            if (numberOfNew == listOfEpicSubtasks.size()) { //Тут -1 т.к. при инициализации эпика перое значение в списке подзадач 0 (но 0 не используется, все ID начинаются с 1)
                currentRecalculatedEpic.setTaskStatus(TaskStatus.NEW);
                listOfEpics.put(i, currentRecalculatedEpic);
            } else if (numberOfDone == listOfEpicSubtasks.size()) {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.DONE);
                listOfEpics.put(i, currentRecalculatedEpic);
            } else {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.IN_PROGRESS);
                listOfEpics.put(i, currentRecalculatedEpic);//Сохраняем вычисленное значение родительской задачи
            }
        }
    }

    public boolean checkTasksOverlapping(Task taskToCheck){
        boolean result = false;
        for(Task taskToCompare: sortedListOfTasksByDateTime){
            if(taskToCheck.getStartDateTime().isBefore(taskToCompare.getStartDateTime()) && taskToCheck.calculateTaskEndDateTime().isAfter(taskToCompare.getStartDateTime())){
                result = true;
            }
            if(taskToCheck.getStartDateTime().isBefore(taskToCompare.getStartDateTime()) && taskToCheck.calculateTaskEndDateTime().isAfter(taskToCompare.calculateTaskEndDateTime())){
                result = true;
            }
            if(taskToCompare.calculateTaskEndDateTime().isAfter(taskToCheck.getStartDateTime()) && taskToCompare.getStartDateTime().isBefore(taskToCheck.getStartDateTime())){
                result = true;
            }
        }
        return result;
    }
}
class taskComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        boolean comparation = o1.getStartDateTime().isBefore(o2.getStartDateTime());
        int result = 0;
        if (comparation) {
            result = -1;
        } else {
            result = 1;
        }
        return result;
    }
}
