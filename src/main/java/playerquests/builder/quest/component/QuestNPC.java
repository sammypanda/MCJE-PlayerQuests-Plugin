package playerquests.builder.quest.component;

import org.bukkit.Material; // for if NPC is a block
import org.bukkit.entity.HumanEntity; // the player

import com.fasterxml.jackson.annotation.JsonIgnore; // ignore a field when serialising to a JSON object
import com.fasterxml.jackson.annotation.JsonProperty; // specifying property for when serialising to a JSON object

import playerquests.Core; // for accessing singletons
import playerquests.builder.quest.type.Location;
import playerquests.builder.quest.QuestBuilder; // the quest itself
import playerquests.builder.quest.component.npc.type.BlockNPC; // 'Block' NPC type
import playerquests.builder.quest.component.npc.type.NPCType; // the type of NPC, such as 'Block'
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.utility.ChatUtils; // sends error messages to player
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
    @JsonIgnore
    private QuestBuilder quest;

    /**
     * The parent director.
     */
    @JsonIgnore
    private ClientDirector director;

    /**
     * What the NPC is assigned to.
     */
    @JsonProperty("assigned")
    private NPCType assigned;

    /**
     * Where the NPC exists in the world.
     */
    @JsonProperty("location")
    private Location location;

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
    @JsonIgnore
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
        this.quest = quest;
        this.director = quest.getDirector();

        // try to save this NPC to the quest list
        return quest.addNPC(npc, false);
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
        HumanEntity player = this.director.getPlayer(); // the player to send invalid npc messages to

        if (this.name == null) {
            ChatUtils.sendError(player, "The NPC name must be set");
            return false;
        }

        if (this.assigned == null) {
            ChatUtils.sendError(player, "The NPC must be assigned to a type");
            return false;
        }

        return true;
    }
    
    /**
     * Assign the NPC to something.
     */
    @JsonIgnore
    public void assign(NPCType npcType) {
        this.assigned = npcType;
    }

    /**
     * Set the location of this NPC in the world.
     * @param location PlayerQuests Location object
     */
    @JsonIgnore
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Get the location of this NPC in the world.
     * @return Bukkit Location object
     */
    @JsonIgnore
    public Location getLocation() {
        return this.location;
    }

    /**
     * Gets a material which represents this NPC.
     */
    @JsonIgnore
    public Material getMaterial() {
        if (this.assigned instanceof BlockNPC) {
            BlockNPC npc = (BlockNPC) this.assigned;
            return npc.getBlock();
        }

        return Material.RED_STAINED_GLASS; // default to unset
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
}
