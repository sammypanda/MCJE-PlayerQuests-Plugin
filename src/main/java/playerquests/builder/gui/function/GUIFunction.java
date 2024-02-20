package playerquests.builder.gui.function;

import java.util.ArrayList; // used to store the params for this meta action
import java.util.function.Consumer; // used for onFinish

import playerquests.client.ClientDirector; // powers functionality for functions

/**
 * Passes and handles the GUI 'Functions' (otherwise known as 'Meta Actions') called by a GUI.
 * <p>
 * GUI functions are pre-defined functions that make
 * it possible to do more with GUI templates. They
 * generally simplify more complex operations.
 */
public abstract class GUIFunction {

    /**
     * the params passed into this function.
     */
    protected ArrayList<Object> params;

    /**
     * director which powers functionality.
     */
    protected ClientDirector director;

    /**
     * if this function has errored.
     */
    protected Boolean errored = false;

    /**
     * code that can be set to run when this function is finished.
     */
    private Consumer<GUIFunction> onFinish;

    /**
     * Not intended to be created directly, is abstract class for GUI functions.
     * <p>
     * See docs/README for list of GUI functions.
     * @param params the list of parameters for a function
     * @param director client director for the function to be able to control the plugin
    */
    public GUIFunction(ArrayList<Object> params, ClientDirector director) {
        this.params = params;
        this.director = director;
    }

    /**
     * Method to be overridden by each meta action class.
     */
    public abstract void execute();

    /**
     * Sets code to be executed when the function is finished.
     * @param onFinish the code to run when the function completes
     */
    public GUIFunction onFinish(Consumer<GUIFunction> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    /**
     * Run the code set to run when the function finishes.
     * @see #onFinish(Runnable)
     */
    public void finished() {
        if (this.onFinish != null) {
            onFinish.accept(this);
        }
    }

    /**
     * Set the params for this function to use when it executes.
     * @param params the expected params for the specific meta action.
     */
    public void setParams(ArrayList<Object> params) {
        this.params = params;
    }

    /**
     * Director used to power functionality.
     * @param director the client director.
     */
    public void setDirector(ClientDirector director) {
        this.director = director;
    }
}
