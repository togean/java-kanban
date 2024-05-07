import java.util.ArrayList;
public class Epic extends Task{
    private ArrayList<Integer> epicSubtasks;
    public ArrayList<Integer> getEpicSubtasks() {
        return epicSubtasks;
    }

    public void setEpicSubtasks(ArrayList<Integer> epicSubtasks) {
        this.epicSubtasks = epicSubtasks;
    }


    public Epic(String taskDescription, String taskDetails){
        super(taskDescription, taskDetails);
        epicSubtasks = new ArrayList<>();
    }
    @Override
    public String toString() {
        String result;
        result = "Задача: " + super.getTaskDescription() + ", описание: " + super.getTaskDetails() + ", тип задачи: EPIC, ID: " + this.taskIndex+", статус задачи: " + this.taskStatus;
        return result;
    }
}
