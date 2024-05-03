import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Tasks> myTasks;
    private HashMap<Integer, SubTasks> mySubTasks;

    public TaskManager() {
        myTasks = new HashMap<>();
        mySubTasks = new HashMap<>();
    }

    public void getAllTasks() {
        System.out.println("Перечень всех задач:");
        for (Tasks tasksToShow : myTasks.values()) {
            System.out.println(tasksToShow.toString());
        }
        System.out.println("Перечень всех подзадач:");
        for (Tasks tasksToShow : mySubTasks.values()) {
            System.out.println(tasksToShow.toString());
        }

    }

    public Integer getParentIDForSubTask(String parentDescription) {
        Integer parentID = -1;
        for (Integer i : myTasks.keySet()) {
            Tasks task = myTasks.get(i);
            if (task.getTaskDescription().equals(parentDescription)) {
                parentID = i;
            }
        }
        return parentID;
    }

    public void deleteAllTasks() {
        myTasks.clear();
        mySubTasks.clear();
    }

    public void deleteSubTask(Integer subtaskIDToDelete) {
        Integer parentID = -1;//Переменная для хранения ссылки на родителя
        boolean needChangeTaskType = true;
        for (Integer i : mySubTasks.keySet()) {
            SubTasks subtaskToGetParentID = mySubTasks.get(i);
            if (i == subtaskIDToDelete) {
                parentID = subtaskToGetParentID.getParentTaskID();
                mySubTasks.remove(subtaskIDToDelete);
                break;
            }
        }
        //Далее нужно проверить, что у родителя больше нет подзадач и если это так, то родительская задача из эпика должна превратиться просто в задачу
        if (parentID >= 0) {//Проверяем, остались ли ещё подзадачи со связью с данным родителем
            for (Integer i : mySubTasks.keySet()) {
                SubTasks subtaskToGetParentID = mySubTasks.get(i);
                if (subtaskToGetParentID.getParentTaskID() == parentID) {
                    //подзадачи ссылающиеся на данного родителя есть, так что изменять тип задачи с эпика на просто задачу не будем
                    needChangeTaskType = false;
                    break; //Выходим из цикла так как нам надо было обнаружить хотя бы одну связь с родительской задачей
                }
            }
            if (needChangeTaskType) {//Если же связей больше нет, то меняем тип родительской задачи
                Tasks updatedParentTask = myTasks.get(parentID);
                updatedParentTask.setTaskEpic(false);
                myTasks.put(parentID, updatedParentTask);
            }
        }
    }

    public void createTask(String taskDescription, String taskDetails, TasksTypes taskType, String parentTaskDescription) {
        if (!taskType.equals(TasksTypes.SUBTASK)) {//Если задача не явлется подзадачей, то будет создана задача
            boolean canCreateTask = true;//Предполагаем, что задачу можем создать
            for (Tasks task : myTasks.values()) {//Проверяем, что такой же задачи ещё небыло
                if (task.getTaskDescription().equals(taskDescription)) {
                    canCreateTask = false;//Такая задача уже была - не можем такую же создавать
                    break;
                }
            }
            if (canCreateTask) {//Если такой задачи ещё небыло, то можем создать новую задачу с заданным описанием
                Tasks newTask = new Tasks(taskDescription, taskDetails, TaskStatuses.NEW, taskType);
                myTasks.put(myTasks.size(), newTask);
            }
        } else {//В нашем трекере пользователь пытается создать подзадачу, а значит, нам надо найти родительскую по описанию данной поздадачи
            Integer parentTaskID = -1;
            for (Integer i : myTasks.keySet()) { //В цикле ищем совпадения по описанию родительской задачи
                Tasks taskToCompare = myTasks.get(i);
                if (taskToCompare.getTaskDescription().equals(parentTaskDescription)) {
                    parentTaskID = i;//Родительская задача найдена
                    break;
                }
            }
            if (parentTaskID >= 0) {//Если родитель найден, то выполняем проверку на возможность создания подзадачи
                boolean canCreateSubTask = true;
                for (Integer i : mySubTasks.keySet()) {//В цикле проверяем есть ли такая подзадача в списке подзадач - так как подзадача могла быть у другого родителя. Предполагается, что у разных родителем могут быть одинаковые подзадачи
                    SubTasks taskToCompare = mySubTasks.get(i);
                    if (taskToCompare.getTaskDescription().equals(taskDescription)) {
                        canCreateSubTask = false;//Обнаружили такую подзадачу - надо проверить, у того же она родителя или у другого
                        break;
                    }
                }
                if (canCreateSubTask) {//Если нашей подзадачи ранее небыло, то можем её создать
                    Integer newID = mySubTasks.size();
                    SubTasks newSubTask = new SubTasks(taskDescription, taskDetails, TaskStatuses.NEW, TasksTypes.SUBTASK, parentTaskID);
                    mySubTasks.put(newID, newSubTask);
                    //Далее, наш родитель должен стать эпиком, так как у него появились подзадачи
                    Tasks tempTaskToCompare = myTasks.get(parentTaskID);
                    if (!tempTaskToCompare.isTaskEpic()) {//Проверяем, может родительская задача уже является эпиком
                        tempTaskToCompare.setTaskEpic(true);//Если родитель небыл эпиком, то выставляем, что он теперь эпик
                    }
                } else {//Если же подзадача уже была - надо проверить, совпадают ли ID родителя. Если совпадают - не можем создать подзадачу, так как у одного родителя не может быть две одинаковых подзадачи
                    for (Integer i : mySubTasks.keySet()) {//Смотрим в подзадачах, нашу подзадачу и проверяем её родительский ID
                        SubTasks subtaskToCompare = mySubTasks.get(i);
                        if (subtaskToCompare.getParentTaskID() != parentTaskID) {
                            //Если у существующей подзадачи другой родитель, то новую такую же подзадачу у другого родителя можем создать
                            Integer newID = mySubTasks.size();
                            SubTasks newSubTask = new SubTasks(taskDescription, taskDetails, TaskStatuses.NEW, TasksTypes.SUBTASK, parentTaskID);
                            mySubTasks.put(newID, newSubTask);
                            //Далее, как и раньше наш родитель должен стать эпиком, так как у него появились подзадачи
                            Tasks tempTaskToCompare = myTasks.get(parentTaskID);
                            if (!tempTaskToCompare.isTaskEpic()) {//Проверяем, может родительская задача уже является эпиком
                                tempTaskToCompare.setTaskEpic(true);//Если родитель небыл эпиком, то выставляем, что он теперь эпик
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateTask(Tasks task) {
        for (Integer i : myTasks.keySet()) {
            Tasks taskToUpdate = myTasks.get(i);
            if (taskToUpdate.getTaskDescription().equals(task.getTaskDescription())) {
                if (task.isTaskEpic()) {//Если же задача эпик, то статус не меняем, он будет пересчитываться, по-этому оставляем старое значение
                    task.setTaskStatus(taskToUpdate.getTaskStatus());
                }
                myTasks.put(i, task);
            }
        }
    }

    public void updateSubTask(SubTasks subTask) {
        for (Integer i : mySubTasks.keySet()) {
            Tasks subtaskToUpdate = mySubTasks.get(i);
            if (subtaskToUpdate.getTaskDescription().equals(subTask.getTaskDescription())) {
                mySubTasks.put(i, subTask);
            }
        }
    }

    public void deleteTask(Integer taskIDToDelete) {
        boolean canDeleteTask = false;//Флаг того, что могу удалить выбранную задачу (проверка что запись с нужным для удаления ID есть)
        for (Integer i : myTasks.keySet()) {
            if (i == taskIDToDelete) {
                canDeleteTask = true;
                break;
            }
        }
        if (canDeleteTask) {
            //Сначала проверяем, что на задача с данным ID не эпик - т.е. у неё нет подзадач и на неё никакая подзадача не ссылается
            Tasks taskToDelete = myTasks.get(taskIDToDelete);
            if (!taskToDelete.isTaskEpic()) {//Если задача НЕ эпик, то можем удалять
                myTasks.remove(taskIDToDelete);
            } else {//Если подзадача эпик, то сначала удалим все её подзадачи, а потом и сам эпик.
                for (Integer i : mySubTasks.keySet()) {
                    SubTasks subtasksToDelete = mySubTasks.get(i);
                    if (subtasksToDelete.getParentTaskID() == taskIDToDelete) {
                        mySubTasks.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<SubTasks> getSubTasksOfEpic(String epicDescription) {
        ArrayList<SubTasks> listOfSubtasksForEPIC = new ArrayList<>();
        Integer subtasksParentIDs = -1;
        for (Integer i : myTasks.keySet()) {//Сначала нам надо найти ID эпика
            Tasks taskToExtractAllSubtasks = myTasks.get(i);
            if (taskToExtractAllSubtasks.getTaskDescription().equals(epicDescription)) {
                subtasksParentIDs = i;//Нашли ID эпика
                break;
            }
        }
        if (subtasksParentIDs > 0) {//Если ID эпика нашли, то набираем ArrayList его подзадач
            for (Integer i : mySubTasks.keySet()) {//Сначала нам надо найти ID эпика
                SubTasks subtaskToCheckTheirParentId = mySubTasks.get(i);
                if (subtaskToCheckTheirParentId.getParentTaskID() == subtasksParentIDs) {
                    listOfSubtasksForEPIC.add(subtaskToCheckTheirParentId);
                }
            }
        }
        return listOfSubtasksForEPIC;
    }

    public void recalculateOrUpdateTaskStatus() {
        for (Integer i : myTasks.keySet()) {//Проходим по каждой задаче
            Tasks currentRecalculatedTask = myTasks.get(i);
            boolean isTaskDone = true;
            boolean isTaskNew = true;
            Integer numberOfSubtasks = 0;//Учёт сколько подзадач у задачи, что бы анализировать потом в цикле статусы
            ArrayList<TaskStatuses> idsOfSubtasksForCurrentTask = new ArrayList<>();
            if (currentRecalculatedTask.isTaskEpic) {//Пересчёт только для эпиков
                //
                //Вот тут у еня вопрос: Эпиком же по условию может быть ТОЛЬКО задача С НАЛИЧИЕМ подзадач. Почему в условиях для эпика говорится, что ЕСЛИ У ЭПИКА НЕТ подзадач, то его статус - NEW. Ведь по определению у ЭПИКА ОБЯЗАТЕЛЬНО должна быть подзадача.
                //
                for (Integer j : mySubTasks.keySet()) {//В цикле читаем статусы подзадач текущей родительской задачи для расчёта статуса родительской задачи
                    SubTasks currentSubtaskToCalculateTask = mySubTasks.get(j);
                    if (currentSubtaskToCalculateTask.getParentTaskID() == i) {//Если у подзадачи родителем является текущая задача, то учитываем её статус в расчёте статуса родителя
                        idsOfSubtasksForCurrentTask.add(currentSubtaskToCalculateTask.getTaskStatus());//Добавляем в список значения статуса у очередной подзадачи
                        numberOfSubtasks++;//Считаем сколько подзадач, что бы потом организовать цикл подсчёта
                    }
                }
                int numberOfNew = 0; //Кол-во подзадач статуса New
                int numberOfDone = 0; //Кол-во подзадач статуса DONE
                for (int k = 0; k < numberOfSubtasks; k++) {//Идём по циклу выявленных подзадач нашей родительской задачи и сравниваем статусы, одновременно их подсчитывая
                    if (idsOfSubtasksForCurrentTask.get(k).equals(TaskStatuses.NEW)) {
                        numberOfNew++;
                    }
                    if (idsOfSubtasksForCurrentTask.get(k).equals(TaskStatuses.DONE)) {
                        numberOfDone++;
                    }
                }
                if (numberOfNew == numberOfSubtasks) {
                    currentRecalculatedTask.setTaskStatus(TaskStatuses.NEW);
                    myTasks.put(i, currentRecalculatedTask);
                } else {
                    isTaskNew = false;
                }
                if (numberOfDone == numberOfSubtasks) {
                    currentRecalculatedTask.setTaskStatus(TaskStatuses.DONE);
                    myTasks.put(i, currentRecalculatedTask);
                } else {
                    isTaskDone = false;
                }
                if (!isTaskNew && !isTaskDone) {//Если статус не NEW и не DONE, значит задача пока в состоянии IN_PROGRESS
                    currentRecalculatedTask.setTaskStatus(TaskStatuses.IN_PROGRESS);
                    myTasks.put(i, currentRecalculatedTask);//Сохраняем вычисленное значение родительской задачи
                }
            }
        }
    }
}
