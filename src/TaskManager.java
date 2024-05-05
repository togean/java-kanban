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

    public Task getTaskByDescription(String taskDescription){
        Task taskToBeFound = null;
        for (Integer i : myTasks.keySet()) {
            taskToBeFound = myTasks.get(i);
            if (taskToBeFound.getTaskDescription().equals(taskDescription)) {
                break;
            }
        }
        return taskToBeFound;
    }
    public Epic getEpicByDescription(String taskDescription){
        Epic epicToBeFound = null;
        for (Integer i : myEpics.keySet()) {
            epicToBeFound = myEpics.get(i);
            if (epicToBeFound.getTaskDescription().equals(taskDescription)) {
                break;
            }
        }
        return epicToBeFound;
    }
    public SubTask getSubtaskByDescription(String taskDescription){
        SubTask subtaskToBeFound = null;
        for (Integer i : mySubTasks.keySet()) {
            subtaskToBeFound = mySubTasks.get(i);
            if (subtaskToBeFound.getTaskDescription().equals(taskDescription)) {
                break;
            }
        }
        return subtaskToBeFound;
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



    public void createTask(Task taskToBeCreated) {
        Task newTask = new Task(taskToBeCreated.getTaskDescription(), taskToBeCreated.getTaskDetails(), TaskStatus.NEW);
        newTask.setTaskIndex(newTask.hashCode());
        myTasks.put(newTask.hashCode(), newTask);
    }

    public Integer createEpic(Epic epicToBeCreated) {
        Epic newEpic = new Epic(epicToBeCreated.getTaskDescription(), epicToBeCreated.getTaskDetails(), TaskStatus.NEW);
        newEpic.setTaskIndex(newEpic.hashCode());
        myEpics.put(newEpic.hashCode(), newEpic);
        return newEpic.hashCode();
    }

    public void createSubTask(SubTask taskToBeCreated) {
        Task parenttaskForSubtask = getTaskByDescription(taskToBeCreated.getTaskDescription());//Сабтаск не живёт без родителя, по-этому проверяем родительский таск, если он есть в списке тасков
        Epic parentepicForSubtask = getEpicByDescription(taskToBeCreated.getTaskDescription());//Сабтаск не живёт без родителя, по-этому проверяем родительский эпик, если он есть в списке эпиков

        Integer parentTaskID = -1;

        SubTask newSubTask = new SubTask(taskToBeCreated.getTaskDescription(), taskToBeCreated.getTaskDetails(), TaskStatus.NEW, parentTaskID);
        newSubTask.setTaskIndex(newSubTask.hashCode());

        ArrayList<Integer> listOfSubtasksInEpic = new ArrayList<>();//Тут будем сохранять список подзадач эпика, что бы его продолжить при создании подзадачи

        if(!(parenttaskForSubtask==null)) {
            //Родительская задача найдена в задачах
            //Надо перенести родительскую задачу в эпики c обновлением индекса, так как при создании эпика индекс изменится
            Epic epicToCreate = new Epic(parenttaskForSubtask.getTaskDescription(),parenttaskForSubtask.getTaskDetails(),TaskStatus.NEW);
            parentTaskID = createEpic(epicToCreate);
            parentepicForSubtask = myEpics.get(parentTaskID);//Сохраняем данные вновь созданного эпика для дальнейшей работы (наполнения его списка подзадач)
            myTasks.remove(parenttaskForSubtask.getTaskIndex());//Удаляем уже не нужную задачу
            listOfSubtasksInEpic = parentepicForSubtask.getEpicSubtasks();//Получаем список подзадач нового эпика
        }
        if(!(parentepicForSubtask==null)){
            //Родительская задача найдена в эпиках
            parentTaskID = parentepicForSubtask.getTaskIndex();
            listOfSubtasksInEpic = parentepicForSubtask.getEpicSubtasks(); //Получаем список подзадач родительского эпика
        }

        if (parentTaskID >= 0) {//Если родитель найден, то выполняем проверку на возможность создания подзадачи
            boolean canCreateSubTask = mySubTasks.containsValue(newSubTask);
            if (!canCreateSubTask) {//Если нашей подзадачи ранее небыло, то можем её просто создать
                newSubTask.setParentTaskID(parentepicForSubtask.getTaskIndex());//Выставляем в подзадаче индекс родителя
                mySubTasks.put(newSubTask.hashCode(), newSubTask);
                listOfSubtasksInEpic.add(newSubTask.hashCode());//добавляем в список подзадач эпика новую подзадачу
                parentepicForSubtask.setEpicSubtasks(listOfSubtasksInEpic);//Сохраняем обновлённый список подзадач эпика
                updateEpic(parentepicForSubtask);//Обновляем эпик
                recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков
            } else {//Если же подзадача уже была - надо проверить, совпадают ли ID родителя. Если совпадают - не можем создать подзадачу, так как у одного родителя не может быть две одинаковых подзадачи
                for (Integer i : mySubTasks.keySet()) {//Смотрим в подзадачах, нашу подзадачу и проверяем её родительский ID
                    SubTask subtaskToCompare = mySubTasks.get(i);
                    if (!subtaskToCompare.getParentTaskID().equals(parentTaskID)) {//Если ID НЕ совпадают, то можем создать подзадачу
                        mySubTasks.put(newSubTask.hashCode(), newSubTask);
                    }
                }
            }
        }
    }

    public void updateTask(Task task) {
        Task updatedTask = getTaskByDescription(task.getTaskDescription());
        if(myTasks.containsValue(updatedTask)) {//Проверка, что таск существует
            task.setTaskIndex(updatedTask.getTaskIndex());
            myTasks.put(updatedTask.getTaskIndex(), task);
        }
    }

    public void updateEpic(Epic epicToUpdate) {
        Epic updatedEpic = getEpicByDescription(epicToUpdate.getTaskDescription());
        if(myEpics.containsValue(updatedEpic)) {//Проверка, что эпик существует
            epicToUpdate.setTaskIndex(updatedEpic.getTaskIndex());
            myEpics.put(updatedEpic.getTaskIndex(), epicToUpdate);
        }
    }

    public void updateSubTask(SubTask subTask) {
        SubTask updatedSubtask = getSubtaskByDescription(subTask.getTaskDescription());
        if(mySubTasks.containsValue(updatedSubtask)) {//Проверка, что подзадача существует
            subTask.setTaskIndex(updatedSubtask.getTaskIndex());//Выставили правильное значение индекса подзадачи
            subTask.setParentTaskID(updatedSubtask.getParentTaskID());//Выставили правильное значение индекса родителя
            mySubTasks.put(updatedSubtask.getTaskIndex(), subTask);
            recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков, так как мы изменили подзадачу
        }
    }

    public void deleteTask(Integer taskIDToDelete) {
        myTasks.remove(taskIDToDelete);
    }

    public void deleteEpic(Integer epicIDToDelete) {
        ArrayList<Integer> listOfSubtasksToBeDeleted;
        Epic epicToBeDeleted = myEpics.get(epicIDToDelete);
        listOfSubtasksToBeDeleted = epicToBeDeleted.getEpicSubtasks();
        for(int i: listOfSubtasksToBeDeleted){
            mySubTasks.remove(i);
        }
        myEpics.remove(epicIDToDelete);
    }
    public void deleteSubTask(Integer subtaskIDToDelete) {
        ArrayList<Integer> listOfSubtasksInEpic;
        SubTask subtaskToBeDeleted = mySubTasks.get(subtaskIDToDelete);
        Epic epicToUpdate = myEpics.get(subtaskToBeDeleted.getParentTaskID());
        listOfSubtasksInEpic = epicToUpdate.getEpicSubtasks();
        for(Integer i: listOfSubtasksInEpic){
            if(i.equals(subtaskIDToDelete)){
                listOfSubtasksInEpic.remove(i);
                break;
            }
        }
        epicToUpdate.setEpicSubtasks(listOfSubtasksInEpic);
        myEpics.put(epicToUpdate.getTaskIndex(), epicToUpdate);//Сохраняем в эпике обновлённый список подзадач
        mySubTasks.remove(subtaskIDToDelete);
        recalculateOrUpdateTaskStatus();//Обновляем статусы эпиков
    }
    public ArrayList<SubTask> getSubTasksOfEpic(int epicID) {

        ArrayList<SubTask> listOfSubtasksForEPIC = new ArrayList<>();
        ArrayList<Integer> listOfSubtasksIDs;
        Epic epicToGetList = myEpics.get(epicID);//Получаем данные выбранного эпика
        listOfSubtasksIDs = epicToGetList.getEpicSubtasks();//Забираем из эпика список его сабтасков
        for (Integer i : listOfSubtasksIDs) {//Идём по списку сабтасков выбранного эпика
            SubTask subtaskToCheckTheirParentId = mySubTasks.get(i);
            listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);//Наполняем список сабтасков
        }
        return listOfSubtasksForEPIC;
    }

    private void recalculateOrUpdateTaskStatus() {
        for (Integer i : myEpics.keySet()) {//Проходим по каждой задаче
            Epic currentRecalculatedEpic = myEpics.get(i);
            boolean isTaskDone = true;
            boolean isTaskNew = true;
            int numberOfSubtasks = 0;//Учёт сколько подзадач у задачи, что бы анализировать потом в цикле статусы
            ArrayList<Integer> listOfEpicSubtasks = myEpics.get(i).getEpicSubtasks();
            ArrayList<TaskStatus> idsOfSubtasksForCurrentTask = new ArrayList<>();

            for (Integer j : listOfEpicSubtasks) {//В цикле читаем статусы подзадач текущей родительской задачи для расчёта статуса родительской задачи
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
