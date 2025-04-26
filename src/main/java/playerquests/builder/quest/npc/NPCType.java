package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List; // generic list type
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.client.ClientDirector;

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
    @JsonSubTypes.Type(value = BlockNPC.class, name = "BlockNPC"),
    @JsonSubTypes.Type(value = EntityNPC.class, name = "EntityNPC")
})
public abstract class NPCType {

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
     * @param value the type-specific value used to customize the NPC
     * @param npc the QuestNPC instance
     */
    public NPCType(String value, QuestNPC npc) {
        this.value = value;
        this.npc = npc;
    }

    /**
     * Provides a list of all possible NPC types that can be added to a quest.
     * @return a list of all NPC types
     */
    @SuppressWarnings("unchecked") // it is checked :)
    public static List<Class<? extends NPCType>> getAllTypes() {
        JsonSubTypes jsonSubTypes = NPCType.class.getDeclaredAnnotation(JsonSubTypes.class);

        return Arrays.stream(jsonSubTypes.value())
            .map(type -> type.value())
            .filter(clazz -> NPCType.class.isAssignableFrom(clazz)) // Type check
            .map(clazz -> (Class<? extends NPCType>) clazz) // Safe cast
            .collect(Collectors.toList());
    }

    /**
     * Gets the string representation of the NPC type.
     * @return the simple name of the class representing the NPC type
     */
    @JsonIgnore
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets the QuestNPC associated with this BlockNPC.
     * 
     * @return the QuestNPC object
     */
    public QuestNPC getNPC() {
        return this.npc;
    }

    /**
     * Returns the value of this NPC type.
     * @return the value associated with this NPC type, such as the block name for a BlockNPC
     */
    public String getValue() {
        return value;
    }

    /**
     * Places the NPC in the world.
     * @param player the player who can see the placement
     */
    @JsonIgnore
    abstract public void place(Player player);

    /**
     * Removes the NPC from the world.
     */
    @JsonIgnore
    public abstract void remove();

    /**
     * Removes the NPC for a player.
     * @param player the player to remove the NPC from
     */
    @JsonIgnore
    public abstract void remove(Player player);

    /**
     * Refunds the resources used for the NPC.
     * @param player the player to refund the resources to
     */
    @JsonIgnore
    public abstract void refund(Player player);

    /**
     * Penalizes the resources used for the NPC.
     * @param player the player to penalize
     */
    @JsonIgnore
    public abstract void penalise(Player player);

    /**
     * The button to change the NPC to this type.
     * @param screen the screen the button shows on
     * @param director the director to act on behalf of
     * @param gui the gui which holds the slot where the button shows
     * @param slot the slot position of the gui the button shows
     * @param npc the npc being modified
     * @return
     */
    public abstract GUISlot createTypeSlot(GUIDynamic screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc);
}
