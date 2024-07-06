package models;

import controller.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {
    InMemoryHistoryManager managerForHistory;
    TaskManager managerForInMemoryTasks;


    @BeforeEach
    public void BeforeEach() {
        managerForInMemoryTasks = Managers.getDefault(null);
        managerForHistory = (InMemoryHistoryManager) Managers.getDefaultHistory();
    }

    @Test
    void twoTasksIsEqualsIfItHasSameID() {
        StandardTask newTask = new StandardTask("task1", "task1 details");
        managerForInMemoryTasks.createTask(newTask);
        StandardTask newTask1, newTask2;
        newTask1 = managerForInMemoryTasks.getTask(1);
        newTask2 = managerForInMemoryTasks.getTask(1);
        assertTrue(newTask1.equals(newTask2), "Таски " + newTask1 + " и " + newTask2 + " не равны друг другу");

    }

    @Test
    void canSaveAndReadTaskFromFile() {
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        TaskManager taskManager1 = Managers.getDefault("test.txt");//Создаём менеджер через конструктор
        taskManager1.createTask(newStandardtask);//Создаём вторым менеджером таск с записью в тестовый файл

        FileBackedTaskManager taskManager2 = new FileBackedTaskManager("test.txt");//Создаём менеджер через
        taskManager2 = taskManager2.loadFromFile();
        StandardTask task1 = taskManager1.getTask(1);
        StandardTask task2 = taskManager2.getTask(1);

        assertEquals(task2.getDescription(), task1.getDescription(), "Записанный в файл таск не соответствует прочитанному из этого файла");
    }

    @Test
    void twoInstancesOfStandardTaskIsEqualsIfItHasSameID() {
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForInMemoryTasks.createTask(newStandardtask);
        Task newTask1, newTask2;
        newTask1 = managerForInMemoryTasks.getTask(1);
        newTask2 = managerForInMemoryTasks.getTask(1);
        assertTrue(newTask1.equals(newTask2), "Таски " + newTask1 + " и " + newTask2 + " не равны друг другу");
    }

    @Test
    void twoInstancesOfEpicsIsEqualsIfItHasSameID() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForInMemoryTasks.createEpic(newEpic);
        Epic newEpic1, newEpic2;
        newEpic1 = managerForInMemoryTasks.getEpic(1);
        newEpic2 = managerForInMemoryTasks.getEpic(1);
        assertTrue(newEpic1.equals(newEpic2), "Таски " + newEpic1 + " и " + newEpic2 + " не равны друг другу");
    }

    @Test
    void twoInstancesOfSubTasksIsEqualsIfItHasSameID() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");//Сначала создаём эпик для связи с подзадачей
        managerForInMemoryTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForInMemoryTasks.createSubtask(newSubTask);
        SubTask newSubTask1, newSubTask2;
        newSubTask1 = managerForInMemoryTasks.getSubTask(2);//Запрашиваем одинаковый ID подзадачи
        newSubTask2 = managerForInMemoryTasks.getSubTask(2);
        assertTrue(newSubTask1.equals(newSubTask2), "Таски " + newSubTask1 + " и " + newSubTask2 + " не равны друг другу");
    }

    @Test
    void subtaskCanNotBeEpicForOtherSubtask() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");//Сначала создаём эпик для связи с подзадачей
        managerForInMemoryTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForInMemoryTasks.createSubtask(newSubTask);
        SubTask newSubTask2 = new SubTask("SubTask2", "SubTask2 details", 1);
        managerForInMemoryTasks.createSubtask(newSubTask2);
        SubTask newSubTask3 = new SubTask("SubTask1", "SubTask1 details", 3);//Вторая подзадача создаётся с ID=2, по этому его тут и пробуем
        int result = managerForInMemoryTasks.createSubtask(newSubTask3);
        assertTrue(result == 0, "Созданная подзадача пытается сослаться на подзадачу как на эпик");
    }

    @Test
    void canFindCreatedTaskByID() {
        int plannedTaskID = managerForInMemoryTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int createdtaskID = managerForInMemoryTasks.createTask(newStandardtask);
        newStandardtask = managerForInMemoryTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.getId() == createdtaskID, "Созданная задача некорректно создаётся и не находится под ожидаемым ID");

        int plannedEpicID = managerForInMemoryTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        int createdEpicID = managerForInMemoryTasks.createEpic(newEpic);
        newEpic = managerForInMemoryTasks.getEpic(plannedEpicID);
        assertTrue(newEpic.getId() == createdEpicID, "Созданный эпик не корректно создаётся и не находится под ожидаемым ID");

        int plannedSubTaskID = managerForInMemoryTasks.getTaskID();
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2);
        int createdSubTaskID = managerForInMemoryTasks.createSubtask(newSubTask);
        newSubTask = managerForInMemoryTasks.getSubTask(plannedSubTaskID);
        assertTrue(newSubTask.getId() == createdSubTaskID, "Созданная подзадача не корректно создаётся и не находится под ожидаемым ID");

    }

    @Test
    void managerDoesnotChangeTaskWhenCreatesIt() {
        int plannedTaskID = managerForInMemoryTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForInMemoryTasks.createTask(newStandardtask);
        StandardTask standardtaskToCheck = managerForInMemoryTasks.getTask(plannedTaskID);
        assertTrue(newStandardtask.equals(standardtaskToCheck), "Созданная задача изменяется при работе менеджера задач");

        int plannedEpicID = managerForInMemoryTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForInMemoryTasks.createEpic(newEpic);
        Epic epicToCheck = managerForInMemoryTasks.getEpic(plannedEpicID);
        assertTrue(newEpic.equals(epicToCheck), "Созданный эпик изменяется при работе менеджера задач");

        int plannedSubTaskID = managerForInMemoryTasks.getTaskID();
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 2);
        managerForInMemoryTasks.createSubtask(newSubTask);
        SubTask subTaskToCheck = managerForInMemoryTasks.getSubTask(plannedSubTaskID);
        assertTrue(newSubTask.equals(subTaskToCheck), "Созданная подзадача изменяется при работе менеджера задач");
    }

    @Test
    void canUpdateAnyTaskAsExpected() {
        int plannedTaskID = managerForInMemoryTasks.getTaskID();
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForInMemoryTasks.createTask(newStandardtask);
        String oldDetails = (managerForInMemoryTasks.getTask(plannedTaskID)).getDetails();
        TaskStatus oldStatus = (managerForInMemoryTasks.getTask(plannedTaskID)).getTaskStatus();
        managerForInMemoryTasks.updateTask(plannedTaskID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedDetails = (managerForInMemoryTasks.getTask(plannedTaskID)).getDetails();
        TaskStatus updatedStatus = (managerForInMemoryTasks.getTask(plannedTaskID)).getTaskStatus();
        assertTrue(!updatedDetails.equals(oldDetails), "Обновлённое описание задачи не равно ожидаемому при обновлении");
        assertTrue(!updatedStatus.equals(oldStatus), "Обновлённый статус задачи не равен ожидаемому при обновлении");

        int plannedEpicID = managerForInMemoryTasks.getTaskID();
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForInMemoryTasks.createEpic(newEpic);
        String oldEpicDetails = (managerForInMemoryTasks.getEpic(plannedEpicID)).getDetails();
        managerForInMemoryTasks.updateEpic(plannedEpicID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedEpicDetails = (managerForInMemoryTasks.getEpic(plannedEpicID)).getDetails();
        assertTrue(!updatedEpicDetails.equals(oldEpicDetails), "Обновлённое описание эпика не равно ожидаемому при обновлении");

        int plannedSubTaskID = managerForInMemoryTasks.getTaskID();
        SubTask newSubtask = new SubTask("SubTask1", "SubTask1 details", 2);
        managerForInMemoryTasks.createSubtask(newSubtask);
        String oldSubtaskDetails = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getDetails();
        TaskStatus oldSubtaskStatus = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getTaskStatus();
        managerForInMemoryTasks.updateSubtask(plannedSubTaskID, "Updated details", TaskStatus.IN_PROGRESS);
        String updatedSubTaskDetails = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getDetails();
        TaskStatus updatedSubTaskStatus = (managerForInMemoryTasks.getSubTask(plannedSubTaskID)).getTaskStatus();
        assertTrue(!updatedSubTaskDetails.equals(oldSubtaskDetails), "Обновлённое описание подзадачи не равно ожидаемому при обновлении");
        assertTrue(!updatedSubTaskStatus.equals(oldSubtaskStatus), "Обновлённый статус подзадачи не равен ожидаемому при обновлении");
    }

    @Test
    void canDeleteTask() {
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.deleteTask(1);
        assertNull(managerForInMemoryTasks.getTask(1), "Задача не удалилась");
    }

    @Test
    void canDeleteEpic() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForInMemoryTasks.createEpic(newEpic);
        managerForInMemoryTasks.deleteEpic(1);
        assertNull(managerForInMemoryTasks.getTask(1), "Эпик не удалился");

    }

    @Test
    void canDeleteSubTask() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForInMemoryTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForInMemoryTasks.createSubtask(newSubTask);
        managerForInMemoryTasks.deleteSubtask(2);
        assertNull(managerForInMemoryTasks.getTask(2), "Задача не удалилась");
    }

    @Test
    void canSaveHistory() {
        List<Task> listOfHistory;
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int id = managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.getTask(id);
        listOfHistory = managerForInMemoryTasks.getHistory();
        assertTrue(listOfHistory.size() > 0, "задача не помещена в историю");
    }

    @Test
    void compareTaskInListAndTaskInHistory() {
        List<Task> listOfHistory;
        boolean taskIsTheSame = false;
        StandardTask newStandardtask = new StandardTask("StandardTask1", "StandardTask1 details");
        int id = managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.getTask(id);
        listOfHistory = managerForInMemoryTasks.getHistory();
        for (Task task : listOfHistory) {
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
        int id = managerForInMemoryTasks.createTask(newStandardtask);
        managerForInMemoryTasks.getTask(id);
        numberOfTasksInHistoryBeforeDeletion = managerForInMemoryTasks.getHistory().size();
        managerForHistory.remove(id);
        numberOfTasksInHistoryAfterDeletion = managerForInMemoryTasks.getHistory().size();
        assertTrue((numberOfTasksInHistoryBeforeDeletion - numberOfTasksInHistoryAfterDeletion) == 0, "задача не удалена в истории");
    }

    @Test
    void managerShouldReturnRealInstancesOfManagers() {
        TaskManager newTaskManagerForTest = Managers.getDefault(null);
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        int EpicID = newTaskManagerForTest.createEpic(newEpic);
        assertNotNull(EpicID, "Новый taskMeneger неправильно реализовал эпик");

        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        int SubTaskID = newTaskManagerForTest.createSubtask(newSubTask);
        assertNotNull(SubTaskID, "Новый taskMeneger неправильно реализовал эпик");


        HistoryManager newHistoryManager = Managers.getDefaultHistory();
        newHistoryManager.add(newEpic);
        assertNotNull(newHistoryManager.getHistory(), "HistoryManager неправильно вернул getHistory");
    }

    @Test
    void tryToGetSubtasksOfEpic() {
        Epic newEpic = new Epic("Epic1", "Epic1 details");
        managerForInMemoryTasks.createEpic(newEpic);
        SubTask newSubTask = new SubTask("SubTask1", "SubTask1 details", 1);
        managerForInMemoryTasks.createSubtask(newSubTask);
        ArrayList<SubTask> listOfSubtasks = managerForInMemoryTasks.getSubTasksOfEpic(1);
        assertNotNull(listOfSubtasks, "Проблемы с получением подзадач эпика");
    }
}