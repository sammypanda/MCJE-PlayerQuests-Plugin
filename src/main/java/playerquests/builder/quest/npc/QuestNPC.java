package playerquests.builder.quest.npc;

import java.util.UUID;

import org.bukkit.Bukkit; // bukkit singleton
import org.bukkit.Material; // for if NPC is a block
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore; // ignore a field when serialising to a JSON object
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty; // specifying property for when serialising to a JSON object

import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.LocationData; // quest entity locations
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;

/**
 * Represents a Non-Player Character (NPC) associated with a quest.
 * 
 * This class provides details about a quest NPC, including its ID, name, assigned type, location, and
 * associated quest. It includes methods for managing and validating the NPC, and for interacting with
 * players in the context of the quest.
 */
public class QuestNPC {

    /**
     * The unique identifier for the NPC.
     * <p>
     * Initialized with an out-of-bounds ID to ensure it is not visible until properly set.
     * </p>
     */
    @JsonProperty("id")
    private String id;

    /**
     * The aesthetic name of the NPC.
     */
    @JsonProperty("name")
    private String name = null;

    /**
     * The quest that this NPC is associated with.
     */
    @JsonBackReference
    private Quest quest;

    /**
     * The client director that manages the plugin.
     */
    @JsonIgnore
    private ClientDirector director;

    /**
     * The type to which this NPC is assigned.
     */
    @JsonProperty("assigned")
    @JsonManagedReference
    private NPCType assigned;

    /**
     * The location of the NPC in the world.
     */
    @JsonProperty("location")
    private LocationData location;

    /**
     * Constructs a new empty QuestNPC.
     */
    public QuestNPC() {
        // Nothing here
    }

    /**
     * Constructs a new QuestNPC with the specified ID.
     * 
     * @param id The unique identifier for the NPC.
     */
    public QuestNPC(@JsonProperty("id") String id) {
        this.id = id;
    }

    /**
     * Gets the unique ID of this NPC.
     * 
     * @return The ID of the NPC.
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets the most appropriate title for this NPC.
     * <p>
     * This is typically the NPC's name.
     * </p>
     * 
     * @return The title of the NPC.
     */
    @JsonIgnore
    public String getTitle() {
        return this.getName();
    }

    @Override
    public String toString() {
        return this.getID();
    }

    /**
     * Gets the aesthetic name of this NPC.
     * 
     * @return The name of the NPC.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the aesthetic name of this NPC.
     * 
     * @param name The new name for the NPC.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Saves the NPC to a quest.
     * <p>
     * If the NPC's ID is 'npc_-1', it will be replaced with the new ID.
     * </p>
     * 
     * @param quest The quest builder to save this NPC to.
     * @param npc The NPC object to be saved.
     * @return True if the NPC was successfully saved, otherwise false.
     */
    @JsonIgnore
    public Boolean save(QuestBuilder quest, QuestNPC npc) {
        // set the parent quest and director
        this.quest = quest.build();
        this.director = quest.getDirector();

        // try to save this NPC to the quest list
        return quest.addNPC(npc);
    }

    /**
     * Sets a new unique ID for this NPC.
     * 
     * @param id The new ID to assign to the NPC.
     */
    @JsonIgnore
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Validates the NPC's properties.
     * <p>
     * Checks if the NPC has a name, is assigned to a type, and has a location. Sends an error message
     * to the quest creator if any properties are missing.
     * </p>
     * 
     * @return True if the NPC is valid, otherwise false.
     */
    @JsonIgnore
    public boolean isValid() {
        UUID questCreator = quest.getCreator();
        Player player = null;
        MessageBuilder response = ChatUtils.message("Something is wrong with a quest NPC") // default message; default sends to console
            .type(MessageType.ERROR)
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN);
        boolean isValid = true; // assume is valid

        if (this.name == null) {
            response.content("The NPC name must be set");
            isValid = false;
        }

        if (this.assigned == null) {
            response.content("The NPC must be assigned to a type");
            isValid = false;
        }

        if (this.location == null) {
            response.content("The NPC must be placed at a location");
            isValid = false;
        }

        // when there is a quest creator, try set player object from creator UUID
        if (questCreator != null) {
            player = Bukkit.getPlayer(quest.getCreator()); // the player to send invalid npc messages to
            response // send a message to the player
                .player(player)
                .style(MessageStyle.PRETTY);
        }

        // send the response
        if (!isValid) {
            response.send(); // send our :( message
        }

        return isValid;
    }
    
    /**
     * Assigns a type to the NPC.
     * 
     * @param npcType The type to assign to the NPC.
     */
    @JsonIgnore
    public void assign(NPCType npcType) {
        this.assigned = npcType;
    }

    /**
     * Sets the location of the NPC in the world.
     * 
     * @param location The location data to set.
     */
    @JsonIgnore
    public void setLocation(LocationData location) {
        this.location = location;
    }

    /**
     * Gets the location of the NPC in the world.
     * 
     * @return The location data of the NPC.
     */
    @JsonIgnore
    public LocationData getLocation() {
        return this.location;
    }

    /**
     * Gets the block data representing this NPC, if applicable.
     * 
     * @return The block data of the NPC, or default if not assigned to a block.
     */
    @JsonIgnore
    public BlockData getBlock() {
        if (this.assigned instanceof BlockNPC npc) {
            return npc.getBlock();
        }

        return Material.RED_STAINED_GLASS.createBlockData(); // default to unset
    }

    /**
     * Checks if the NPC has been assigned to a type.
     * 
     * @return True if the NPC is assigned to a type, otherwise false.
     */
    @JsonIgnore
    public boolean isAssigned() {
        return this.assigned != null;
    }

    /**
     * Gets the type to which this NPC is assigned.
     * 
     * @return The NPC type.
     */
    @JsonIgnore
    public NPCType getAssigned() {
        return this.assigned;
    }

    /**
     * Spawns and registers the NPC in the world according to its assigned type.
     * @param quester the quest client who should see the NPC
     */
    @JsonIgnore
    public void spawn(QuestAction<?,?> action, QuestClient quester) {
        this.assigned.register(action, quester, // keep reference to the NPC 
            this.assigned.spawn(action, quester) // spawn the NPC into the world
        );
    }

    /**
     * Despawns and unregisters the NPC in the world according to its assigned type and action.
     * @param quester the quest client that should no longer see the NPC
     */
    @JsonIgnore
    public void despawn(QuestAction<?,?> action, QuestClient quester) {
        this.assigned.despawn(action, quester); // remove the NPC from the world
        this.assigned.unregister(action, quester); // remove reference to the NPC
    }

    /**
     * Indiscrimantly despawns all of an NPC from a quester.
     * @param quester the quest client that should no longer see the NPC
     */
    @JsonIgnore 
    public void despawn(QuestClient quester) {
        quester.getTrackedActions().forEach(action -> this.despawn(action, quester));
    }

    /**
     * Gets the quest this NPC is associated with.
     * 
     * @return The quest that created this NPC.
     */
    public Quest getQuest() {
        return this.quest;
    }

    /**
     * Sets the quest that this NPC should belong to.
     * 
     * @param quest The quest to set.
     */
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    /**
     * Refunds a player through the assigned NPC type.
     * 
     * @param quester the quest client who should be refunded
     */
    @JsonIgnore
    public void refund(QuestClient quester) {
        this.getAssigned().refund(quester);
    }

    /**
     * Penalizes a player through the assigned NPC type.
     * 
     * @param quester the quest client who should be penalised
     */
    @JsonIgnore
    public void penalise(QuestClient quester) {
        this.getAssigned().penalise(quester);
    }
}
