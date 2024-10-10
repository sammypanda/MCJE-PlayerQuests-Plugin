package playerquests.builder.quest.stage;

import com.fasterxml.jackson.annotation.JsonBackReference; // stops infinite recursion
import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonKey;

import playerquests.Core; // accessing plugin singeltons
import playerquests.product.Quest; // back reference to quest this stage belongs to
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * Represents a stage in a quest.
 */
public class QuestStage {

    /**
     * The quest this stage belongs to.
     */
    @JsonBackReference
    private Quest quest;

    /**
     * The id for the stage
     */
    @JsonKey    
    private String stageID;

    /**
     * Default constructor for Jackson deserialization.
     */
    public QuestStage() {}

    /**
     * Constructs a new {@code QuestStage} with the specified stage ID.
     * @param stageID the unique identifier for this stage
     */
    public QuestStage(String stageID) {
        this.stageID = stageID;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a numeric stage ID.
     * @param quest the quest this stage belongs to
     * @param stageIDNumber the numeric identifier for this stage
     */
    public QuestStage(Quest quest, Integer stageIDNumber) {
        this.stageID = "stage_"+stageIDNumber;

        // set which quest this stage belongs to
        this.quest = quest;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a fully qualified stage ID.
     * This constructor parses the stage ID from the provided string and initializes the stage.
     * @param quest the quest this stage belongs to
     * @param stageID the fully qualified stage ID (e.g., "stage_1")
     */
    public QuestStage(Quest quest, String stageID) {
        this(quest, Integer.parseInt(stageID.substring(6)));
    }

    /**
     * Sets the quest associated with this stage.
     * @param quest the quest to associate with this stage
     */
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    /**
     * Gets the quest associated with this stage.
     * @return the quest associated with this stage
     */
    public Quest getQuest() {
        return this.quest;
    }

    /**
     * Returns the unique identifier for this stage.
     * @return the stage ID
     */
    @JsonIgnore
    public String getID() {
        if (this.stageID == null) {
            throw new IllegalArgumentException("Stage IDs cannot be null.");
        }

        return this.stageID;
    }

    /**
     * Returns the title of the stage. Currently represented as the stage ID.
     * @return the title of the stage
     */
    @JsonIgnore
    @Key("QuestStage")
    public String getTitle() {
        return this.stageID;
    }                           

    @Override
    public String toString() {
        return this.stageID;
    }
}
