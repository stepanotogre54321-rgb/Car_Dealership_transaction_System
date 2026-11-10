package chbcars.util;

import java.awt.Component;
import java.io.IOException;
import chbcars.ui.Dialogs;

/**
 * One reusable place for error handling. Any button action can be wrapped in
 * ErrorHandler.execute(...) so validation, file and unexpected errors are
 * caught the same way everywhere.
 */
public final class ErrorHandler {

    /** An action that may throw any exception. */
    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    private ErrorHandler() {
    }

    /** Runs the action; returns true when it finished without any error. */
    public static boolean execute(Component parent, Action action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            Dialogs.warn(parent, "Please check your input", e.getMessage());
        } catch (DataException e) {
            logError(e);
            Dialogs.error(parent, "Data Error", e.getMessage());
        } catch (Exception e) {
            logError(e);
            Dialogs.error(parent, "Unexpected Error", "Something went wrong: " + e.getMessage());
        }
        return false;
    }

    /** Writes the error to data/error.log (never throws). */
    public static void logError(Throwable error) {
        try {
            FileHandler.appendLine(FileHandler.path("error.log"),
                    Format.now() + " | " + error.getClass().getSimpleName() + " | " + error.getMessage());
        } catch (IOException ignored) {
            // nothing else can be done if the log itself cannot be written
        }
    }
}
