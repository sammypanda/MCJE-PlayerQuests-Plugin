package playerquests.builder.quest.component.npc.type;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Passes and handles the quest stage action 'types'.
 * <p>
 * Quest actions are pre-defined behaviours that make
 * it possible to do more with quests. They
 * generally simplify more complex operations.
 */
public class NPCType {

    /**
     * The list of NPC types
     */
    private List<String> npcTypes = new ArrayList<>();

    /**
     * Following ran on every instantiation
     */
    {
        // Adding types to the list
        this.npcTypes = NPCType.allActionTypes();
    }

    /**
     * Value of this NPC type.
     * <p>
     * Such as, the block name.
     */
    protected String value;

    /**
     * Not intended to be created directly, is abstract class for NPC types.
     * <p>
     * See docs/README for list of action types.
    */
    public NPCType(String value) {
        this.value = value;
    }

    /**
     * Shows a list of all the action types that could be added to a quest stage.
     * @return list of every action type
     */
    public static List<String> allActionTypes() {
        List<String> actionTypes = new ArrayList<>();

        actionTypes.add("Block");

        return actionTypes;
    }

    @Override
    @JsonProperty("type")
    public String toString() {
        return "NPCType";
    }

    /**
     * Returns the value of this NPC type.
     * @return NPC type value, such as the block name for a Block type NPC
     */
    public String getValue() {
        return value;
    }
}
