package chbcars.util;

/** Thrown when reading or writing the text-file database fails. */
public class DataException extends RuntimeException {
    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }
}
