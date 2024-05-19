package models;

public class SubTask extends Task{
    private Integer parentID;

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
                '}';
    }

    public SubTask(String description, String details, Integer parentID) {
        super(description, details);
        setParentID(parentID);
    }
}
