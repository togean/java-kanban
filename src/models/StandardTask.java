package models;

import java.time.Duration;
import java.time.LocalDateTime;

public class StandardTask extends Task {

    public StandardTask(String description, String standardTaskDetails, LocalDateTime taskStartDate, Duration taskDuration) {

        super(description, standardTaskDetails, taskStartDate, taskDuration);

    }

}
