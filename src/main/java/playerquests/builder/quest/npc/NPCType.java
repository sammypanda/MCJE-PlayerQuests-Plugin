package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List; // generic list type

import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Handles the different types of NPCs in quests.
 * 
 * Quest NPCs are the entities with which players interact during quests.
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
     * Default constructor for Jackson serialization.
     */
    public NPCType() {}

    /**
     * Constructs an NPCType with the specified value and associated QuestNPC.
     * 
     * @param value the type-specific value used to customize the NPC
     * @param npc the QuestNPC instance
     */
    public NPCType(String value, QuestNPC npc) {
        this.value = value;
        this.npc = npc;
    }

    /**
     * Provides a list of all possible NPC types that can be added to a quest.
     * 
     * @return a list of all NPC types
     */
    public static List<String> allNPCTypes() {
        return Arrays.asList(
            "Block"
        );
    }

    /**
     * Gets the string representation of the NPC type.
     * 
     * @return the simple name of the class representing the NPC type
     */
    @JsonIgnore
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the value of this NPC type.
     * 
     * @return the value associated with this NPC type, such as the block name for a BlockNPC
     */
    public String getValue() {
        return value;
    }

    /**
     * Places the NPC in the world.
     * @param player the player who can see the placement
     * 
     * @throws IllegalStateException if the method is not overridden in a subclass
     */
    public void place(Player player) {
        throw new IllegalStateException("Tried to place an NPC that has not been given a type. (or the type has not correctly overriden the place method)");
    }

    /**
     * Removes the NPC from the world.
     * 
     * @throws IllegalStateException if the method is not overridden in a subclass
     */
    public void remove() {
        throw new IllegalStateException("Tried to remove an NPC that has not been given a type. (or the type has not correctly overriden the place method)");
    }

    /**
     * Refunds the resources used for the NPC.
     * 
     * @param player the player to refund the resources to
     * @throws IllegalStateException if the method is not overridden in a subclass
     */
    public void refund(Player player) {
        throw new IllegalStateException("Tried to refund an NPC that has not been given a type. (or the type has not correctly overriden the place method)");
    }

    /**
     * Penalizes the resources used for the NPC.
     * 
     * @param player the player to penalize
     * @throws IllegalStateException if the method is not overridden in a subclass
     */
    public void penalise(Player player) {
        throw new IllegalStateException("Tried to consume resources for an NPC that has not been given a type. (or the type has not correctly overriden the place method)");
    }
}
