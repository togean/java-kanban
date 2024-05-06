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
        System.out.println("Перечень всех задач:");
        for (Task tasksToShow : myTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
    }

    public void getAllSubTasks() {
        System.out.println("Перечень всех подзадач:");
        for (Task tasksToShow : mySubTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
    }

    public void getAllEpics() {
        System.out.println("Перечень всех эпиков:");
        for (Epic epicToShow : myEpics.values()) {
            System.out.println(epicToShow.toString());
        }
    }

    public Integer createTask(Task taskToBeCreated) {
        Task newTask = new Task(taskToBeCreated.getTaskDescription(), taskToBeCreated.getTaskDetails(), TaskStatus.NEW);
        newTask.setTaskIndex(newTask.hashCode());
        myTasks.put(newTask.hashCode(), newTask);
        return newTask.hashCode();
    }

    public Integer createEpic(Epic epicToBeCreated) {
        Epic newEpic = new Epic(epicToBeCreated.getTaskDescription(), epicToBeCreated.getTaskDetails(), TaskStatus.NEW);
        newEpic.setTaskIndex(newEpic.hashCode());
        myEpics.put(newEpic.hashCode(), newEpic);
        return newEpic.hashCode();
    }

    public Integer createSubTask(SubTask taskToBeCreated) {
        SubTask newSubTask = new SubTask(taskToBeCreated.getTaskDescription(), taskToBeCreated.getTaskDetails(), TaskStatus.NEW, taskToBeCreated.getParentTaskID());
        newSubTask.setTaskIndex(newSubTask.hashCode());
        mySubTasks.put(newSubTask.hashCode(), newSubTask);
        //Добавляем подзадачу в список подзадач эпика, если ID родителя в подзадаче не равен 0
        if(taskToBeCreated.getParentTaskID()>0) {
            ArrayList<Integer> listOfEpicsSubTasks;
            Epic epicToBeUpdated = myEpics.get(newSubTask.getParentTaskID());
            listOfEpicsSubTasks = myEpics.get(newSubTask.getParentTaskID()).getEpicSubtasks();
            listOfEpicsSubTasks.add(newSubTask.getTaskIndex());
            epicToBeUpdated.setEpicSubtasks(listOfEpicsSubTasks);
            updateEpic(epicToBeUpdated);//Обновляем список подзадаач для эпика
        }
        return newSubTask.hashCode();
    }

    public void updateTask(Task task) {
        myTasks.put(task.getTaskIndex(), task);
    }

    public void updateEpic(Epic epicToUpdate) {
        myEpics.put(epicToUpdate.getTaskIndex(), epicToUpdate);
    }

    public void updateSubTask(SubTask subTask) {
         mySubTasks.put(subTask.getTaskIndex(), subTask);
         recalculateOrUpdateTaskStatus();//Пересчитываем статусы эпиков, так как мы изменили подзадачу
    }

    public void deleteAllTasks() {
        myTasks.clear();
        mySubTasks.clear();
        myEpics.clear();
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
