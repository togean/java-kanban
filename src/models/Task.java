package models;

import java.util.Objects;

public abstract class Task {
    private String description;
    private String details;
    private TaskStatus taskStatus;

    private Integer id;

    public Task(String description, String details) {
        this.description = description;
        this.details = details;
//        this.id = id;
        this.taskStatus = TaskStatus.NEW;
    }

    public String getDescription() {
        return description;
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

    public void setDescription(String description) {
        this.description = description;
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
                '}';
    }
}
