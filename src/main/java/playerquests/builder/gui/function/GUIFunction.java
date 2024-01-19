package playerquests.builder.gui.function;

import java.util.ArrayList; // used to store the params for this meta action

/**
 * Passes and handles the GUI 'Functions' (otherwise known as 'Meta Actions') called by a GUI.
 * <p>
 * GUI functions are pre-defined functions that make
 * it possible to do more with GUI templates. They
 * generally simplify more complex operations.
 */
public abstract class GUIFunction {

    /**
     * the params passed into this function
     */
    protected ArrayList<Object> params;

    /**
     * Set the params for this function to use when it executes.
     * @param params the expected params for the specific meta action.
     */
    public void setParams(ArrayList<Object> params) {
        this.params = params;
    }
}
