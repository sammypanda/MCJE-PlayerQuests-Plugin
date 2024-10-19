package playerquests.builder.quest.stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference; // stops infinite recursion
import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.StagePath;
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
     * The id for the stage.
     */
    @JsonProperty("id")
    private String id;

    /**
     * The map of actions.
     */
    @JsonManagedReference
    private Map<String, QuestAction> actions = new HashMap<String, QuestAction>();

    /**
     * List of starting points.
     */
    @JsonProperty("startpoints")
    private List<StagePath> startPoints = new ArrayList<StagePath>();

    /**
     * Constructs a new {@code QuestStage} with the specified stage ID.
     * @param id the unique identifier for this stage
     */
    public QuestStage(@JsonProperty("id") String id) {
        this.id = id;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a numeric stage ID.
     * @param quest the quest this stage belongs to
     * @param idNumber the numeric identifier for this stage
     */
    public QuestStage(Quest quest, Integer idNumber) {
        this.id = "stage_"+idNumber;

        // set which quest this stage belongs to
        this.quest = quest;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a fully qualified stage ID.
     * This constructor parses the stage ID from the provided string and initializes the stage.
     * @param quest the quest this stage belongs to
     * @param id the fully qualified stage ID (e.g., "stage_1")
     */
    public QuestStage(Quest quest, String id) {
        this(quest, Integer.parseInt(id.substring(6)));
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
        return this.id;
    }

    /**
     * Returns the title of the stage. Currently represented as the stage ID.
     * @return the title of the stage
     */
    @JsonIgnore
    @Key("QuestStage")
    public String getTitle() {
        return this.id;
    }                           

    @Override
    public String toString() {
        return this.id;
    }

    /**
     * Sets the unique identifier for this stage.
     * @param id the stage ID to set
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Gets a map of actions in this stage.
     * @return the actions
     */
    public Map<String, QuestAction> getActions() {
        return this.actions;    
    }

    /**
     * Adds a new action to this stage.
     * This method generates a new action ID, assigns it to the action, and adds it to the actions map.
     * @param action the action to add
     * @return the ID of the newly added action
     */
    @JsonIgnore
    public String addAction(QuestAction action) {
        String actionID = "action_"+this.actions.size(); // get next ID
        action.setID(actionID); // set the ID local to the action
        this.actions.put(action.getID(), action); // add to the actions map
        return actionID;
    }

    /**
     * Gets the actions the quest starts with.
     * @return the starting actions
     */
    public List<StagePath> getStartPoints() {
        return this.startPoints;
    }
}
