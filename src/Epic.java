public class Epic extends Task{
    public Epic(String taskDescription, String taskDetails, TaskStatus taskStatus){
        super(taskDescription, taskDetails, taskStatus);

    }
    @Override
    public String toString() {
        String result;
        result = "Задача: " + super.getTaskDescription() + ", описание: " + super.getTaskDetails() + ", тип задачи: EPIC, ID: " + this.taskIndex+", статус задачи: " + this.taskStatus;
        return result;
    }
}
