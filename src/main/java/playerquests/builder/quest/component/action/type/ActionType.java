package playerquests.builder.quest.component.action.type;

import java.security.InvalidParameterException; // thrown if parameters are malformed or missing
import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import playerquests.builder.gui.GUIBuilder; // for working on GUIs

/**
 * Passes and handles the quest stage action 'types'.
 * <p>
 * Quest actions are pre-defined behaviours that make
 * it possible to do more with quests. They
 * generally simplify more complex operations.
 */
public class ActionType {

    /**
     * The quest templates belonging to no-one or this player
     */
    private List<String> actionTypes = new ArrayList<>();

    /**
     * Following ran on every instantiation
     */
    {
        // Adding types to the list
        this.actionTypes = ActionType.allActionTypes();
    }

    /**
     * Params passed into this action.
     */
    protected ArrayList<Object> params;

    /**
     * Not intended to be created directly, is abstract class for action types.
     * <p>
     * See docs/README for list of action types.
    */
    public ActionType(ArrayList<Object> params) {
        this.params = params;
    }

    /**
     * Shows a list of all the action types that could be added to a quest stage.
     * @return list of every action type
     */
    public static List<String> allActionTypes() {
        List<String> actionTypes = new ArrayList<>();

        actionTypes.add("None");
        actionTypes.add("Speak");

        return actionTypes;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    /**
     * Create GUI slots that are options for this action.
     * @param gui the GUI to create the slots in
     * @param deniedSlots a list of slots that cannot have the option buttons set on
     */
    public void putOptionSlots(GUIBuilder gui, List<Integer> deniedSlots) {
        // error if GUI is not defined
        if (gui == null) {
            throw new InvalidParameterException("GUI missing to put the quest action options slots in.");
        }

        // error if trying to access this class directly instead of by an extended member
        if (this.getClass().getSimpleName().equals("ActionType")) {
            throw new IllegalStateException("Tried to build option slots without defining the type of action.");
        }
    }
}
