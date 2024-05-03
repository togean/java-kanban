public class SubTasks extends Tasks{
    private Integer parentTaskID;
    public SubTasks(String taskDescription, String taskDetails, TaskStatuses taskStatus, TasksTypes taskType, int parentTaskID) {
        super(taskDescription,taskDetails,taskStatus,taskType);
        this.parentTaskID = parentTaskID;
    }
    @Override
    public String toString() {
        String result;
        result = "Подзадача: " + super.getTaskDescription() + ", описание: "+super.getTaskDetails()+", тип задачи:"+TasksTypes.SUBTASK+", статус задачи: "+ super.getTaskStatus();
        result = result + ", ID родительской задачи: "+this.parentTaskID;
        return result;
    }

    public Integer getParentTaskID() {
        return parentTaskID;
    }

    public void setParentTaskID(Integer parentTaskID) {
        this.parentTaskID = parentTaskID;
    }
}
