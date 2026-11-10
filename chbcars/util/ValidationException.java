package chbcars.util;

/** Thrown when the user enters invalid or incomplete data (checked, so it must be handled). */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
