package playerquests.builder.quest.npc;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Passes and handles the quest npc 'types'.
 * <p>
 * Quest NPCs are how quests are interacted
 * with in-game.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BlockNPC.class, name = "BlockNPC")
})
public class NPCType {

    /**
     * The NPC this BlockNPC type belongs to.
     */
    @JsonBackReference
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
     * Type of this NPC.
     * <p>
     * Such as, 'Block'.
     */
    protected String type;

    /**
     * Value of this NPC type.
     * <p>
     * Such as, the block name.
     */
    protected String value;

    /**
     * Defaut constructor (for Jackson)
    */
    public NPCType() {}

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

    /**
     * Gets the string representation of the type.
     * @return current action type as a string
     */
    @JsonIgnore
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets list of NPC types.
     * @return list of possible NPC types
     */
    @JsonIgnore
    public List<String> getTypes() {
        return this.npcTypes;
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
