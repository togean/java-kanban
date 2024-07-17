package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> listOfSubtasks = new ArrayList<>();
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");


    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    private LocalDateTime endTime;

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
                ", startDate=" + super.getStartDateTime().format(DATE_TIME_FORMATTER) +
                ", duration=" + super.getDuration().toMinutes() +
                ", EndTime=" + endTime.format(DATE_TIME_FORMATTER) +
                '}';
    }

    public void setListOfTasks(ArrayList<Integer> listOfTasks) {
        this.listOfSubtasks = listOfTasks;
    }

    public Epic(String description, String details, LocalDateTime taskStartDate, Duration taskDuration) {
        super(description, details, taskStartDate, taskDuration);
        this.endTime = calculateTaskEndDateTime();
    }
}
