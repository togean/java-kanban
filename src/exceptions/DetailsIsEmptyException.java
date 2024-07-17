package exceptions;

public class DetailsIsEmptyException extends RuntimeException{
    public DetailsIsEmptyException(String message){
        super(message);
    }
}
