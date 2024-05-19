package models;

import controller.InMemoryTaskManager;
import controller.Managers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    Managers manager = new Managers();//Создаём утилитарный класс
    InMemoryTaskManager managerForTasks;
    @BeforeEach
    public void BeforeEach() {
        managerForTasks = (InMemoryTaskManager) manager.getDefault();
    }
    @Test
    void twoTasksIsEqualsIfItHasSameID() {
        Task newTask = new StandardTask("task1", "task1 details");
        managerForTasks.create(newTask);
        Task newTask1, newTask2;
        newTask1 = managerForTasks.getTask(0);
        newTask2 = managerForTasks.getTask(0);
        assertTrue(newTask1.equals(newTask2),"Таски "+newTask1+" и "+newTask2+" не равны друг другу");

    }
    @Test
    void twoInstancesOfStandardTaskIsEqualsIfItHasSameID() {
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForTasks.create(newStandardtask);
        Task newTask1, newTask2;
        newTask1 = managerForTasks.getTask(0);
        newTask2 = managerForTasks.getTask(0);
        assertTrue(newTask1.equals(newTask2),"Таски "+newTask1+" и "+newTask2+" не равны друг другу");
    }
    @Test
    void twoInstancesOfEpicsIsEqualsIfItHasSameID() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.create(newEpic);
        Task newEpic1, newEpic2;
        newEpic1 = managerForTasks.getTask(0);
        newEpic2 = managerForTasks.getTask(0);
        assertTrue(newEpic1.equals(newEpic2),"Таски "+newEpic1+" и "+newEpic2+" не равны друг другу");
    }
    @Test
    void twoInstancesOfSubTasksIsEqualsIfItHasSameID() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");//Сначала создаём эпик для связи с подзадачей
        managerForTasks.create(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 0);
        managerForTasks.create(newSubTask);
        Task newSubTask1, newSubTask2;
        newSubTask1 = managerForTasks.getTask(0);//Запрашиваем одинаковый ID подзадачи
        newSubTask2 = managerForTasks.getTask(0);
        assertTrue(newSubTask1.equals(newSubTask2),"Таски "+newSubTask1+" и "+newSubTask2+" не равны друг другу");
    }
    @Test
    void subtaskCanNotBeEpicForOtherSubtask(){
        Epic newEpic = new Epic("Epic1", "Epic1 details");//Сначала создаём эпик для связи с подзадачей
        managerForTasks.create(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 0);
        managerForTasks.create(newSubTask);
        SubTask newSubTask2 = new SubTask("SubTask2", "SubTask2 details", 0);
        managerForTasks.create(newSubTask2);
        SubTask newSubTask3 = new SubTask("SubTask1", "SubTask1 details", 2);//Вторая подзадача создаётся с ID=2, по этому его тут и пробуем
        int result = managerForTasks.create(newSubTask3);
        assertTrue(result==0, "Созданная подзадача пытается сослаться на подзадачу как на эпик");
    }
    @Test
    void canFindCreatedTaskByID(){
        int plannedTaskID = managerForTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int createdtaskID = managerForTasks.create(newStandardtask);
        newStandardtask = (StandardTask) managerForTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.getId()==createdtaskID, "Созданная задача некорректно создаётся и не находится под ожидаемым ID");

        int plannedEpicID = managerForTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        int createdEpicID = managerForTasks.create(newEpic);
        newEpic = (Epic) managerForTasks.getTask(plannedEpicID);
        assertTrue(newEpic.getId()==createdEpicID, "Созданный эпик не корректно создаётся и не находится под ожидаемым ID");

        int plannedSubTaskID = managerForTasks.getTaskID();
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        int createdSubTaskID = managerForTasks.create(newSubTask);
        newSubTask = (SubTask) managerForTasks.getTask(plannedSubTaskID);
        assertTrue(newSubTask.getId()==createdSubTaskID, "Созданная подзадача не корректно создаётся и не находится под ожидаемым ID");

    }
    @Test
    void managerDoesnotChangeTaskWhenCreatesIt(){
        int plannedTaskID = managerForTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForTasks.create(newStandardtask);
        StandardTask standardtaskToCheck = (StandardTask) managerForTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.equals(standardtaskToCheck),"Созданная задача изменяется при работе менеджера задач");

        int plannedEpicID = managerForTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.create(newEpic);
        Epic epicToCheck = (Epic) managerForTasks.getTask(plannedEpicID);
        assertTrue(newEpic.equals(epicToCheck), "Созданный эпик изменяется при работе менеджера задач");

        int plannedSubTaskID = managerForTasks.getTaskID();
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForTasks.create(newSubTask);
        SubTask subTaskToCheck = (SubTask) managerForTasks.getTask(plannedSubTaskID);
        assertTrue(newSubTask.equals(subTaskToCheck), "Созданная подзадача изменяется при работе менеджера задач");
    }

}