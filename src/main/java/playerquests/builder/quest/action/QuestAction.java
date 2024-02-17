package playerquests.builder.quest.action;

import java.security.InvalidParameterException; // thrown if parameters are malformed or missing
import java.util.ArrayList; // array type of list
import java.util.List; // generic list type
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.Core;
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
     * Not intended to be created directly, is abstract class for action types.
     * <p>
     * See docs/README for list of action types.
    */
    public QuestAction() {
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
        return this.getStage().getActions().entrySet().stream()
            .filter(entry -> entry.getValue().equals(this))
            .map(Map.Entry::getKey)
            .findFirst()
            .get();
    }

    /**
     * Gets the stage instance this quest action belongs to.
     * @return a QuestStage instance
     */
    @JsonIgnore
    public QuestStage getStage() {
        Optional<QuestStage> currentStage = Core.getKeyHandler().getInstances().stream() // get all registered instances
            .filter(instance -> instance instanceof QuestStage) // filter out anything that is not a quest stage
            .map(instance -> (QuestStage) instance) // map the quest stage object to QuestStage class type
            .filter(stage -> stage.getActions().containsValue(this)) // check if the actions list in the stage contains this QuestAction
            .findFirst();

        if (currentStage.isPresent()) {
            this.stage = currentStage.get();
            return currentStage.get();
        } 
         
        throw new IllegalStateException("Stray action found. Action: " + this.toString() + " has no parent stage.");
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
