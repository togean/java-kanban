package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public abstract class Task {


    private String description;
    private String details;
    private TaskStatus taskStatus;
    private Duration duration;
    private LocalDateTime startDateTime;
    private Integer id;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }


    public LocalDateTime calculateTaskEndDateTime() {
        return startDateTime.plusMinutes(duration.toMinutes());
    }

    public Task(String description, String details, LocalDateTime taskStartDate, Duration taskDuration) {

        this.startDateTime = taskStartDate;
        this.duration = taskDuration;
        this.description = description;
        this.details = details;
        this.taskStatus = TaskStatus.NEW;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(description, task.description) && Objects.equals(details, task.details) && taskStatus == task.taskStatus && Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, details, taskStatus, id);
    }

    @Override
    public String toString() {
        return "models.Task{" +
                "description='" + description + '\'' +
                ", details='" + details + '\'' +
                ", status='" + taskStatus + '\'' +
                ", id=" + id +
                ", startDate=" + startDateTime.format(DATE_TIME_FORMATTER) +
                ", duration=" + duration.toMinutes() +
                '}';
    }
}
