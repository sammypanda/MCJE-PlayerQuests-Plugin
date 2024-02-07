package playerquests.builder.quest.component.npc.type;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Passes and handles the quest npc 'types'.
 * <p>
 * Quest NPCs are how quests are interacted
 * with in-game.
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
        this.npcTypes = NPCType.allNPCTypes();
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
     * See docs/README for list of NPC types.
    */
    public NPCType(String value) {
        this.value = value;
    }

    /**
     * Shows a list of all the NPC types that could be added to a quest.
     * @return list of every NPC type
     */
    public static List<String> allNPCTypes() {
        List<String> npcTypes = new ArrayList<>();

        npcTypes.add("Block");

        return npcTypes;
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
