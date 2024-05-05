public class SubTask extends Task {
    private Integer parentTaskID;

    public SubTask(String taskDescription, String taskDetails, TaskStatus taskStatus, int parentTaskID) {
        super(taskDescription, taskDetails, taskStatus);
        this.parentTaskID = parentTaskID;
    }

    @Override
    public String toString() {
        String result;
        result = "Подзадача: " + super.getTaskDescription() + ", описание: " + super.getTaskDetails() + ", тип задачи:SUBTASK, ID: " + this.taskIndex+ ", статус задачи: " + super.getTaskStatus();
        result = result + ", ID родительской задачи: " + this.parentTaskID;
        return result;
    }

    public Integer getParentTaskID() {
        return parentTaskID;
    }

    public void setParentTaskID(Integer parentTaskID) {
        this.parentTaskID = parentTaskID;
    }
}
