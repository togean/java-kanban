package models;

import java.time.Duration;
import java.time.LocalDateTime;

public class StandardTask extends Task {
    private final String standarttaskDetails;

    public StandardTask(String description, String standarttaskDetails, LocalDateTime taskStartDate, Duration taskDuration) {

        super(description, standarttaskDetails, taskStartDate, taskDuration);
        this.standarttaskDetails = standarttaskDetails;
    }
}
