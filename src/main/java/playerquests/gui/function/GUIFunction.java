package playerquests.gui.function;

import java.util.ArrayList; // used to store the params for this meta action
import java.util.Objects; // used to require params be not null
import java.util.stream.IntStream; // used to validate params are the correct types

import org.bukkit.entity.HumanEntity; // used to set the player

import playerquests.gui.GUI; // used to refer to the function and the slot parent GUI instance

/**
 * Passes and handles the GUI 'Functions' (otherwise known as 'Meta Actions') called by a GUI.
 */
public abstract class GUIFunction {

    /**
     * the GUI this function was spawned from
     */
    protected GUI parentGui;

    /**
     * the params passed into this function
     */
    protected ArrayList<Object> params;

    /**
     * the player the function should execute for
     */
    protected HumanEntity player;

    /**
     * Set the params for this function to use when it executes.
     * @param params the expected params for the specific meta action.
     */
    public void setParams(ArrayList<Object> params) {
        this.params = params;
    }

    /**
     * Setting the GUI that the GUI Function belongs to.
     * @param parentGui the GUI instance the player is seeing when they trigger the gui function/meta action.
     */
    public void setParentGUI(GUI parentGui) {
        this.parentGui = parentGui;
    }

    /**
     * Setting the player for the GUI Function to run on.
     * @param player the entity who should see the functions execute.
     */
    public void setPlayer(HumanEntity player) {
        this.player = player;
    }
    
    /**
     * Method to be overridden by each meta action class.
     */
    public abstract void execute();

    /**
     * Essential utility which checks that the params suit this meta action. 
     * @param params the values the meta action requires.
     * @param expectedTypes the type of values the meta action requires.
     */
    public void validateParams(ArrayList<Object> params, Class<?>... expectedTypes) {
        Objects.requireNonNull(params, "Params cannot be null");

        // check if the size of the params list is the same as the size of the expectedTypes list
        if (params.size() != expectedTypes.length) {
            throw new IllegalArgumentException("Incorrect number of parameters");
        }

        // check with a filter if any param is not an instance of it's expected type
        IntStream.range(0, params.size())
        .filter(i -> !expectedTypes[i].isInstance(params.get(i)))
        .findFirst()
        .ifPresent(index -> {
            throw new IllegalArgumentException("Parameter at index " + index + " does not match the expected type");
        });
    }
}