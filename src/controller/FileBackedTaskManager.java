package controller;

import exceptions.FileToSaveTasksNotFound;
import models.*;

import java.io.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    String fileName;

    public FileBackedTaskManager(String filename) {
        super();
        this.fileName = filename;
        readFromFile();
    }

    private void save() {

        File file = new File(this.fileName);
        try (Writer writer = new FileWriter(file, false)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : super.getListOfStandardTasks().values()) {
                writer.write(task.getId() + "," + TaskTypes.TASK + "," + task.getDescription() + "," + task.getTaskStatus() + "," + task.getDetails() + ",TASK\n");
            }
            for (Epic epic : super.getListOfEpics().values()) {
                writer.write(epic.getId() + "," + TaskTypes.EPIC + "," + epic.getDescription() + "," + epic.getTaskStatus() + "," + epic.getDetails() + ",EPIC\n");
            }
            for (SubTask subtask : super.getListOfSubTasks().values()) {
                writer.write(subtask.getId() + "," + TaskTypes.SUBTASK + "," + subtask.getDescription() + "," + subtask.getTaskStatus() + "," + subtask.getDetails() + "," + subtask.getParentID() + "\n");
            }

        } catch (FileToSaveTasksNotFound ex) {
            throw new RuntimeException("Файл " + this.fileName + " не найден");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

     public void readFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            while (line != null) {
                String[] partsOfLine = line.split(",");
                if (partsOfLine[1].equals("TASK")) {
                    StandardTask loadeStandardtask = new StandardTask(partsOfLine[2], partsOfLine[4]);
                    super.createTask(loadeStandardtask);
                } else if (partsOfLine[1].equals("EPIC")) {
                    Epic loadeEpic = new Epic(partsOfLine[2], partsOfLine[4]);
                    super.createEpic(loadeEpic);
                } else if (partsOfLine[1].equals("SUBTASK")) {
                    SubTask loadedSubTask = new SubTask(partsOfLine[2], partsOfLine[4], Integer.parseInt(partsOfLine[5]));
                    super.createSubtask(loadedSubTask);
                }
                line = reader.readLine();
            }
        } catch (FileToSaveTasksNotFound e) {
            throw new RuntimeException("Файл " + fileName + " не найден");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Integer createTask(StandardTask taskToBeCreated) {
        int createdTaskID = super.createTask(taskToBeCreated);
        save();
        return createdTaskID;
    }

    @Override
    public Integer createSubtask(SubTask taskToBeCreated) {
        int createdTaskID = super.createSubtask(taskToBeCreated);
        save();
        return createdTaskID;
    }

    @Override
    public Integer createEpic(Epic taskToBeCreated) {
        int createdTaskID = super.createEpic(taskToBeCreated);
        save();
        return createdTaskID;
    }

    @Override
    public void deleteSubtask(Integer taskToBeDeleted) {
        super.deleteSubtask(taskToBeDeleted);
        save();
    }

    @Override
    public void deleteTask(Integer taskToBeDeleted) {
        super.deleteTask(taskToBeDeleted);
        save();
    }

    @Override
    public void deleteEpic(Integer taskToBeDeleted) {
        super.deleteEpic(taskToBeDeleted);
        save();
    }

    @Override
    public void deleteAll() {
        for (int i = 0; i < super.getListOfEpics().size(); i++) {
            deleteEpic(i);
        }
        for (int i = 0; i < super.getListOfStandardTasks().size(); i++) {
            deleteTask(i);
        }
        save();
    }

    @Override
    public void updateTask(Integer taskID, String taskNewDetails, TaskStatus taskNewStatus) {
        super.updateTask(taskID, taskNewDetails, taskNewStatus);
        save();
    }

    @Override
    public void updateSubtask(Integer subtaskID, String subtaskNewDetails, TaskStatus subtaskNewStatus) {
        super.updateSubtask(subtaskID, subtaskNewDetails, subtaskNewStatus);
        save();
    }

    @Override
    public void updateEpic(Integer epicID, String epicNewDetails, TaskStatus epicNewStatus) {
        super.updateSubtask(epicID, epicNewDetails, epicNewStatus);
        save();
    }

    @Override
    public StandardTask getTask(Integer taskToBeDisplayedByID) {
        StandardTask task = super.getListOfStandardTasks().get(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public SubTask getSubTask(Integer taskToBeDisplayedByID) {
        SubTask task = super.getListOfSubTasks().get(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(Integer taskToBeDisplayedByID) {
        Epic task = super.getListOfEpics().get(taskToBeDisplayedByID);
        if (task != null) {
            managerForHistory.add(task);
        }
        return task;
    }

}
