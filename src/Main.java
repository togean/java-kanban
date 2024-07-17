import controller.FileBackedTaskManager;
import models.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        FileBackedTaskManager tasksManager = FileBackedTaskManager.loadFromFile("tasks.csv");

        while (true) {
            printMenu();
            String description;
            String details;
            String taskStatus;
            String inputDateTime;
            LocalDateTime taskStartDate;
            Duration taskDuration;
            int indexToManipulate;
            int parentIDForSubtask;
            DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");

            String command = scanner.nextLine();
            switch (command) {
                case "0":
                    System.out.println("Введите description новой задачи: ");
                    description = scanner.nextLine();
                    System.out.println("Введите details новой задачи: ");
                    details = scanner.nextLine();
                    System.out.println("Введите время старта новой задачи (HH:mm dd.MM.yy): ");
                    inputDateTime = scanner.nextLine();
                    taskStartDate = LocalDateTime.parse(inputDateTime, DATE_TIME_FORMATTER);
                    System.out.println("Введите длительность новой задачи (в минутах): ");
                    taskDuration = Duration.ofMinutes(scanner.nextInt());
                    StandardTask newStandardtask = new StandardTask(description, details, taskStartDate, taskDuration);
                    tasksManager.createTask(newStandardtask);
                    break;
                case "1":
                    System.out.println("Введите description нового эпика: ");
                    description = scanner.nextLine();
                    System.out.println("Введите details нового эпика: ");
                    details = scanner.nextLine();
                    taskStartDate = LocalDateTime.now();
                    taskDuration = Duration.ofMinutes(1);
                    Epic newEpic = new Epic(description, details, taskStartDate, taskDuration);
                    tasksManager.createEpic(newEpic);
                    break;
                case "2":
                    System.out.println("Введите description новой подзадачи: ");
                    description = scanner.nextLine();
                    System.out.println("Введите details новой подзадачи: ");
                    details = scanner.nextLine();
                    System.out.println("Введите id эпика для новой подзадачи: ");
                    parentIDForSubtask = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Введите дату старта новой подзадачи (HH:mm dd.MM.yy): ");
                    inputDateTime = scanner.nextLine();
                    taskStartDate = LocalDateTime.parse(inputDateTime, DATE_TIME_FORMATTER);
                    System.out.println("Введите длительность новой подзадачи (в минутах): ");
                    taskDuration = Duration.ofMinutes(scanner.nextInt());
                    SubTask newSubTask = new SubTask(description, details, parentIDForSubtask, taskStartDate, taskDuration);
                    tasksManager.createSubtask(newSubTask);
                    break;
                case "3":
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
                    tasksManager.updateTask(taskID, taskDetails, newTaskStatus);
                    break;
                case "4":
                    System.out.println("Введите ID обновляемого эпика: ");
                    Integer epicID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Введите новый details для эпика: ");
                    details = scanner.nextLine();
                    tasksManager.updateEpic(epicID, details, TaskStatus.NEW);
                    break;
                case "5":
                    System.out.println("Введите ID обновляемой подзадачи: ");
                    int subtaskID = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Введите новый details для задачи: ");
                    String subtaskDetails = scanner.nextLine();
                    System.out.println("Введите новый статус задачи (NEW, IN_PROGRESS, DONE): ");
                    taskStatus = scanner.nextLine();
                    TaskStatus newSubTaskStatus = TaskStatus.NEW;
                    if (taskStatus.equals("DONE")) {
                        newSubTaskStatus = TaskStatus.DONE;
                    }
                    if (taskStatus.equals("IN_PROGRESS")) {
                        newSubTaskStatus = TaskStatus.IN_PROGRESS;
                    }
                    tasksManager.updateSubtask(subtaskID, subtaskDetails, newSubTaskStatus);
                    break;
                case "6":
                    System.out.println("Все задачи: " + tasksManager.getAllTasks());
                    break;
                case "7":
                    System.out.println("Все эпики: " + tasksManager.getAllEpics());
                    break;
                case "8":
                    System.out.println("Все подзадачи: " + tasksManager.getAllSubtasks());
                    break;
                case "9":
                    System.out.println("Введите ID эпика для вывода его подзадач: ");
                    int epicIDToGetListOfSubtasks = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Список подзадач: " + tasksManager.getSubTasksOfEpic(epicIDToGetListOfSubtasks));
                    break;
                case "10":
                    tasksManager.deleteAll();
                    break;
                case "11":
                    System.out.println("Введите ID задачи для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    tasksManager.deleteTask(indexToManipulate);
                    break;
                case "12":
                    System.out.println("Введите ID эпика для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    tasksManager.deleteEpic(indexToManipulate);
                    break;
                case "13":
                    System.out.println("Введите ID подзадачи для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    tasksManager.deleteSubtask(indexToManipulate);
                    break;
                case "14":
                    System.out.println("Введите ID задачи для вывода: ");
                    taskID = scanner.nextInt();
                    scanner.nextLine();
                    tasksManager.getTask(taskID);
                    break;
                case "15":
                    System.out.println("Введите ID эпика для вывода: ");
                    taskID = scanner.nextInt();
                    scanner.nextLine();
                    tasksManager.getEpic(taskID);
                    break;
                case "16":
                    System.out.println("Введите ID подзадачи для вывода: ");
                    taskID = scanner.nextInt();
                    scanner.nextLine();
                    tasksManager.getSubTask(taskID);
                    break;
                case "17":
                    List<Task> history = tasksManager.getHistory();
                    System.out.println("История: " + history);
                    break;
                case "18":
                    System.out.println("Список задач в порядке приоритета: " + tasksManager.getPrioritizedTasks());
                    break;
                case "19":
                    return;
            }
        }
    }

    private static void printMenu() {
        System.out.println("------- Меню создания задач ------- ");
        System.out.println("0. Добавить задачу");
        System.out.println("1. Добавить эпик");
        System.out.println("2. Добавить подзадачу");
        System.out.println("------- Меню обновления (тут история НЕ ведётся) ------- ");
        System.out.println("3. Обновить задачу");
        System.out.println("4. Обновить эпик");
        System.out.println("5. Обновить подзадачу");
        System.out.println("------- Меню вывода задач (без истории) ------- ");
        System.out.println("6. Вывести все задачи");
        System.out.println("7. Вывести все эпики");
        System.out.println("8. Вывести все подзадачи");
        System.out.println("9. Вывести список подзадач эпика");
        System.out.println("------- Меню удаления ------- ");
        System.out.println("10. Удалить все задачи, эпики и подзадачи");
        System.out.println("11. Удалить задачу по ID");
        System.out.println("12. Удалить эпик по ID");
        System.out.println("13. Удалить подзадачу по ID");
        System.out.println("------- Меню работы с историей (для УЧЁТА истории обращения) ------- ");
        System.out.println("14. Запросить задачу по ID ");
        System.out.println("15. Запросить эпик по ID ");
        System.out.println("16. Запросить подзадачу по ID ");
        System.out.println("17. Запросить историю обращений к задачам");
        System.out.println("------- Меню вывода задач в порядке приоритета ------- ");
        System.out.println("18. Вывести все задачи в порядке приоритета по дате");
        System.out.println("------- Меню завершения работы ------- ");
        System.out.println("19. Выход");
    }

}
