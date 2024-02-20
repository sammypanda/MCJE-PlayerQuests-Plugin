package playerquests.product;

import java.util.Map; // generic map type
import java.util.UUID; // identifies the player who created this quest

import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder

/**
 * The Quest product containing all the information 
 * about a quest, ready to be played.
 */
public class Quest {

    /**
     * The label of this quest.
     */
    private String title;

    /**
     * The starting/entry point stage ID for this quest.
     */
    private String entry;

    /**
     * The map of NPCs used in this quest, by their ID.
     */
    private Map<String, QuestNPC> npcs;

    /**
     * The map of stages used in this quest, by the stage ID.
     */
    private Map<String, QuestStage> stages;

    /**
     * The UUID of the player who created this quest.
     */
    private UUID creator;
    
    /**
     * Creates a quest instance for playing and viewing!
     * @param title label of this quest
     * @param entry starting/entry point stage for this quest
     * @param npcs map of NPCs used in this quest
     * @param stages map of stages used in this quest
     * @param creator UUID of player who created this quest
     */
    public Quest(String title, QuestStage entry, Map<String, QuestNPC> npcs, Map<String, QuestStage> stages, UUID creator) {
        this.title = title;
        this.entry = entry.getID();
        this.npcs = npcs;
        this.stages = stages;
        this.creator = creator;
    }

    /**
     * Gets the label of this quest.
     * @return the label of this quest
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the starting/entry point stage ID for this quest.
     * @return the ID of the starting/entry point stage for this quest
     */
    public String getEntry() {
        return entry;
    }

    /**
     * Gets the map of NPCs used in this quest.
     * @return the map of NPCs used in this quest
     */
    public Map<String, QuestNPC> getNPCs() {
        return npcs;
    }

    /**
     * Gets the map of stages used in this quest.
     * @return the map of stages used in this quest
     */
    public Map<String, QuestStage> getStages() {
        return stages;
    }

    /**
     * Gets the creator (UUID) of this quest.
     * @return the creator (UUID) of this quest
     */
    public UUID getCreator() {
        return creator;
    }

    /**
     * Gets the liquid ID for this quest.
     * <p>
     * IDs aren't fixed, if the quest title 
     * changes, it's considered a different quest.
     * This means creating new versions of the 
     * same quest is very easy. like Quest -> Quest2.
     */
    public String getID() {
        return String.format("%s_%s" , title, creator);
    }
}