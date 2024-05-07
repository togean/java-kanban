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
    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }
    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public Task(String taskDescription, String taskDetails) {
        this.taskDescription = taskDescription;
        this.taskDetails = taskDetails;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getTaskDetails() {
        return taskDetails;
    }


    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public int hashCode(){
        int hash = 17;
        if(taskDescription!=null){
            hash = hash + taskDescription.hashCode();
        }
        if(taskDetails!=null){
            hash = hash + taskDetails.hashCode();
        }
        if(hash<0){
            hash=hash*(-1);
        }
        return hash;
    }
    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj==null) return false;
        if(this.getClass()!=obj.getClass()) return false;
        Task task = (Task) obj;
        return Objects.equals(taskDescription, task.taskDescription) &&
                Objects.equals(taskDetails, task.taskDetails) &&
                (taskIndex.equals(task.taskIndex));
    }
    @Override
    public String toString() {
        String result;
        result = "Задача: " + this.taskDescription + ", описание: " + this.taskDetails + ", тип задачи:TASK, ID: " + this.taskIndex+", статус задачи: " + this.taskStatus;
        return result;
    }
}
