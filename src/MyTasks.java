import java.util.ArrayList;
public class MyTasks {
    public static TaskManager taskmanager = new TaskManager();
    public static void main(String[] args) {
        System.out.println("------- Создаем задачи------- ");
        addTask("Поход в магазин", "Купить хлеб и молоко", TasksTypes.TASK, null);
        addTask("Поездка в горы 1", "Собрать рюкзак", TasksTypes.TASK, null);
        addTask("Поездка в горы", "Собрать рюкзак 2", TasksTypes.TASK, null);//Для проверки на выявление повторов
        addTask("Пройти курс JAVA", "Выучить язык программирования JAVA", TasksTypes.TASK, null);

        addTask("Взять кошелёк", "Взять в магазин кошелёк с нужной суммой", TasksTypes.SUBTASK, "Поход в магазин");
        addTask("Выучить ArrayList", "Выучить структуру ArrayList", TasksTypes.SUBTASK, "Пройти курс JAVA");
        addTask("Выучить ArrayList 2", "Выучить структуру ArrayList 2", TasksTypes.SUBTASK, "Пройти курс JAVA");//Для проверки на дубляж подзадач в задаче
        addTask("Выучить HashMap", "Выучить структуру HashMap", TasksTypes.SUBTASK, "Пройти курс JAVA");

        System.out.println("------- Вывод списка всех задач и подзадач -------");
        getAllTasks();

        System.out.println("------- Вывод списка всех подзадач эпика \"Пройти курс JAVA\"-------");
        getSubTasksOfEpic("Пройти курс JAVA");//Вызов списка выбранного эпика - в данном случае для примера выводим подзадачи эпика "Пройти курс JAVA"

        System.out.println("------- Удаление задачи по ID (для примера берём ID=1) -------");
        deleteTask(1);//Удаление задачи с id=1 (как пример)
        deleteTask(1);//Повторная попытка удаления задачи с тем же ID, для проверки что не будет ошибки

        System.out.println("------- Удаление подзадачи по ID (для примера берём ID=1) -------");
        deleteSubTask(1);//Удаление подзадачи с id=1;(как пример)
        deleteSubTask(1);//Повторная попытка удаления подзадачи с тем же ID, что и ранее для проверки что не будет ошибки

        System.out.println("------- Вывод списка всех задач и подзадач после удалений по ID-шкам (1) -------");
        getAllTasks();//Для вывода того, что осталось после вызова остальных функций

        System.out.println("------- Смотрим, стал ли эпик снова обычной задачей (поход в магазин, у него не останется подзадач) - удаляем подзадачу c ID=0  -------");
        deleteSubTask(0);
        getAllTasks();//Для вывода того, что осталось после удаления подзадачи 0

        System.out.println("------- Обновляем задачи \"Поход в магазин\" и \"Выучить HashMap\" -------");
        updateTask("Поход в магазин", "Купить хлеб, молоко и кефир", TasksTypes.TASK, TaskStatuses.IN_PROGRESS);//Обновляем задачу
        updateSubTask("Выучить HashMap", "Выучить структуру HashMap очень хорошо", TasksTypes.SUBTASK, TaskStatuses.IN_PROGRESS, "Пройти курс JAVA");//Обновляем задачу
        getAllTasks();//Для вывода того, что осталось после обновления задач

        System.out.println("------- Обновление статуса подзадач на \"DONE\" -------");
        updateSubTask("Выучить ArrayList 2","Выучить структуру ArrayList",TasksTypes.SUBTASK,TaskStatuses.DONE, "Пройти курс JAVA");
        updateSubTask("Выучить HashMap","Выучить структуру HashMap очень хорошо",TasksTypes.SUBTASK,TaskStatuses.DONE, "Пройти курс JAVA");
        getAllTasks();//Вывод всех задач и подзадач после обновления подзадачи

        System.out.println("------- Обновление статуса задач после обновления подзадачи на DONE -------");
        recalculateStatuses();//Вывод пересчёта задач на основе обновлений
        getAllTasks();//Вывод всех задач и подзадач после обновлений

        System.out.println("------- Обновление статуса предыдущих подзадач на \"NEW\" -------");
        updateSubTask("Выучить ArrayList 2","Выучить структуру ArrayList",TasksTypes.SUBTASK,TaskStatuses.NEW, "Пройти курс JAVA");
        updateSubTask("Выучить HashMap","Выучить структуру HashMap очень хорошо",TasksTypes.SUBTASK,TaskStatuses.NEW, "Пройти курс JAVA");
        getAllTasks();//Вывод всех задач и подзадач после обновления подзадачи

        System.out.println("------- Обновление статуса задач после обновления подзадачи на NEW -------");
        recalculateStatuses();//Вывод пересчёта задач на основе обновлений
        getAllTasks();//Вывод всех задач и подзадач после обновлений

        System.out.println("------- Вывод списка всех задач и подзадач после полного удаления-------");
        deleteAllTasks();
        getAllTasks();//Для вывода того, что осталось после удления всех задач

    }

    public static void addTask(String taskDescription, String taskDetails, TasksTypes taskType, String parentTaskDescription){//Функция добавления здач
        taskmanager.createTask(taskDescription,taskDetails, taskType, parentTaskDescription);
    }
    public static void recalculateStatuses(){//Функция пересчёта статусов
        taskmanager.recalculateOrUpdateTaskStatus();
    }
    public static void updateTask(String taskDescription, String taskDetails, TasksTypes taskType, TaskStatuses taskStatus){//Функция обновления задач
        Tasks updatedTask;
        updatedTask = new Tasks(taskDescription,  taskDetails, taskStatus, taskType);
        taskmanager.updateTask(updatedTask);
    }
    public static void updateSubTask(String taskDescription, String taskDetails, TasksTypes taskType, TaskStatuses taskStatus, String parentDescription){//Функция обновления подзадач
        Integer subtaskParentID = taskmanager.getParentIDForSubTask(parentDescription);//Получаем ID родительской задачи, т.к. это нужно при создании экземпляра подзадачи, ведь при изменении описания радительской задачи, подзадача "подцепится к другой родительской задаче"
        SubTasks updatedSubTask;
        updatedSubTask = new SubTasks(taskDescription,  taskDetails, taskStatus, taskType,subtaskParentID);//Создаём экземпляр подзадачи и передаём в таскменеджер для нахождения и обновления соответсвующей подзадачи
        taskmanager.updateSubTask(updatedSubTask);
    }
    public static void getAllTasks(){//Функция вывода задач и подзадач
        taskmanager.getAllTasks();
    }
    public static void getSubTasksOfEpic(String epicDescription){//Функция получения всех подзадач заданного эпика
        ArrayList<SubTasks> listOfSubTasks = taskmanager.getSubTasksOfEpic(epicDescription);
        System.out.println("Список подзадач выбранного эпика: "+listOfSubTasks.toString());

    }
    public static void deleteAllTasks(){//Удаление всех задач и подзадач
        taskmanager.deleteAllTasks();
    }
    public static void deleteTask(Integer taskIDToDelete){//Удаление конкретной задачи
        taskmanager.deleteTask(taskIDToDelete);
    }
    public static void deleteSubTask(Integer subtaskIDToDelete){//Удаление конкретной подзадачи
        taskmanager.deleteSubTask(subtaskIDToDelete);
    }
}
