package playerquests.builder.quest.npc;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty; // well-formed serialisation

import playerquests.builder.quest.data.LocationData; // describes location of a playerquests item

/**
 * Passes and handles the quest npc 'types'.
 * <p>
 * Quest NPCs are how quests are interacted
 * with in-game.
 */
public class NPCType {

    /**
     * The NPC this BlockNPC type belongs to.
     */
    @JsonIgnore
    protected QuestNPC npc;

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
     * @param value the type-specific value used to customise the NPC
     * @param npc the npc details
    */
    public NPCType(String value, QuestNPC npc) {
        this.value = value;
        this.npc = npc;
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

    /**
     * Place the NPC in the world.
     */
    public void place() {
        throw new IllegalStateException("Tried to place an NPC that has not been given a type. (or the type has not correctly overriden the place method)");
    }

    /**
     * Remove the NPC from the world.
     */
    public void remove() {
        throw new IllegalStateException("Tried to remove an NPC that has not been given a type. (or the type has not correctly overriden the place method)");
    }
}
