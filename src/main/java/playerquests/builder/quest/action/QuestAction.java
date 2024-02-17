package playerquests.builder.quest.action;

import java.security.InvalidParameterException; // thrown if parameters are malformed or missing
import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.gui.GUIBuilder; // for working on GUIs
import playerquests.builder.quest.stage.QuestStage;

/**
 * Passes and handles the quest stage action 'types'.
 * <p>
 * Quest actions are pre-defined behaviours that make
 * it possible to do more with quests. They
 * generally simplify more complex operations.
 */
public class QuestAction {

    /**
     * The parent stage this action belongs to.
     */
    private QuestStage stage;

    /**
     * The ID of this action.
     */
    private String action;

    /**
     * Not intended to be created directly, is abstract class for action types.
     * <p>
     * Use .submit() on this method to add it to it's quest stage.
     * See docs/README for list of action types.
     * @param parentStage stage this action belongs to
    */
    public QuestAction(QuestStage parentStage) {
        this.stage = parentStage;
        this.action = "action_-1";
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
     * Gets the string representation of the type.
     * @return current action type as a string
     */
    @JsonProperty("type")
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /** 
     * Gets this action's ID in the stage.
     * @return current action ID (name)
    */
    @JsonProperty("name")
    public String getID() {
        return this.action;
    }

    /** 
     * Sets this action's ID.
     * @param ID new action ID (name)
    */
    public String setID(String ID) {
        return this.action = ID;
    }
    
    /**
     * Gets the stage this action belongs to.
     * @return current quest stage instance
     */
    @JsonIgnore
    public QuestStage getStage() {
        return this.stage;
    }

    /**
     * Submits this function to the stage.
     * <p>
     * This also gives it a valid ID.
     * @return the quest action submitted to the quest stage
     */
    public QuestAction submit() {
        this.stage.addAction(this);

        return this;
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
