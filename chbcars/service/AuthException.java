package chbcars.service;

import chbcars.util.ValidationException;

/** Login failure. When "terminate" is true the program must close (3 wrong tries). */
public class AuthException extends ValidationException {

    private final boolean terminate;

    public AuthException(String message, boolean terminate) {
        super(message);
        this.terminate = terminate;
    }

    public boolean mustTerminate() {
        return terminate;
    }
}
