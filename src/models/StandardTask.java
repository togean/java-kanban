package models;

public class StandardTask extends Task {
    private final String standarttaskDetails;

    public StandardTask(String description, String standarttaskDetails) {
        super(description, standarttaskDetails);
        this.standarttaskDetails = standarttaskDetails;
    }
}
