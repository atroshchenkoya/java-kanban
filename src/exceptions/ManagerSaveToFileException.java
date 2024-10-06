package exceptions;

public class ManagerSaveToFileException extends RuntimeException {

    public ManagerSaveToFileException(Throwable throwable) {
        super(throwable);
    }
}
