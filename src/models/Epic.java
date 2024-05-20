package models;

import java.util.ArrayList;
public class Epic extends Task {
    ArrayList<Integer> listOfSubtasks = new ArrayList<>();

    public ArrayList<Integer> getListOfSubtasks() {
        return listOfSubtasks;
    }

    @Override
    public String toString() {
        return "models.Epic{" +
                "description='" + super.getDescription() + '\'' +
                ", status='" + super.getTaskStatus() + '\'' +
                ", details='" + super.getDetails() + '\'' +
                ", id=" + super.getId() +
                ", listOfSubtasks=" + listOfSubtasks +
                '}';
    }

    public void setListOfTasks(ArrayList<Integer> listOfTasks) {
        this.listOfSubtasks = listOfTasks;
    }

    public Epic(String description, String details) {
        super(description, details);
        listOfSubtasks.add(0);
    }
}
