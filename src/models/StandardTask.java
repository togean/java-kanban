package models;

public class StandardTask extends Task {
    private final String standarttaskDetails;

    public StandardTask(String description, Integer id, String standarttaskDetails) {
        super(description, id, standarttaskDetails);
        this.standarttaskDetails = standarttaskDetails;
    }
}
