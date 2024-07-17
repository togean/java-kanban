package exceptions;

public class DescriptionIsEmptyException extends RuntimeException {
    public DescriptionIsEmptyException(String message) {
        super(message);
    }
}
