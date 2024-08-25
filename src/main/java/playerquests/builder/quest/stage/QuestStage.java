package playerquests.builder.quest.stage;

import java.util.LinkedHashMap; // hash map type with sequencing
import java.util.Map; // generic map type

import com.fasterxml.jackson.annotation.JsonBackReference; // stops infinite recursion
import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonManagedReference; // refers to the parent of a back reference
import com.fasterxml.jackson.annotation.JsonProperty; // specifiying fields for showing when json serialised

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.action.None; // default QuestAction type
import playerquests.builder.quest.action.QuestAction; // abstract class for quest actions
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.data.StagePath;
import playerquests.product.Quest; // back reference to quest this stage belongs to
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * Represents a stage in a quest, including the actions and connections associated with it.
 * 
 * A quest stage can have various actions and a specified entry point for execution. It also holds connections
 * to other stages within the quest.
 */
public class QuestStage {

    /**
     * The quest this stage belongs to.
     */
    @JsonBackReference
    private Quest quest;

    /**
     * List of the quest actions.
     */
    @JsonProperty("actions")
    @JsonManagedReference
    private Map<String, QuestAction> actions = new LinkedHashMap<String, QuestAction>();

    /**
     * The latest currently edited action.
     */
    private String actionInEditing;

    /**
     * The id for the stage
     */
    @JsonProperty("id")
    private String stageID = "stage_-1";

    /**
     * Entry point action for the stage.
     */
    @JsonProperty("entry")
    private StagePath entryPoint;

    /**
     * The connections for the quest stage.
     */
    @JsonProperty("connections")
    private ConnectionsData connections = new ConnectionsData();

    /**
     * Default constructor for Jackson deserialization.
     */
    public QuestStage() {}

    /**
     * Constructs a new {@code QuestStage} with the specified stage ID.
     * 
     * This constructor initializes the stage with a default action and sets it as the entry point.
     *
     * @param stageID the unique identifier for this stage
     */
    public QuestStage(String stageID) {
        this.stageID = stageID;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        QuestAction action = new None(this).submit();

        // set the default first action as the default entry point
        this.setEntryPoint(new StagePath(this, action));
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a numeric stage ID.
     * 
     * This constructor initializes the stage with a default action and sets it as the entry point.
     *
     * @param quest the quest this stage belongs to
     * @param stageIDNumber the numeric identifier for this stage
     */
    public QuestStage(Quest quest, Integer stageIDNumber) {
        this.stageID = "stage_"+stageIDNumber;

        // set which quest this stage belongs to
        this.quest = quest;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        QuestAction action = new None(this).submit();

        // set the default first action as the default entry point
        this.setEntryPoint(new StagePath(this, action));
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a fully qualified stage ID.
     * 
     * This constructor parses the stage ID from the provided string and initializes the stage.
     *
     * @param quest the quest this stage belongs to
     * @param stageID the fully qualified stage ID (e.g., "stage_1")
     */
    public QuestStage(Quest quest, String stageID) {
        this(quest, Integer.parseInt(stageID.substring(6)));
    }

    /**
     * Sets the quest associated with this stage.
     *
     * @param quest the quest to associate with this stage
     */
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    /**
     * Gets the quest associated with this stage.
     *
     * @return the quest associated with this stage
     */
    public Quest getQuest() {
        return this.quest;
    }

    /**
     * Returns the unique identifier for this stage.
     *
     * @return the stage ID
     */
    public String getID() {
        return this.stageID;
    }

    /**
     * Returns the title of the stage. Currently represented as the stage ID.
     *
     * @return the title of the stage
     */
    @JsonIgnore
    @Key("QuestStage")
    public String getTitle() {
        return this.stageID;
    }

    /**
     * Gets the map of actions associated with this stage.
     *
     * @return a map of action instances, keyed by action ID
     */
    @JsonIgnore
    public Map<String, QuestAction> getActions() {
        return this.actions;
    }

    /**
     * Adds a new action to this stage.
     * 
     * This method generates a new action ID, assigns it to the action, and adds it to the actions map.
     *
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
     * Removes an action from this stage.
     *
     * @param action the action to remove
     */
    public void removeAction(QuestAction action) {
        this.actions.remove(action.getID());
    }

    /**
     * Sets the entry point action for this stage.
     * 
     * The entry point specifies the initial action to be executed when this stage is reached.
     *
     * @param path the entry point action
     */
    public void setEntryPoint(StagePath path) {
        this.entryPoint = path;
    }

    /**
     * Gets the entry point action for this stage.
     *
     * @return the entry point action
     */
    @JsonIgnore
    public StagePath getEntryPoint() {
        return this.entryPoint;
    }

    /**
     * Gets the ID of the action currently being edited.
     *
     * @return the ID of the action being edited
     */
    @JsonIgnore
    public String getActionToEdit() {
        return this.actionInEditing;
    }

    /**
     * Sets the ID of the action currently being edited.
     *
     * @param actionID the ID of the action to edit
     */
    public void setActionToEdit(String actionID) {
        this.actionInEditing = actionID;
    }

    /**
     * Changes the type of an existing action in this stage.
     * 
     * This method updates the action in the actions map and adjusts the entry point if necessary.
     *
     * @param currentAction the ID of the action to replace
     * @param newActionInstance the new action instance to replace the old one
     */
    public void changeActionType(String currentAction, QuestAction newActionInstance) {
        newActionInstance.setID(currentAction); // update the ID in the action local
        this.actions.replace(currentAction, newActionInstance); // replace in main list

        if (currentAction.equals(this.entryPoint.getAction())) {
            this.entryPoint = new StagePath(this, newActionInstance);
        }
    }

    /**
     * Gets the connections data for this stage.
     * 
     * This data represents how this stage connects to other stages.
     *
     * @return the connections data
     */
    @JsonIgnore
    public ConnectionsData getConnections() {
        return this.connections;
    }

    @Override
    public String toString() {
        return this.stageID;
    }
}
