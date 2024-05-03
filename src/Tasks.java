public class Tasks {
    //
    //Для задач не создаю отдельное поле индекса, так как HashMap и так будет содержать уникальное поле индекса каждой задачи
    //
    private String taskDescription;
    private String taskDetails;
    boolean isTaskEpic;
    TaskStatuses taskStatus;

    public Tasks(String taskDescription, String taskDetails, TaskStatuses taskStatus, TasksTypes taskType) {
        this.taskDescription = taskDescription;
        this.taskDetails = taskDetails;
        this.taskStatus = taskStatus;
        if(taskType.equals(TasksTypes.EPIC)){
            isTaskEpic = true;
        }else{
            isTaskEpic = false;
        }
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public TaskStatuses getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatuses taskStatus) {
        this.taskStatus = taskStatus;
    }

    public boolean isTaskEpic() {
        return isTaskEpic;
    }

    public void setTaskEpic(boolean taskEpic) {
        isTaskEpic = taskEpic;
    }

    @Override
    public String toString() {
        String result;
        result = "Задача: " + this.taskDescription + ", описание: "+this.taskDetails+", тип задачи:"+(isTaskEpic?TasksTypes.EPIC:TasksTypes.TASK)+", статус задачи: "+ this.taskStatus;
        return result;
    }
}
