package playerquests.builder.gui.function;

import java.util.ArrayList; // used to store the params for this meta action
import java.util.Objects; // used to require params be not null
import java.util.stream.IntStream; // used to validate params are the correct types

import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
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
     * the slot this function belongs to.
     */
    protected GUISlot slot;

    /**
     * director which powers functionality.
     */
    protected ClientDirector director;

    /**
     * if this function has errored.
     */
    protected Boolean errored = false;

    /**
     * Not intended to be created directly, is abstract class for GUI functions.
     * <p>
     * See docs/README for list of GUI functions.
     * @param params the list of parameters for a function
     * @param director client director for the function to be able to control the plugin
     * @param slot the GUI slot this function belongs to
    */
    public GUIFunction(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        this.params = params;
        this.director = director;
        this.slot = slot;
    }

    /**
     * Method to be overridden by each meta action class.
     */
    public abstract void execute();

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

    /**
     * Parent slot this function belongs to.
     * @param slot slot object containing content/functionality
     */
    public void setSlot(GUISlot slot) {
        this.slot = slot;
    }
}
