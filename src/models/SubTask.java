package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubTask extends Task {
    private Integer parentID;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");

    public Integer getParentID() {
        return parentID;
    }

    public void setParentID(Integer parentID) {
        this.parentID = parentID;
    }

    @Override
    public String toString() {
        return "models.SubTask{" +
                "description='" + super.getDescription() + '\'' +
                ", status='" + super.getTaskStatus() + '\'' +
                ", details='" + super.getDetails() + '\'' +
                ", id=" + super.getId() +
                ", parentID=" + parentID +
                ", startDate=" + super.getStartDateTime().format(DATE_TIME_FORMATTER) +
                ", duration=" + super.getDuration().toMinutes() +
                '}';
    }

    public SubTask(String description, String details, Integer parentID, LocalDateTime taskStartDate, Duration taskDuration) {
        super(description, details, taskStartDate, taskDuration);
        setParentID(parentID);
    }
}
