package playerquests.builder.quest.component.action.type;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

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
     * Not intended to be created directly, is abstract class for action types.
     * <p>
     * See docs/README for list of action types.
    */
    public ActionType() {}

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
}
