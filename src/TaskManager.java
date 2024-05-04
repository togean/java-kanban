import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;
import java.util.HashMap;
public class TaskManager {
    private HashMap<Integer, Task> myTasks;
    private HashMap<Integer, SubTask> mySubTasks;
    private HashMap<Integer, Epic> myEpics;

    public TaskManager() {
        myTasks = new HashMap<>();
        mySubTasks = new HashMap<>();
        myEpics = new HashMap<>();
    }

    public void getAllTasks() {
        System.out.println("Перечень всех задач (" + myTasks.size() + "):");
        for (Task tasksToShow : myTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
    }

    public void getAllSubTasks() {
        System.out.println("Перечень всех подзадач (" + mySubTasks.size() + "):");
        for (Task tasksToShow : mySubTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
    }

    public void getAllEpics() {
        System.out.println("Перечень всех эпиков (" + myEpics.size() + "):");
        for (Epic epicToShow : myEpics.values()) {
            System.out.println(epicToShow.toString());
        }
    }

    public void deleteAllTasks() {
        myTasks.clear();
        mySubTasks.clear();
        myEpics.clear();
    }

    public void deleteSubTask(Integer subtaskIDToDelete) {
        mySubTasks.remove(subtaskIDToDelete);
        recalculateOrUpdateTaskStatus();//Обновляем статусы эпиков

    }

    public void createTask(String taskDescription, String taskDetails) {
        //Сначала проверяем, нет ли уже задача с таким description в списке задач. Если нет, то создать можно. А если нет - то проверяем в эпиках - там тоже совпадения быть недолжно
        boolean hasDublicate = false;
        Task compare;
        for (Integer i : myTasks.keySet()) {
            compare = myTasks.get(i);
            if (compare.getTaskDescription().equals(taskDescription)) {
                hasDublicate = true;
            }
        }
        for (Integer i : myEpics.keySet()) {
            compare = myEpics.get(i);
            if (compare.getTaskDescription().equals(taskDescription)) {
                hasDublicate = true;
            }
        }
        if (!hasDublicate) {
            Task newTask = new Task(taskDescription, taskDetails, TaskStatus.NEW);
            newTask.setTaskIndex(newTask.hashCode());
            myTasks.put(newTask.hashCode(), newTask);
         }
    }

    public void createEpic(String epicDescription, String epicDetails) {
        boolean hasDublicate = false;
        for (Integer i : myEpics.keySet()) {
            Task compare = myEpics.get(i);
            if (compare.getTaskDescription().equals(epicDescription)) {
                hasDublicate = true;
            }
        }
        if (!hasDublicate) {
            Epic newEpic = new Epic(epicDescription, epicDetails, TaskStatus.NEW);
            newEpic.setTaskIndex(newEpic.hashCode());
            myEpics.put(newEpic.hashCode(), newEpic);
        }
    }

    public void createSubTask(String taskDescription, String taskDetails, String parentTaskDescription) {
        //В нашем трекере пользователь пытается создать подзадачу, а значит, нам надо найти родительскую по описанию данной поздадачи
        Integer parentTaskID = -1;
        for (Integer i : myTasks.keySet()) { //В цикле ищем совпадения по описанию родительской задачи сначала в задачах, по после поищем в эпиках, если в задачах она не найдётся
            Task taskToCompare = myTasks.get(i);
            if (taskToCompare.getTaskDescription().equals(parentTaskDescription)) {
                //Родительская задача найдена в задачах
                //Надо перенести родительскую задачу в эпики c обновлением индекса, так как при создании эпика индекс изменится
                Epic newEpic = new Epic(taskToCompare.getTaskDescription(), taskToCompare.getTaskDetails(), TaskStatus.NEW);//Создаём из задачи новый эпик
                newEpic.setTaskIndex(newEpic.hashCode());
                myEpics.put(newEpic.hashCode(), newEpic);   //Сохраняем новый эпик
                parentTaskID = newEpic.hashCode();
                myTasks.remove(i);//Удаляем уже не нужную задачу

                break;
            }
        }
        if (parentTaskID < 0) {//Если в задачх родителя не нашли, пробуем его найти в эпиках
            for (Integer i : myEpics.keySet()) { //В цикле ищем совпадения по описанию родительской задачи сначала в задачах, по после поищем в эпиках, если в задачах она не найдётся
                Epic epicToCompare = myEpics.get(i);
                if (epicToCompare.getTaskDescription().equals(parentTaskDescription)) {
                    parentTaskID = epicToCompare.getTaskIndex();//Родительская задача найдена в эпиках
                    break;
                }
            }
        }
        if (parentTaskID >= 0) {//Если родитель найден, то выполняем проверку на возможность создания подзадачи
            boolean canCreateSubTask = true;
            for (Integer i : mySubTasks.keySet()) {//В цикле проверяем есть ли такая подзадача в списке подзадач - так как подзадача могла быть у другого родителя. Предполагается, что у разных родителей могут быть одинаковые подзадачи
                SubTask taskToCompare = mySubTasks.get(i);
                if (taskToCompare.getTaskDescription().equals(taskDescription)) {
                    canCreateSubTask = false;//Обнаружили такую подзадачу - сразу не можем создать подзадачу, т.к. надо проверить, у того же она родителя или у другого
                    break;
                }
            }
            if (canCreateSubTask) {//Если нашей подзадачи ранее небыло, то можем её просто создать
                SubTask newSubTask = new SubTask(taskDescription, taskDetails, TaskStatus.NEW, TasksType.SUBTASK, parentTaskID);
                newSubTask.setTaskIndex(newSubTask.hashCode());
                mySubTasks.put(newSubTask.hashCode(), newSubTask);

                recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков
            } else {//Если же подзадача уже была - надо проверить, совпадают ли ID родителя. Если совпадают - не можем создать подзадачу, так как у одного родителя не может быть две одинаковых подзадачи
                for (Integer i : mySubTasks.keySet()) {//Смотрим в подзадачах, нашу подзадачу и проверяем её родительский ID
                    SubTask subtaskToCompare = mySubTasks.get(i);
                    if (!subtaskToCompare.getParentTaskID().equals(parentTaskID)) {//Если ID НЕ совпадают, то можем создать подзадачу
                        SubTask newSubTask = new SubTask(taskDescription, taskDetails, TaskStatus.NEW, TasksType.SUBTASK, parentTaskID);
                        newSubTask.setTaskIndex(newSubTask.hashCode());
                        mySubTasks.put(newSubTask.hashCode(), newSubTask);
                    }
                }
            }
        }
    }

    public void updateTask(Task task) {
        Integer taskID = -1;
        for (Integer i : myTasks.keySet()) {//Что бы обновить задачу, надо её найти в мапе и узнать её ID
            Task taskToSetID = myTasks.get(i);
            if (taskToSetID.getTaskDescription().equals(task.getTaskDescription())) {
                taskID = taskToSetID.getTaskIndex();//Нашли ID нашей задачи, подлежащей обновлению
                break;
            }
        }
        if (taskID >= 0) {
            task.setTaskIndex(taskID);
            myTasks.put(taskID, task);
        }
    }

    public void updateEpic(Epic epicToUpdate) {
        Integer epicID = -1;
        for (Integer i : myEpics.keySet()) {//Что бы обновить эпик, надо его найти в мапе и узнать его ID
            Epic epicToSetID = myEpics.get(i);
            if (epicToSetID.getTaskDescription().equals(epicToUpdate.getTaskDescription())) {
                epicID = epicToSetID.getTaskIndex();//Нашли ID нашего эпика, подлежащей обновлению
                break;
            }
        }
        if (epicID >= 0) {
            epicToUpdate.setTaskIndex(epicID);
            myEpics.put(epicID, epicToUpdate);
        }
    }

    public void updateSubTask(SubTask subTask) {
        Integer subtaskID = -1;
        Integer parentID = 0;//Будем выявлять связь с родителем, так как в переданной subTask нет родительского ID
        for (Integer i : mySubTasks.keySet()) {//Что бы обновить подзадачу, надо её найти в мапе и узнать её ID
            SubTask subtaskToSetID = mySubTasks.get(i);
            if (subtaskToSetID.getTaskDescription().equals(subTask.getTaskDescription())) {
                subtaskID = subtaskToSetID.getTaskIndex();//Нашли ID нашей подзадачи, подлежащей обновлению
                parentID = subtaskToSetID.getParentTaskID();
                break;
            }
        }
        if (subtaskID >= 0) {
            subTask.setTaskIndex(subtaskID);//Выставили правильное значение индекса
            subTask.setParentTaskID(parentID);//Выставили правильное значение индекса родителя
            mySubTasks.put(subtaskID, subTask);
            recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков, так как мы изменили подзадачу
        }

    }

    public void deleteTask(Integer taskIDToDelete) {
        myTasks.remove(taskIDToDelete);
    }

    public void deleteEpic(Integer epicIDToDelete) {
        boolean canDeleteEpic = true;//Флаг того, что у эпика больше нет подзадач и он может быть удалён
        for (Integer i : mySubTasks.keySet()) {//Проверяем, что подзадачи не ссылаются на этот эпик
            SubTask subtaskLinkedToEpic = mySubTasks.get(i);
            System.out.println("Проверяю ID "+subtaskLinkedToEpic.getParentTaskID());
            if (subtaskLinkedToEpic.getParentTaskID().equals(epicIDToDelete)) {
                System.out.println("Есть связь - не  могу удалить эпик");
                canDeleteEpic = false;
                break;
            }
        }
        if (canDeleteEpic) {
            myEpics.remove(epicIDToDelete);
        }
    }

    public ArrayList<SubTask> getSubTasksOfEpic(String epicDescription) {
        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        Integer epicParentIDs = -1;
        for (Integer i : myEpics.keySet()) {//Сначала нам надо найти ID эпика по его описанию
            Epic epicToExtractAllSubtasks = myEpics.get(i);
            if (epicToExtractAllSubtasks.getTaskDescription().equals(epicDescription)) {
                epicParentIDs = epicToExtractAllSubtasks.getTaskIndex();//Нашли ID эпика
                break;
            }
        }
        if (epicParentIDs >= 0) {//Если ID эпика нашли, то набираем ArrayList его подзадач
            for (Integer i : mySubTasks.keySet()) {//Сначала нам надо найти ID эпика
                SubTask subtaskToCheckTheirParentId = mySubTasks.get(i);
                if (subtaskToCheckTheirParentId.getParentTaskID().equals(epicParentIDs)) {
                    listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);
                }
            }
        }
        return listOfSubtasksForEPIC;
    }

    private void recalculateOrUpdateTaskStatus() {
        for (Integer i : myEpics.keySet()) {//Проходим по каждой задаче
            Epic currentRecalculatedEpic = myEpics.get(i);
            boolean isTaskDone = true;
            boolean isTaskNew = true;
            Integer numberOfSubtasks = 0;//Учёт сколько подзадач у задачи, что бы анализировать потом в цикле статусы
            ArrayList<TaskStatus> idsOfSubtasksForCurrentTask = new ArrayList<>();

            for (Integer j : mySubTasks.keySet()) {//В цикле читаем статусы подзадач текущей родительской задачи для расчёта статуса родительской задачи
                SubTask currentSubtaskToCalculateTask = mySubTasks.get(j);
                if (currentSubtaskToCalculateTask.getParentTaskID().equals(i)) {//Если у подзадачи родителем является текущая задача, то учитываем её статус в расчёте статуса родителя
                    idsOfSubtasksForCurrentTask.add(currentSubtaskToCalculateTask.getTaskStatus());//Добавляем в список значения статуса у очередной подзадачи
                    numberOfSubtasks++;//Считаем сколько подзадач, что бы потом организовать цикл подсчёта
                }
            }
            int numberOfNew = 0; //Кол-во подзадач статуса New
            int numberOfDone = 0; //Кол-во подзадач статуса DONE
            for (int k = 0; k < numberOfSubtasks; k++) {//Идём по циклу выявленных подзадач нашей родительской задачи и сравниваем статусы, одновременно их подсчитывая
                if (idsOfSubtasksForCurrentTask.get(k).equals(TaskStatus.NEW)) {
                    numberOfNew++;
                }
                if (idsOfSubtasksForCurrentTask.get(k).equals(TaskStatus.DONE)) {
                    numberOfDone++;
                }
            }
            if (numberOfNew == numberOfSubtasks) {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.NEW);
                myEpics.put(i, currentRecalculatedEpic);
            } else {
                isTaskNew = false;
            }
            if (numberOfDone == numberOfSubtasks) {
                currentRecalculatedEpic.setTaskStatus(TaskStatus.DONE);
                myEpics.put(i, currentRecalculatedEpic);
            } else {
                isTaskDone = false;
            }
            if (!isTaskNew && !isTaskDone) {//Если статус не NEW и не DONE, значит задача пока в состоянии IN_PROGRESS
                currentRecalculatedEpic.setTaskStatus(TaskStatus.IN_PROGRESS);
                myEpics.put(i, currentRecalculatedEpic);//Сохраняем вычисленное значение родительской задачи
            }
        }
    }
}
