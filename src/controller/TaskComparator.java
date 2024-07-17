package controller;

import models.Task;

import java.util.Comparator;

class TaskComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        boolean comparation = o1.getStartDateTime().isBefore(o2.getStartDateTime());
        int result = 0;
        if (comparation) {
            result = -1;
        } else {
            result = 1;
        }
        return result;
    }
}