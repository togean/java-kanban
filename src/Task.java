import java.util.Objects;

public class Task {
    private String taskDescription;
    private String taskDetails;
    Integer taskIndex;
    TaskStatus taskStatus;

    public Integer getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(Integer taskIndex) {
        this.taskIndex = taskIndex;
    }

    public Task(String taskDescription, String taskDetails, TaskStatus taskStatus) {
        this.taskDescription = taskDescription;
        this.taskDetails = taskDetails;
        this.taskStatus = taskStatus;
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

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass()) return false;
        Task task = (Task) obj;
        return Objects.equals(taskDescription, task.taskDescription) &&
                Objects.equals(taskDetails, task.taskDetails) &&
                (taskIndex==task.taskIndex);
    }
    @Override
    public String toString() {
        String result;
        result = "Задача: " + this.taskDescription + ", описание: " + this.taskDetails + ", тип задачи:TASK, ID: " + this.taskIndex+", статус задачи: " + this.taskStatus;
        return result;
    }
}
