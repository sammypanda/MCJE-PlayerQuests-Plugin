package playerquests.utility.exception;

/**
 * Exception to throw when a QuestStage is missing.
 */
public class MissingStageException extends RuntimeException {

    /**
     * Constructor that accepts a message and a cause
     * @param message the message of how the exception happened.
     * @param cause the message of why the exception happened.
     */
    public MissingStageException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
