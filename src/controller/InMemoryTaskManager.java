package controller;

import models.Epic;
import models.SubTask;
import models.Task;
import models.TaskStatus;
import models.StandardTask;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, StandardTask> listOfStandardTasks = new HashMap<>();
    private final HashMap<Integer, SubTask> listOfSubtasks = new HashMap<>();
    private final HashMap<Integer, Epic> listOfEpics = new HashMap<>();
    private Integer taskID = 1;

    HistoryManager managerForHistory = Managers.getDefaultHistory();

    TaskComparator taskComporator = new TaskComparator();//Компоратор для сравнения задачек по времени их начала
    private final TreeSet<Task> sortedListOfTasksByDateTime = new TreeSet<>(taskComporator);//Создаём сортированный список всех задачек

    public TreeSet<Task> getPrioritizedTasks() {
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
        if (!checkTasksOverlapping(taskToBeCreated)) {
            taskToBeCreated.setId(taskID);
            listOfStandardTasks.put(taskID, taskToBeCreated);
            createdTaskID = taskID;
            taskID++;
        } else {
            System.out.println("Обнаружено пересечение с другой задачей");
        }
        return createdTaskID;
    }

    @Override
    public Integer createSubtask(SubTask taskToBeCreated) {
        int createdTaskID = 0;
        if (!checkTasksOverlapping(taskToBeCreated)) {
            Integer subtaskID = taskID;
            taskToBeCreated.setId(subtaskID);
            int parentID = taskToBeCreated.getParentID();
            if (!listOfSubtasks.containsKey(parentID)) {
                Epic epicToBeLinkedWithSubtask = listOfEpics.get(taskToBeCreated.getParentID());
                if (epicToBeLinkedWithSubtask != null) {
                    listOfSubtasks.put(subtaskID, taskToBeCreated);
                    ArrayList<Integer> listOfEpicsSubtasksToBeUpdated = epicToBeLinkedWithSubtask.getListOfSubtasks();
                    listOfEpicsSubtasksToBeUpdated.add(subtaskID);
                    epicToBeLinkedWithSubtask.setListOfSubTasks(listOfEpicsSubtasksToBeUpdated);
                    listOfEpics.put(taskToBeCreated.getParentID(), epicToBeLinkedWithSubtask);
                    createdTaskID = taskID;
                    taskID++;
                }
                recalculateOrUpdateTaskStatus();
            }
        } else {
            System.out.println("Обнаружено пересечение с другой задачей");
        }
        return createdTaskID;
    }

    @Override
    public Integer createEpic(Epic taskToBeCreated) {
        int createdTaskID = 0;
        if (!checkTasksOverlapping(taskToBeCreated)) {
            taskToBeCreated.setId(taskID);
            listOfEpics.put(taskID, taskToBeCreated);
            createdTaskID = taskID;
            taskID++;
        } else {
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
            epicToBeUnLinkedWithDeletedSubtask.setListOfSubTasks(listOfEpicsSubtasks);
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
            listOfStandardTasks.remove(standardtaskToBeDeleted.getId());
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
                listOfSubtasksToBeDeleted.forEach(i -> {
                    sortedListOfTasksByDateTime.remove(listOfSubtasks.get(i));
                    listOfSubtasks.remove(i);
                    managerForHistory.remove(taskToBeDeleted);
                });
            }
            listOfEpics.remove(taskToBeDeleted);
            managerForHistory.remove(taskToBeDeleted);
            sortedListOfTasksByDateTime.remove(epicToBeDeleted);
        }
    }

    @Override
    public void deleteAll() {
        listOfEpics.forEach((number, epic) -> deleteEpic(number));
        listOfStandardTasks.forEach((number, task) -> deleteTask(number));
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
                listOfSubtasksIDs.forEach(i -> {
                    SubTask subtaskToCheckTheirParentId = listOfSubtasks.get(i);
                    listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);
                });
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
        result.addAll(listOfSubtasks.values());
        return result;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> result = new ArrayList<>();
        result.addAll(listOfEpics.values());
        return result;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> result = new ArrayList<>();
        result.addAll(listOfStandardTasks.values());
        return result;
    }

    private void recalculateOrUpdateTaskStatus() {
        listOfEpics.values().stream()
                .peek(epic -> {
                    int numberOfSubTasks = epic.getListOfSubtasks().size();
                    int listOfSubTasksWithStatusDONE = 0;
                    int listOfSubTasksWithStatusNEW = 0;
                    Optional<SubTask> subtaskWithEarlieStartTime = Optional.empty();
                    Optional<SubTask> subtaskWithLatestStartTime = Optional.empty();
                    for (int i : epic.getListOfSubtasks()) {
                        listOfSubTasksWithStatusDONE = listOfSubTasksWithStatusDONE + listOfSubtasks.values().stream()
                                .filter(subtask -> subtask.getId().equals(i))
                                .filter(subtask -> subtask.getTaskStatus().equals(TaskStatus.DONE))
                                .toList().size();
                        listOfSubTasksWithStatusNEW = listOfSubTasksWithStatusNEW + listOfSubtasks.values().stream()
                                .filter(subtask -> subtask.getId().equals(i))
                                .filter(subtask -> subtask.getTaskStatus().equals(TaskStatus.NEW))
                                .toList().size();
                        subtaskWithEarlieStartTime = listOfSubtasks.values().stream()
                                .min(taskComporator);
                        subtaskWithLatestStartTime = listOfSubtasks.values().stream()
                                .max(taskComporator);
                    }

                    if (subtaskWithEarlieStartTime.isPresent()) {
                        epic.setStartDateTime(subtaskWithEarlieStartTime.get().getStartDateTime());
                    }
                    if (subtaskWithLatestStartTime.isPresent()) {
                        epic.setEndTime(subtaskWithLatestStartTime.get().getStartDateTime().plusMinutes(subtaskWithLatestStartTime.get().getDuration().toMinutes()));
                    }
                    if (numberOfSubTasks == listOfSubTasksWithStatusDONE) {
                        epic.setTaskStatus(TaskStatus.DONE);
                    } else if (numberOfSubTasks == listOfSubTasksWithStatusNEW) {
                        epic.setTaskStatus(TaskStatus.NEW);
                    } else {
                        epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                    }
                })
                .collect(Collectors.toList());

    }

    public boolean checkTasksOverlapping(Task taskToCheck) {
        boolean result = false;
        for (Task taskToCompare : sortedListOfTasksByDateTime) {
            if (taskToCheck.getStartDateTime().isBefore(taskToCompare.getStartDateTime()) && taskToCheck.calculateTaskEndDateTime().isAfter(taskToCompare.getStartDateTime())) {
                result = true;
            }
            if (taskToCheck.getStartDateTime().isBefore(taskToCompare.getStartDateTime()) && taskToCheck.calculateTaskEndDateTime().isAfter(taskToCompare.calculateTaskEndDateTime())) {
                result = true;
            }
            if (taskToCompare.calculateTaskEndDateTime().isAfter(taskToCheck.getStartDateTime()) && taskToCompare.getStartDateTime().isBefore(taskToCheck.getStartDateTime())) {
                result = true;
            }
            if (taskToCompare.getStartDateTime().equals(taskToCheck.getStartDateTime()) && (!taskToCompare.getClass().equals(Epic.class)) && (!taskToCheck.getClass().equals(SubTask.class))) {
                result = true;
            }
        }
        return result;
    }
}
