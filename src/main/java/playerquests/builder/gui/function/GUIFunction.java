package playerquests.builder.gui.function;

import java.util.List; // used to store the params for this gui function
import java.util.function.Consumer; // used for onFinish

import playerquests.client.ClientDirector; // powers functionality for functions

/**
 * Represents a GUI function (also known as a gui function) that is triggered by a graphical user interface (GUI).
 * <p>
 * GUI functions are pre-defined operations that simplify complex tasks within a GUI-based application or plugin.
 * These functions are designed to be flexible and extendable, allowing various actions to be executed based on GUI interactions.
 * </p>
 */
public abstract class GUIFunction {

    /**
     * The parameters passed into this function.
     * <p>
     * These parameters are used during the execution of the function to provide the necessary context or data.
     * </p>
     */
    protected List<Object> params;

    /**
     * The director responsible for managing and powering functionality within the plugin or application.
     * <p>
     * The {@link ClientDirector} provides access to various services and controls needed for the function's execution.
     * </p>
     */
    protected ClientDirector director;

    /**
     * Indicates whether the function has encountered an error.
     * <p>
     * This boolean flag helps in tracking the success or failure of the function's execution.
     * </p>
     */
    protected Boolean errored = false;

    /**
     * Code that is set to run when this function is finished.
     * <p>
     * This allows for custom actions to be performed once the function has completed its primary task.
     * </p>
     */
    private Consumer<GUIFunction> onFinish;

    /**
     * Constructs a new GUI function with specified parameters and director.
     * <p>
     * This constructor initializes the function with the provided parameters and director, which are required for its operation.
     * </p>
     * 
     * @param params The list of parameters to be used by the function.
     * @param director The client director used to control the plugin or application.
     */
    protected GUIFunction(List<Object> params, ClientDirector director) {
        this.params = params;
        this.director = director;
    }

    /**
     * Abstract method to be overridden by subclasses to define the specific behavior of the function.
     * <p>
     * Subclasses should implement this method to specify what action the function should perform when executed.
     * </p>
     */
    public abstract void execute();

    /**
     * Sets the code to be executed when the function is finished.
     * <p>
     * This method allows for defining custom actions that should take place once the function has completed its execution.
     * </p>
     * 
     * @param onFinish The code to run when the function completes, implemented as a {@link Consumer} of {@link GUIFunction}.
     * @return The current instance of {@code GUIFunction} for method chaining.
     */
    public GUIFunction onFinish(Consumer<GUIFunction> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    /**
     * Executes the code set to run when the function finishes.
     * <p>
     * This method triggers the {@link #onFinish} code, if it has been set, allowing for post-execution actions to be performed.
     * </p>
     * 
     * @see #onFinish(Consumer)
     */
    public void finished() {
        if (this.onFinish != null) {
            onFinish.accept(this);
        }
    }

    /**
     * Sets the parameters for this function to use during execution.
     * <p>
     * This method allows updating the parameters that the function will use when its {@link #execute} method is called.
     * </p>
     * 
     * @param params The list of parameters to be used by the function.
     */
    public void setParams(List<Object> params) {
        this.params = params;
    }

    /**
     * Sets the director used to power functionality.
     * <p>
     * This method allows updating the {@link ClientDirector} instance that provides control and services for the function.
     * </p>
     * 
     * @param director The client director to be used.
     */
    public void setDirector(ClientDirector director) {
        this.director = director;
    }
}
