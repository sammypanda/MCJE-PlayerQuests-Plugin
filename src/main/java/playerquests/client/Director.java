package playerquests.client;

/**
 * Represents a generic director in the PlayerQuests plugin.
 * <p>
 * The {@code Director} class is an abstract class that defines the basic structure
 * for different types of directors used within the plugin. Directors are responsible
 * for various tasks and functionalities related to the plugin's operation.
 * </p>
 */
public abstract class Director {

    /**
     * Abstract constructor should not be used directly, use the subclasses.
     */
    protected Director() {}

    /**
     * Closes the director and performs any necessary cleanup.
     * <p>
     * This method is meant to be implemented by subclasses to provide the specific
     * logic for closing and cleaning up resources related to the director. It ensures
     * that all resources are properly released and no memory leaks occur.
     * </p>
     */
    public abstract void close();
}
