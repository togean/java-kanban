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
        System.out.println("Перечень всех задач ("+myTasks.size()+"):");
        for (Task tasksToShow : myTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
    }

    public void getAllSubTasks() {
        System.out.println("Перечень всех подзадач ("+mySubTasks.size()+"):");
        for (Task tasksToShow : mySubTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
    }
    public void getAllEpics() {
        System.out.println("Перечень всех эпиков ("+myEpics.size()+"):");
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
        for(Integer i: myTasks.keySet()){
            Task compare = myTasks.get(i);
            if(compare.getTaskDescription().equals(taskDescription)){
                hasDublicate = true;
            }
        }
        if(!hasDublicate) {
            Task newTask = new Task(taskDescription, taskDetails, TaskStatus.NEW);
            Integer newTaskID = -1;
            myTasks.put(myTasks.size(), newTask);
            for (Integer i : myTasks.keySet()) {
                Task taskToSetID = myTasks.get(i);
                if (taskToSetID.getTaskDescription().equals(taskDescription)) {
                    newTaskID = i;
                    break;
                }
            }
            if (newTaskID >= 0) {
                newTask.setTaskIndex(newTaskID);
                updateTask(newTask);//Так как в конструкторе мы не знаем, какой по счёту у нас создаётся элемент, то обновляем запись, в которой прописываем ID-шник задачи
            }
        }
    }
    public void createEpic(String epicDescription, String epicDetails) {
        boolean hasDublicate = false;
        for(Integer i: myEpics.keySet()){
            Task compare = myEpics.get(i);
            if(compare.getTaskDescription().equals(epicDescription)){
                hasDublicate = true;
            }
        }
        if(!hasDublicate) {
            Epic newEpic = new Epic(epicDescription, epicDetails, TaskStatus.NEW);
            Integer newTaskID = -1;
            myEpics.put(myEpics.size(), newEpic);
            for (Integer i : myEpics.keySet()) {
                Task taskToSetID = myEpics.get(i);
                if (taskToSetID.getTaskDescription().equals(epicDescription)) {
                    newTaskID = i;
                    break;
                }
            }
            if (newTaskID >= 0) {
                newEpic.setTaskIndex(newTaskID);
                updateEpic(newEpic);//Так как в конструкторе мы не знаем, какой по счёту у нас создаётся элемент, то обновляем запись, в которой прописываем ID-шник эпика
            }
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
                    myEpics.put(myEpics.size(), newEpic);//Сохраняем новый эпик
                    myTasks.remove(i);//Удаляем уже не нужную задачу
                    Integer newEpicID=-1;
                    for(Integer j : myEpics.keySet()){//После создания эпика надо узнать какой ему присвоился ID, что бы далее прописать его полю TaskIndex эпика и далее связать его с подзадачей
                        Task taskToSetID = myEpics.get(j);
                        if(taskToSetID.getTaskDescription().equals(taskToCompare.getTaskDescription())){
                            newEpicID = j;//Выяснили какой ID был определён в HashMap для созданного эпика - его и будем далее использовать
                            break;
                        }
                    }
                    if(newEpicID>=0) {
                        newEpic.setTaskIndex(newEpicID);
                        parentTaskID = newEpicID;//Родительским ID для подзадачи будет ID ЭПИКА, а не задачи, т.к. задача теперь стала эпиком и получила новый ID
                        updateEpic(newEpic);
                    }
                    break;
                }
            }
            if(parentTaskID<0){//Если в задачх родителя не нашли, пробуем его найти в эпиках
                for (Integer i : myEpics.keySet()) { //В цикле ищем совпадения по описанию родительской задачи сначала в задачах, по после поищем в эпиках, если в задачах она не найдётся
                    Epic epicToCompare = myEpics.get(i);
                    if (epicToCompare.getTaskDescription().equals(parentTaskDescription)) {
                        parentTaskID = i;//Родительская задача найдена в эпиках
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
                    Integer newID = mySubTasks.size();
                    SubTask newSubTask = new SubTask(taskDescription, taskDetails, TaskStatus.NEW, TasksType.SUBTASK, parentTaskID);
                    mySubTasks.put(newID, newSubTask);
                    Integer newSubtaskID=-1;
                    for(Integer j : mySubTasks.keySet()){//После создания эпика надо узнать какой ему присвоился ID, что бы далее прописать его полю TaskIndex эпика и далее связать его с подзадачей
                        SubTask subtaskToSetID = mySubTasks.get(j);
                        if(subtaskToSetID.getTaskDescription().equals(taskDescription)){
                            newSubtaskID = j;//Выяснили какой ID был определён в HashMap для созданного эпика - его и будем далее использовать
                            break;
                        }
                    }
                    if(newSubtaskID>=0) {
                        newSubTask.setTaskIndex(newSubtaskID);
                        updateSubTask(newSubTask);//Так как в конструкторе мы не знаем, какой по счёту у нас создаётся элемент, то обновляем запись, в которой прописываем ID-шник подзадачи
                    }
                    recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков
                } else {//Если же подзадача уже была - надо проверить, совпадают ли ID родителя. Если совпадают - не можем создать подзадачу, так как у одного родителя не может быть две одинаковых подзадачи
                    for (Integer i : mySubTasks.keySet()) {//Смотрим в подзадачах, нашу подзадачу и проверяем её родительский ID
                        SubTask subtaskToCompare = mySubTasks.get(i);
                        if (subtaskToCompare.getParentTaskID() != parentTaskID) {//Если ID НЕ совпадают, то можем создать подзадачу
                            Integer newID = mySubTasks.size();
                            SubTask newSubTask = new SubTask(taskDescription, taskDetails, TaskStatus.NEW, TasksType.SUBTASK, parentTaskID);
                            mySubTasks.put(newID, newSubTask);
                        }
                    }
                }
            }
    }

    public void updateTask(Task task) {
        Integer taskID=-1;
        for(Integer i : myTasks.keySet()){//Что бы обновить задачу, надо её найти в мапе и узнать её ID
            Task taskToSetID = myTasks.get(i);
            if(taskToSetID.getTaskDescription().equals(task.getTaskDescription())){
                taskID = i;//Нашли ID нашей задачи, подлежащей обновлению
                break;
            }
        }
        if(taskID>=0) {
            task.setTaskIndex(taskID);
            myTasks.put(taskID, task);
        }
    }
    public void updateEpic(Epic epicToUpdate) {
        Integer epicID=-1;
        for(Integer i : myEpics.keySet()){//Что бы обновить эпик, надо его найти в мапе и узнать его ID
            Epic epicToSetID = myEpics.get(i);
            if(epicToSetID.getTaskDescription().equals(epicToUpdate.getTaskDescription())){
                epicID = i;//Нашли ID нашего эпика, подлежащей обновлению
                break;
            }
        }
        if(epicID>=0) {
            epicToUpdate.setTaskIndex(epicID);
            myEpics.put(epicID, epicToUpdate);
        }
    }
    public void updateSubTask(SubTask subTask) {
        Integer subtaskID=-1;
        Integer parentID=0;//Будем выявлять связь с родителем, так как в переданной subTask нет родительского ID
        for(Integer i : mySubTasks.keySet()){//Что бы обновить подзадачу, надо её найти в мапе и узнать её ID
            SubTask subtaskToSetID = mySubTasks.get(i);
            if(subtaskToSetID.getTaskDescription().equals(subTask.getTaskDescription())){
                subtaskID = i;//Нашли ID нашей подзадачи, подлежащей обновлению
                parentID = subtaskToSetID.getParentTaskID();
                break;
            }
        }
        if(subtaskID>=0) {
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
            if (subtaskLinkedToEpic.getParentTaskID() == epicIDToDelete) {
                canDeleteEpic = false;
                break;
            }
        }
        if(canDeleteEpic){
            myEpics.remove(epicIDToDelete);
        }
    }
    public ArrayList<SubTask> getSubTasksOfEpic(String epicDescription) {
        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        Integer epicParentIDs = -1;
        for (Integer i : myEpics.keySet()) {//Сначала нам надо найти ID эпика по его описанию
            Epic epicToExtractAllSubtasks = myEpics.get(i);
            if (epicToExtractAllSubtasks.getTaskDescription().equals(epicDescription)) {
                epicParentIDs = i;//Нашли ID эпика
                System.out.println("Такой эпик нашёлся");
                break;
            }
        }
        if (epicParentIDs >= 0) {//Если ID эпика нашли, то набираем ArrayList его подзадач
            for (Integer i : mySubTasks.keySet()) {//Сначала нам надо найти ID эпика
                SubTask subtaskToCheckTheirParentId = mySubTasks.get(i);
                if (subtaskToCheckTheirParentId.getParentTaskID() == epicParentIDs) {
                    listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);
                    System.out.println("Добавил подзадачу в список");
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
                    if (currentSubtaskToCalculateTask.getParentTaskID() == i) {//Если у подзадачи родителем является текущая задача, то учитываем её статус в расчёте статуса родителя
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
