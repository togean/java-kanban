import controller.InMemoryTaskManager;
import controller.Managers;
import models.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Managers manager = new Managers();//Создаём утилитарный класс
        InMemoryTaskManager managerForTasks = (InMemoryTaskManager) manager.getDefault();//Указываем ему какого менеджера создать через утилитарный класс

        while (true) {
            printMenu();
            String Description;
            String Details;
            String taskStatus;
            int indexToManipulate;
            int parentIDForSubtask;

            String Command = scanner.nextLine();
            switch (Command) {
                case "0":
                    System.out.println("Введите description новой задачи: ");
                    Description = scanner.nextLine();
                    System.out.println("Введите details новой задачи: ");
                    Details = scanner.nextLine();
                    StandardTask newStandardtask = new StandardTask(Description, Details);
                    managerForTasks.create(newStandardtask);
                    break;
                case "1":
                    System.out.println("Введите description нового эпика: ");
                    Description = scanner.nextLine();
                    System.out.println("Введите details нового эпика: ");
                    Details = scanner.nextLine();
                    Epic newEpic = new Epic(Description, Details);
                    managerForTasks.create(newEpic);
                    break;
                case "2":
                    System.out.println("Введите description новой подзадачи: ");
                    Description = scanner.nextLine();
                    System.out.println("Введите details новой подзадачи: ");
                    Details = scanner.nextLine();
                    System.out.println("Введите родителя новой подзадачи: ");
                    parentIDForSubtask = scanner.nextInt();
                    SubTask newSubTask = new SubTask(Description, Details, parentIDForSubtask);
                    managerForTasks.create(newSubTask);
                    break;
                case "3","5":
                    System.out.println("Введите ID обновляемой задачи: ");
                    int taskID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Введите новый details для задачи: ");
                    String taskDetails = scanner.nextLine();
                    System.out.println("Введите новый статус задачи (NEW, IN_PROGRESS, DONE): ");
                    taskStatus = scanner.nextLine();
                    TaskStatus newTaskStatus = TaskStatus.NEW;
                    if (taskStatus.equals("DONE")) {
                        newTaskStatus = TaskStatus.DONE;
                    }
                    if (taskStatus.equals("IN_PROGRESS")) {
                        newTaskStatus = TaskStatus.IN_PROGRESS;
                    }
                    managerForTasks.update(taskID,taskDetails,newTaskStatus);
                    break;
                case "4":
                    System.out.println("Введите ID обновляемого эпика: ");
                    Integer epicID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Введите новый details для эпика: ");
                    Details = scanner.nextLine();
                    managerForTasks.update(epicID,Details,TaskStatus.NEW);
                    break;
                case "6":
                    System.out.println("Все задачи: "+managerForTasks.getAll("Tasks"));
                    break;
                case "7":
                    System.out.println("Введите ID задачи для вывода: ");
                    taskID = scanner.nextInt();
                    scanner.nextLine();
                    managerForTasks.getTask(taskID);
                    break;
                case "8":
                    System.out.println("Все эпики: "+managerForTasks.getAll("Epics"));
                    break;
                case "9":
                    System.out.println("Все подзадачи: "+managerForTasks.getAll("Subtasks"));
                    break;
                case "10":
                    System.out.println("Все задачи, эпики и подзадачи: "+managerForTasks.getAll("All"));
                    break;
                case "11":
                    managerForTasks.deleteAll();
                    break;
                case "12":
                    System.out.println("Введите ID эпика для вывода его подзадач: ");
                    int epicIDToGetListOfSubtasks = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Список подзадач: "+managerForTasks.getSubTasksOfEpic(epicIDToGetListOfSubtasks));
                    break;
                case "13","14","15":
                    System.out.println("Введите ID для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    managerForTasks.delete(indexToManipulate);
                    break;
                case "16":
                    ArrayList<Task> history = managerForTasks.getHistory();
                    System.out.println("История: "+history);
                    break;
                case "17":
                    return;
            }
        }
    }
    private static void printMenu() {
        System.out.println("------- Меню ------- ");
        System.out.println("0. Добавить задачу");
        System.out.println("1. Добавить эпик");
        System.out.println("2. Добавить подзадачу");
        System.out.println("3. Обновить задачу (тут история НЕ ведётся)");
        System.out.println("4. Обновить эпик (тут история НЕ ведётся)");
        System.out.println("5. Обновить подзадачу (тут история НЕ ведётся)");
        System.out.println("6. Вывести все задачи");
        System.out.println("7. Вывести задачу по ID (для УЧЁТА истории обращения)");
        System.out.println("8. Вывести все эпики");
        System.out.println("9. Вывести все подзадачи");
        System.out.println("10. Вывести все задачи, эпики и подзадачи");
        System.out.println("11. Удалить все задачи, эпики и подзадачи");
        System.out.println("12. Получить список подзадач эпика (тут история НЕ ведётся)");
        System.out.println("13. Удалить задачу по ID");
        System.out.println("14. Удалить эпик по ID");
        System.out.println("15. Удалить подзадачу по ID");
        System.out.println("16. Вывести историю обращений к задачам");
        System.out.println("17. Выход");
    }
}
