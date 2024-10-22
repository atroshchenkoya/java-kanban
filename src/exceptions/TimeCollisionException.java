package exceptions;

public class TimeCollisionException extends RuntimeException {

    public TimeCollisionException(String errorMessage) {
        super(errorMessage);
    }
}
