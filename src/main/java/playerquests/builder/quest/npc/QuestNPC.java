package playerquests.builder.quest.npc;

import java.util.UUID;

import org.bukkit.Bukkit; // bukkit singleton
import org.bukkit.Material; // for if NPC is a block
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.HumanEntity; // the player
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore; // ignore a field when serialising to a JSON object
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty; // specifying property for when serialising to a JSON object

import playerquests.Core; // for accessing singletons
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.data.LocationData; // quest entity locations
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.product.Quest;
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.annotation.Key; // key-value pair annottation

/**
 * Object containing details about a quest NPC.
 */
public class QuestNPC {

    /**
     * The NPC ID.
     * <p>
     * Initialises with an out of bounds ID.
     * This is so it won't show unless the ID
     * is properly set.
     */
    @JsonIgnore
    private String id = "npc_-1";

    /**
     * The NPC name.
     */
    @JsonProperty("name")
    private String name = null;

    /**
     * The parent quest.
     */
    @JsonBackReference
    private Quest quest;

    /**
     * The parent director.
     */
    @JsonIgnore
    private ClientDirector director;

    /**
     * What the NPC is assigned to.
     */
    @JsonProperty("assigned")
    @JsonManagedReference
    private NPCType assigned;

    /**
     * Where the NPC exists in the world.
     */
    @JsonProperty("location")
    private LocationData location;

    /**
     * Operations to run whenever the class is instantiated.
     */
    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);
    }

    /**
     * Instantiates a new empty Quest NPC.
     */
    public QuestNPC() {}

    /**
     * Instantiates a new Quest NPC.
     * @param id the id for this npc, like: npc_1
     */
    public QuestNPC(String id) {
        this.id = id;
    }

    /**
     * Gets the ID for the quest NPC.
     * @return the quest NPC ID
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets the most appropriate 'title' for this NPC.
     * @return the string form of this npc (the npc name)
     */
    @JsonIgnore
    public String getTitle() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Gets the aesthetic name of this NPC.
     * @return the name of the NPC
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the aesthetic name of this NPC.
     * @param name the name of the NPC
     */
    @Key("npc.name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Save the changes to this NPC into a quest.
     * <p>
     * Will replace ID, if ID is 'npc_-1'.
     * @param quest reference to the quest builder
     * @param npc the NPC object to save
     * @return if the npc was successfully saved
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
     * Overriding the ID of this NPC.
     * @param id the id to use instead
     */
    @JsonIgnore
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Checks if everything is correctly set and formed.
     * @return if the NPC object is valid
     */
    @JsonIgnore
    public boolean isValid() {
        UUID questCreator = quest.getCreator();
        HumanEntity player = null;
        MessageBuilder response = new MessageBuilder("Something is wrong with a quest NPC") // default message; default sends to console
            .type(MessageType.ERROR)
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN);
        Boolean isValid = true; // assume is valid

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
     * Assign the NPC to something.
     */
    @JsonIgnore
    public void assign(NPCType npcType) {
        Player player = Bukkit.getPlayer(this.quest.getCreator());

        if (this.assigned != null && player != null) { // if already assigned to something
            this.refund(player);
        }

        this.assigned = npcType;
    }

    /**
     * Set the location of this NPC in the world.
     * @param location PlayerQuests Location object
     */
    @JsonIgnore
    public void setLocation(LocationData location) {
        this.location = location;
    }

    /**
     * Get the location of this NPC in the world.
     * @return Bukkit Location object
     */
    @JsonIgnore
    public LocationData getLocation() {
        return this.location;
    }

    /**
     * Gets a material which represents this NPC.
     */
    @JsonIgnore
    public BlockData getBlock() {
        if (this.assigned instanceof BlockNPC) {
            BlockNPC npc = (BlockNPC) this.assigned;
            return npc.getBlock();
        }

        return Material.RED_STAINED_GLASS.createBlockData(); // default to unset
    }

    /**
     * Returns if the NPC has been assigned to a type, such as a 'Block'.
     * @return the type of NPC assignment
     */
    @JsonIgnore
    public boolean isAssigned() {
        return this.assigned != null;
    }

    /**
     * Returns the information about how the NPC is assigned.
     * @return the NPC assignment
     */
    @JsonIgnore
    public NPCType getAssigned() {
        return this.assigned;
    }

    /**
     * Places the NPC in the world.
     */
    @JsonIgnore
    public void place() {
        this.assigned.place();
    }

    /**
     * Gets the quest this NPC belongs to.
     * @return the quest which created this NPC.
     */
    public Quest getQuest() {
        return this.quest;
    }

    /**
     * Set the quest this NPC should belong to.
     * @param quest the quest which owns this NPC.
     */
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    @JsonIgnore
    public void refund(Player player) {
        this.getAssigned().refund(player);
    }
}
