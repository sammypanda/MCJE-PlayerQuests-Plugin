package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List; // generic list type
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicnpctypes;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.quest.action.QuestAction;
import playerquests.client.ClientDirector;
import playerquests.client.quest.QuestClient;

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
     * Refunds the resources used for the NPC.
     * @param quester the player to refund the resources to
     */
    @JsonIgnore
    public abstract void refund(QuestClient quester);

    /**
     * Penalizes the resources used for the NPC.
     * @param quester the player to penalize
     */
    @JsonIgnore
    public abstract void penalise(QuestClient quester);

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

    /**
     * The button to set the NPC location in the world.
     * @param screen the screen the button shows on
     * @param director the director to act on behalf of
     * @param gui the gui which holds the slot where the button shows
     * @param slot the slot position of the gui the button shows
     * @param npc the npc being modified
     */
    public abstract GUISlot createPlaceSlot(Dynamicnpctypes screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc);

    /**
     * Unregisters an NPC without despawning.
     * Use {@link QuestNPC#despawn(QuestClient)} instead
     * @param action the action the NPC is a part of
     * @param quester the quest client to unregister the NPC from the {@link playerquests.builder.quest.data.QuesterData}
     */
    protected abstract void unregister(QuestAction action, QuestClient quester);

    /**
     * Despawns an NPC without unregistering.
     * Use {@link QuestNPC#despawn(QuestClient)} instead
     * @param action the action the NPC is a part of
     * @param quester the quest client to despawn the NPC from the {@link playerquests.builder.quest.data.QuesterData} of
     */
    protected abstract void despawn(QuestAction action, QuestClient quester);

    /**
     * Registers an NPC unspawned.
     * Use {@link QuestNPC#spawn(QuestClient)} instead
     * @param action the action the NPC is a part of
     * @param quester the quest client to register the NPC into the {@link playerquests.builder.quest.data.QuesterData} of
     * @param value the special value that identifies the NPC in the world
     */
    protected abstract void register(QuestAction action, QuestClient quester, Object value);

    /**
     * Spawns an NPC untracked/unregistered.
     * Use {@link QuestNPC#spawn(QuestClient)} instead
     * @param action the action the NPC is a part of
     * @param quester the quest client to 'show' the NPC to
     * @return the value that identifies the NPC in the world
     */
    protected abstract Object spawn(QuestAction action, QuestClient quester);
}
