import java.util.ArrayList;
import java.util.Scanner;
public class Main {
    public static TaskManager taskmanager = new TaskManager();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String myDescription;
        String myDetails;
        String myStatus;
        int indexToManipulate;
        TaskStatus myTaskStatus = TaskStatus.NEW;
        while (true) {
            printMenu();
            String Command = scanner.nextLine();
            switch (Command) {
                case "0":
                    System.out.println("Введите description новой задачи: ");
                    myDescription = scanner.nextLine();
                    System.out.println("Введите details новой задачи: ");
                    myDetails = scanner.nextLine();
                    addTask(myDescription, myDetails);
                    break;
                case "1":
                    System.out.println("Введите description нового эпика: ");
                    myDescription = scanner.nextLine();
                    System.out.println("Введите details нового эпика: ");
                    myDetails = scanner.nextLine();
                    addEpic(myDescription, myDetails);
                    break;
                case "2":
                    System.out.println("Введите description новой подзадачи: ");
                    myDescription = scanner.nextLine();
                    System.out.println("Введите details новой подзадачи: ");
                    myDetails = scanner.nextLine();
                    addSubTask(myDescription, myDetails);
                    break;
                case "3":
                    System.out.println("Введите ID обновляемой задачи: ");
                    myDescription = scanner.nextLine();
                    System.out.println("Введите новый details для задачи: ");
                    myDetails = scanner.nextLine();
                    System.out.println("Введите новый статус задачи (NEW, IN_PROGRESS, DONE): ");
                    myStatus = scanner.nextLine();
                    if (myStatus.equals("NEW")) {
                        myTaskStatus = TaskStatus.NEW;
                    }
                    if (myStatus.equals("DONE")) {
                        myTaskStatus = TaskStatus.DONE;
                    }
                    if (myStatus.equals("IN_PROGRESS")) {
                        myTaskStatus = TaskStatus.IN_PROGRESS;
                    }
                    updateTask(myDescription, myDetails, myTaskStatus);
                    break;
                case "4":
                    System.out.println("Введите ID обновляемого эпика: ");
                    myDescription = scanner.nextLine();
                    System.out.println("Введите новый details для эпика: ");
                    myDetails = scanner.nextLine();
                    updateEpic(myDescription, myDetails, myTaskStatus);
                    break;
                case "5":
                    System.out.println("Введите ID обновляемой подзадачи: ");
                    myDescription = scanner.nextLine();
                    System.out.println("Введите новый details для подзадачи: ");
                    myDetails = scanner.nextLine();
                    System.out.println("Введите новый статус подзадачи (NEW, IN_PROGRESS, DONE): ");
                    myStatus = scanner.nextLine();
                    if (myStatus.equals("NEW")) {
                        myTaskStatus = TaskStatus.NEW;
                    }
                    if (myStatus.equals("DONE")) {
                        myTaskStatus = TaskStatus.DONE;
                    }
                    if (myStatus.equals("IN_PROGRESS")) {
                        myTaskStatus = TaskStatus.IN_PROGRESS;
                    }
                    updateSubTask(myDescription, myDetails, myTaskStatus);
                    break;
                case "6":
                    getAllTasks();
                    break;
                case "7":
                    getAllEpics();
                    break;
                case "8":
                    getAllSubTasks();
                    break;
                case "9":
                    getAllTasks();
                    getAllEpics();
                    getAllSubTasks();
                    break;
                case "10":
                    deleteAllTasks();
                    break;
                case "11":
                    System.out.println("Введите ID эпика для вывода его подзадач: ");
                    int epicID = scanner.nextInt();
                    getSubTasksOfEpic(epicID);
                    break;
                case "12":
                    System.out.println("Введите номер задачи для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    deleteTask(indexToManipulate);
                    break;
                case "13":
                    System.out.println("Введите номер эпика для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    deleteEpic(indexToManipulate);
                    break;
                case "14":
                    System.out.println("Введите номер подзадачи для удаления: ");
                    indexToManipulate = scanner.nextInt();
                    deleteSubTask(indexToManipulate);
                    break;
                case "15":
                    return;
            }
        }

    }

    private static void printMenu() {
        System.out.println("------- Меню ------- ");
        System.out.println("0. Добавить задачу");
        System.out.println("1. Добавить эпик");
        System.out.println("2. Добавить подзадачу");
        System.out.println("3. Обновить задачу");
        System.out.println("4. Обновить эпик");
        System.out.println("5. Обновить подзадачу");
        System.out.println("6. Вывести все задачи");
        System.out.println("7. Вывести все эпики");
        System.out.println("8. Вывести все подзадачи");
        System.out.println("9. Вывести все задачи, эпики и подзадачи");
        System.out.println("10. Удалить все задачи, эпики и подзадачи");
        System.out.println("11. Получить список подзадач эпика");
        System.out.println("12. Удалить задачу по ID");
        System.out.println("13. Удалить эпик по ID");
        System.out.println("14. Удалить подзадачу по ID");
        System.out.println("15. Выход");
    }

    public static void addTask(String taskDescription, String taskDetails) {//Функция добавления здачи
        Task newTask = new Task(taskDescription, taskDetails, TaskStatus.NEW);
        taskmanager.createTask(newTask);
    }

    public static void addSubTask(String subtaskDescription, String subtaskDetails) {//Функция добавления подздачи
        SubTask newSubTask = new SubTask(subtaskDescription, subtaskDetails, TaskStatus.NEW, 6377);//<-ВОТ ТУТ НАДО РОДИТЕЛЯ УКАЗАТЬ
        taskmanager.createSubTask(newSubTask);
    }

    public static void addEpic(String epicDescription, String epicDetails) {//Функция добавления эпика
        Epic newEpic = new Epic(epicDescription, epicDetails, TaskStatus.NEW);
        taskmanager.createEpic(newEpic);
    }

    public static void updateTask(String taskDescription, String taskDetails, TaskStatus taskStatus) {//Функция обновления задач
        Task updatedTask = new Task(taskDescription, taskDetails, taskStatus);
        taskmanager.updateTask(updatedTask);
    }

    public static void updateEpic(String epicDescription, String epicDetails, TaskStatus epicStatus) {//Функция обновления задач
        Epic updatedTask = new Epic(epicDescription, epicDetails, epicStatus);
        taskmanager.updateEpic(updatedTask);
    }

    public static void updateSubTask(String subtaskDescription, String subtaskDetails, TaskStatus subtaskStatus) {//Функция обновления подзадач
        SubTask updatedSubTask = new SubTask(subtaskDescription, subtaskDetails, subtaskStatus, 0);//Создаём экземпляр подзадачи и передаём в таскменеджер для нахождения и обновления соответсвующей подзадачи
        taskmanager.updateSubTask(updatedSubTask);
    }

    public static void getAllTasks() {//Функция вывода задач и подзадач
        taskmanager.getAllTasks();
    }

    public static void getAllSubTasks() {//Функция вывода задач и подзадач
        taskmanager.getAllSubTasks();
    }

    public static void getAllEpics() {//Функция вывода задач и подзадач
        taskmanager.getAllEpics();
    }

    public static void getSubTasksOfEpic(int epic) {//Функция получения всех подзадач заданного эпика
        ArrayList<SubTask> listOfSubTasks = taskmanager.getSubTasksOfEpic(epic);
        System.out.println("Список подзадач выбранного эпика: " + listOfSubTasks);

    }

    public static void deleteAllTasks() {//Удаление всех задач, эпиков и подзадач
        taskmanager.deleteAllTasks();
    }

    public static void deleteTask(Integer taskIDToDelete) {//Удаление конкретной задачи
        taskmanager.deleteTask(taskIDToDelete);
    }

    public static void deleteEpic(Integer taskIDToDelete) {//Удаление конкретной задачи
        taskmanager.deleteEpic(taskIDToDelete);
    }

    public static void deleteSubTask(Integer subtaskIDToDelete) {//Удаление конкретной подзадачи
        taskmanager.deleteSubTask(subtaskIDToDelete);
    }

}
