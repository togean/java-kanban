package Exceptions;

public class FileToSaveTasksNotFound extends RuntimeException{
    public FileToSaveTasksNotFound(String message){
        super(message);
    }
}
