package playerquests.utility.exception;

/**
 * Exception to throw when a quest action is missing.
 */
public class MissingActionException extends RuntimeException {

    /**
     * Constructor that accepts a message and a cause
     * @param message the message of how the exception happened.
     * @param cause the message of why the exception happened.
     */
    public MissingActionException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
