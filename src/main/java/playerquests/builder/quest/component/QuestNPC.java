package playerquests.builder.quest.component;

import com.fasterxml.jackson.annotation.JsonIgnore; // ignore a field when serialising to a JSON object
import com.fasterxml.jackson.annotation.JsonProperty; // specifying property for when serialising to a JSON object

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
    private String ID = "npc_-1";

    /**
     * The NPC name.
     * <p>
     * Defaults to the ID if not set
     */
    @JsonProperty("name")
    private String name = this.ID;

    /**
     * Instantiates a new empty Quest NPC.
     */
    public QuestNPC() {}

    /**
     * Instantiates a new Quest NPC.
     * @param id the id for this npc, like: npc_1
     */
    public QuestNPC(String id) {
        this.ID = id;
    }

    /**
     * Gets the ID for the quest NPC.
     * @return the quest NPC ID
     */
    @JsonIgnore
    public String getID() {
        return this.ID;
    }

    /**
     * Gets the most appropriate 'title' for this NPC.
     * @return the string form of this npc (the npc ID)
     */
    @JsonIgnore
    public String getTitle() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.getID();
    }
    
}
