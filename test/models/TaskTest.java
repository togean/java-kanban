package models;

import controller.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {
    InMemoryTaskManager managerForTasks;
    InMemoryHistoryManager managerForHistory;


    @BeforeEach
    public void BeforeEach() {
        managerForTasks = (InMemoryTaskManager) Managers.getDefault();
        managerForHistory = (InMemoryHistoryManager) Managers.getDefaultHistory();
    }

    @Test
    void twoTasksIsEqualsIfItHasSameID() {
        StandardTask newTask = new StandardTask("task1", "task1 details");
        managerForTasks.createTask(newTask);
        StandardTask newTask1, newTask2;
        newTask1 = managerForTasks.getTask(1);
        newTask2 = managerForTasks.getTask(1);
        assertTrue(newTask1.equals(newTask2), "Таски " + newTask1 + " и " + newTask2 + " не равны друг другу");

    }

    @Test
    void twoInstancesOfStandardTaskIsEqualsIfItHasSameID() {
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForTasks.createTask(newStandardtask);
        Task newTask1, newTask2;
        newTask1 = managerForTasks.getTask(1);
        newTask2 = managerForTasks.getTask(1);
        assertTrue(newTask1.equals(newTask2), "Таски " + newTask1 + " и " + newTask2 + " не равны друг другу");
    }

    @Test
    void twoInstancesOfEpicsIsEqualsIfItHasSameID() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.createEpic(newEpic);
        Epic newEpic1, newEpic2;
        newEpic1 = managerForTasks.getEpic(1);
        newEpic2 = managerForTasks.getEpic(1);
        assertTrue(newEpic1.equals(newEpic2), "Таски " + newEpic1 + " и " + newEpic2 + " не равны друг другу");
    }

    @Test
    void twoInstancesOfSubTasksIsEqualsIfItHasSameID() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");//Сначала создаём эпик для связи с подзадачей
        managerForTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForTasks.createSubtask(newSubTask);
        SubTask newSubTask1, newSubTask2;
        newSubTask1 = managerForTasks.getSubTask(2);//Запрашиваем одинаковый ID подзадачи
        newSubTask2 = managerForTasks.getSubTask(2);
        assertTrue(newSubTask1.equals(newSubTask2), "Таски " + newSubTask1 + " и " + newSubTask2 + " не равны друг другу");
    }

    @Test
    void subtaskCanNotBeEpicForOtherSubtask() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");//Сначала создаём эпик для связи с подзадачей
        managerForTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForTasks.createSubtask(newSubTask);
        SubTask newSubTask2 = new SubTask("SubTask2", "SubTask2 details", 1);
        managerForTasks.createSubtask(newSubTask2);
        SubTask newSubTask3 = new SubTask("SubTask1", "SubTask1 details", 3);//Вторая подзадача создаётся с ID=2, по этому его тут и пробуем
        int result = managerForTasks.createSubtask(newSubTask3);
        assertTrue(result == 0, "Созданная подзадача пытается сослаться на подзадачу как на эпик");
    }

    @Test
    void canFindCreatedTaskByID() {
        int plannedTaskID = managerForTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int createdtaskID = managerForTasks.createTask(newStandardtask);
        newStandardtask = managerForTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.getId() == createdtaskID, "Созданная задача некорректно создаётся и не находится под ожидаемым ID");

        int plannedEpicID = managerForTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        int createdEpicID = managerForTasks.createEpic(newEpic);
        newEpic = managerForTasks.getEpic(plannedEpicID);
        assertTrue(newEpic.getId() == createdEpicID, "Созданный эпик не корректно создаётся и не находится под ожидаемым ID");

        int plannedSubTaskID = managerForTasks.getTaskID();
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2);
        int createdSubTaskID = managerForTasks.createSubtask(newSubTask);
        newSubTask = managerForTasks.getSubTask(plannedSubTaskID);
        assertTrue(newSubTask.getId() == createdSubTaskID, "Созданная подзадача не корректно создаётся и не находится под ожидаемым ID");

    }

    @Test
    void managerDoesnotChangeTaskWhenCreatesIt() {
        int plannedTaskID = managerForTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForTasks.createTask(newStandardtask);
        StandardTask standardtaskToCheck = managerForTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.equals(standardtaskToCheck), "Созданная задача изменяется при работе менеджера задач");

        int plannedEpicID = managerForTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.createEpic(newEpic);
        Epic epicToCheck = managerForTasks.getEpic(plannedEpicID);
        assertTrue(newEpic.equals(epicToCheck), "Созданный эпик изменяется при работе менеджера задач");

        int plannedSubTaskID = managerForTasks.getTaskID();
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2);
        managerForTasks.createSubtask(newSubTask);
        SubTask subTaskToCheck = managerForTasks.getSubTask(plannedSubTaskID);
        assertTrue(newSubTask.equals(subTaskToCheck), "Созданная подзадача изменяется при работе менеджера задач");
    }

    @Test
    void canUpdateAnyTaskAsExpected() {
        int plannedTaskID = managerForTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForTasks.createTask(newStandardtask);
        String oldDetails = (managerForTasks.getTask(plannedTaskID)).getDetails();
        TaskStatus oldStatus = (managerForTasks.getTask(plannedTaskID)).getTaskStatus();
        managerForTasks.updateTask(plannedTaskID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedDetails = (managerForTasks.getTask(plannedTaskID)).getDetails();
        TaskStatus updatedStatus = (managerForTasks.getTask(plannedTaskID)).getTaskStatus();
        assertTrue(!updatedDetails.equals(oldDetails), "Обновлённое описание задачи не равно ожидаемому при обновлении");
        assertTrue(!updatedStatus.equals(oldStatus), "Обновлённый статус задачи не равен ожидаемому при обновлении");

        int plannedEpicID = managerForTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.createEpic(newEpic);
        String oldEpicDetails = (managerForTasks.getEpic(plannedEpicID)).getDetails();
        managerForTasks.updateEpic(plannedEpicID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedEpicDetails = (managerForTasks.getEpic(plannedEpicID)).getDetails();
        assertTrue(!updatedEpicDetails.equals(oldEpicDetails), "Обновлённое описание эпика не равно ожидаемому при обновлении");

        int plannedSubTaskID = managerForTasks.getTaskID();
        SubTask newSubtask = new SubTask("SubTask1", "SubTask1 details", 2);
        managerForTasks.createSubtask(newSubtask);
        String oldSubtaskDetails = (managerForTasks.getSubTask(plannedSubTaskID)).getDetails();
        TaskStatus oldSubtaskStatus = (managerForTasks.getSubTask(plannedSubTaskID)).getTaskStatus();
        managerForTasks.updateSubtask(plannedSubTaskID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedSubTaskDetails = (managerForTasks.getSubTask(plannedSubTaskID)).getDetails();
        TaskStatus updatedSubTaskStatus = (managerForTasks.getSubTask(plannedSubTaskID)).getTaskStatus();
        assertTrue(!updatedSubTaskDetails.equals(oldSubtaskDetails), "Обновлённое описание подзадачи не равно ожидаемому при обновлении");
        assertTrue(!updatedSubTaskStatus.equals(oldSubtaskStatus), "Обновлённый статус подзадачи не равен ожидаемому при обновлении");
    }

    @Test
    void canDeleteTask() {
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForTasks.createTask(newStandardtask);
        managerForTasks.deleteTask(1);
        assertNull(managerForTasks.getTask(1), "Задача не удалилась");
    }

    @Test
    void canDeleteEpic() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.createEpic(newEpic);
        managerForTasks.deleteEpic(1);
        assertNull(managerForTasks.getTask(1), "Эпик не удалился");

    }

    @Test
    void canDeleteSubTask() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForTasks.createSubtask(newSubTask);
        managerForTasks.deleteSubtask(2);
        assertNull(managerForTasks.getTask(2), "Задача не удалилась");
    }

    @Test
    void canSaveHistory() {
        List<Task> listOfHistory;
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int id = managerForTasks.createTask(newStandardtask);
        managerForTasks.getTask(id);
        listOfHistory = managerForTasks.getHistory();
        assertTrue(listOfHistory.size() > 0, "задача не помещена в историю");
    }
    @Test
    void compareTaskInListAndTaskInHistory() {
        List<Task> listOfHistory;
        boolean taskIsTheSame = false;
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int id = managerForTasks.createTask(newStandardtask);
        managerForTasks.getTask(id);
        listOfHistory = managerForTasks.getHistory();
        for(Task task: listOfHistory){
            taskIsTheSame = newStandardtask.getDescription().equals(task.getDescription());
        }
        assertTrue(taskIsTheSame, "задача в истории не соответствует созданной проверочной задачи");
    }
    @Test
    void canDeleteItemInHistory() {
        List<Task> listOfHistory;
        int numberOfTasksInHistoryBeforeDeletion;
        int numberOfTasksInHistoryAfterDeletion;
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int id = managerForTasks.createTask(newStandardtask);
        managerForTasks.getTask(id);
        numberOfTasksInHistoryBeforeDeletion = managerForTasks.getHistory().size();
        managerForHistory.remove(id);
        numberOfTasksInHistoryAfterDeletion = managerForTasks.getHistory().size();
        assertTrue((numberOfTasksInHistoryBeforeDeletion-numberOfTasksInHistoryAfterDeletion) == 0, "задача не удалена в истории");
    }
    @Test
    void managerShouldReturnRealInstancesOfManagers(){
        Manager newManagerForTest = Managers.getDefault();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        int EpicID = newManagerForTest.createEpic(newEpic);
        assertNotNull(EpicID, "Новый taskMeneger неправильно реализовал эпик");

        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        int SubTaskID = newManagerForTest.createSubtask(newSubTask);
        assertNotNull(SubTaskID, "Новый taskMeneger неправильно реализовал эпик");


        HistoryManager newHistoryManager = Managers.getDefaultHistory();
        newHistoryManager.add(newEpic);
        assertNotNull(newHistoryManager.getHistory(), "HistoryManager неправильно вернул getHistory");
    }
    @Test
    void tryToGetSubtasksOfEpic(){
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForTasks.createSubtask(newSubTask);
        ArrayList<SubTask> listOfSubtasks = managerForTasks.getSubTasksOfEpic(1);
        assertNotNull(listOfSubtasks, "Проблемы с получением подзадач эпика");
    }
}