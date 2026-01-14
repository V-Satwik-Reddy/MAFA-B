package majorproject.maf.exception.auth;

public class InvalidEmailOrPasswordFormatException extends RuntimeException {
    public InvalidEmailOrPasswordFormatException(String message) {
        super(message);
    }
}
