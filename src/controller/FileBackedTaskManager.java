package controller;

import exceptions.FileToSaveTasksNotFound;
import models.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    static String fileName;

    public FileBackedTaskManager(String filename) {
        super();
        this.fileName = filename;
    }

    private void save(String filename) {

        File file = new File(filename);
        try (Writer writer = new FileWriter(file, false)) {
            writer.write("id,type,name,status,description,epic,start,duration\n");
            for (Task task : super.getListOfStandardTasks()) {
                writer.write(task.getId() + "," + TaskTypes.TASK + "," + task.getDescription() + "," + task.getTaskStatus() + "," + task.getDetails() + ",TASK," + task.getStartDateTime() + "," + task.getDuration() + "\n");
            }
            for (Epic epic : super.getListOfEpics()) {
                writer.write(epic.getId() + "," + TaskTypes.EPIC + "," + epic.getDescription() + "," + epic.getTaskStatus() + "," + epic.getDetails() + ",EPIC," + epic.getStartDateTime() + "," + epic.getDuration() + "\n");
            }
            for (SubTask subtask : super.getListOfSubTasks()) {
                writer.write(subtask.getId() + "," + TaskTypes.SUBTASK + "," + subtask.getDescription() + "," + subtask.getTaskStatus() + "," + subtask.getDetails() + "," + subtask.getParentID() + "," + subtask.getStartDateTime() + "," + subtask.getDuration() + "\n");
            }

        } catch (FileNotFoundException ex) {
            throw new FileToSaveTasksNotFound("Файл " + this.fileName + " не найден");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FileBackedTaskManager loadFromFile(String fileName) {
        FileBackedTaskManager manager = new FileBackedTaskManager(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            line = reader.readLine();
            while (line != null) {
                manager.createTaskFromString(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            throw new FileToSaveTasksNotFound("Файл " + fileName + " не найден");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return manager;
    }

    private void createTaskFromString(String line) {
        String[] partsOfLine = line.split(",");
        if (partsOfLine[1].equals("TASK")) {
            StandardTask loadeStandardtask = new StandardTask(partsOfLine[2], partsOfLine[4], LocalDateTime.parse(partsOfLine[6]), Duration.parse(partsOfLine[7]));
            super.createTask(loadeStandardtask);
        } else if (partsOfLine[1].equals("EPIC")) {
            Epic loadeEpic = new Epic(partsOfLine[2], partsOfLine[4], LocalDateTime.parse(partsOfLine[6]), Duration.parse(partsOfLine[7]));
            super.createEpic(loadeEpic);
        } else if (partsOfLine[1].equals("SUBTASK")) {
            SubTask loadedSubTask = new SubTask(partsOfLine[2], partsOfLine[4], Integer.parseInt(partsOfLine[5]), LocalDateTime.parse(partsOfLine[6]), Duration.parse(partsOfLine[7]));
            super.createSubtask(loadedSubTask);
        }
        getPrioritizedTasks();//Если были сохранённые задачи, созаём их сорированный список
    }

    @Override
    public Integer createTask(StandardTask taskToBeCreated) {
        int createdTaskID = super.createTask(taskToBeCreated);
        save(fileName);
        return createdTaskID;
    }

    @Override
    public Integer createSubtask(SubTask taskToBeCreated) {
        int createdTaskID = super.createSubtask(taskToBeCreated);
        save(fileName);
        return createdTaskID;
    }

    @Override
    public Integer createEpic(Epic taskToBeCreated) {
        int createdTaskID = super.createEpic(taskToBeCreated);
        save(fileName);
        return createdTaskID;
    }

    @Override
    public void deleteSubtask(Integer taskToBeDeleted) {
        super.deleteSubtask(taskToBeDeleted);
        save(fileName);
    }

    @Override
    public void deleteTask(Integer taskToBeDeleted) {
        super.deleteTask(taskToBeDeleted);
        save(fileName);
    }

    @Override
    public void deleteEpic(Integer taskToBeDeleted) {
        super.deleteEpic(taskToBeDeleted);
        save(fileName);
    }

    @Override
    public void deleteAll() {
        for (int i = 0; i < super.getListOfEpics().size(); i++) {
            deleteEpic(i);
        }
        for (int i = 0; i < super.getListOfStandardTasks().size(); i++) {
            deleteTask(i);
        }
        save(fileName);
    }

    @Override
    public void updateTask(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus) {
        super.updateTask(taskID, taskNewDetails, taskNewStatus);
        save(fileName);
    }

    @Override
    public void updateSubtask(Integer subtaskID, String subtaskNewDetails, TaskStatus subtaskNewStatus) {
        super.updateSubtask(subtaskID, subtaskNewDetails, subtaskNewStatus);
        save(fileName);
    }

    @Override
    public void updateEpic(Integer epicID, String epicNewDetails, TaskStatus epicNewStatus) {
        super.updateSubtask(epicID, epicNewDetails, epicNewStatus);
        save(fileName);
    }

    @Override
    public StandardTask getTask(Integer taskToBeDisplayedByID) {
        StandardTask task = super.getTask(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public SubTask getSubTask(Integer taskToBeDisplayedByID) {
        SubTask task = super.getSubTask(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(Integer taskToBeDisplayedByID) {
        Epic task = super.getEpic(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

}
