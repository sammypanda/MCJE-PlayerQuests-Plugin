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
import playerquests.product.Quest; // back reference to quest this stage belongs to
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * The information and action list for a quest stage.
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
    private String entryPoint;

    /**
     * The connections for the quest stage.
     */
    @JsonProperty("connections")
    private ConnectionsData connections = new ConnectionsData();

    /**
     * Default constructor (for Jackson)
    */
    public QuestStage() {}

    /**
     * StageID constructor (for Jackson)
    */
    public QuestStage(String stageID) {
        this.stageID = stageID;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        String action = new None(this).submit().getID();

        // set the default first action as the default entry point
        this.setEntryPoint(action);
    }

    /**
     * Constructs a new quest stage.
     * @param quest which quest this stage is in
     * @param stageIDNumber value which the stage is tracked by (stage_[num])
     */
    public QuestStage(Quest quest, Integer stageIDNumber) {
        this.stageID = "stage_"+stageIDNumber;

        // set which quest this stage belongs to
        this.quest = quest;

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        String action = new None(this).submit().getID();

        // set the default first action as the default entry point
        this.setEntryPoint(action);
    }

    /**
     * Constructs a new quest stage.
     * With a fully qualified stage ID string.
     * @param quest which quest this stage is in
     * @param stageID value which the stage is tracked by (stage_[num])
    */
    public QuestStage(Quest quest, String stageID) {
        this(quest, Integer.parseInt(stageID.substring(6)));
    }

    /**
     * Set what quest this stage belongs to.
     * @param quest a quest builder instance
     */
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    /**
     * Get what quest this stage belongs to.
     * @return a quest builder instance
     */
    public Quest getQuest() {
        return this.quest;
    }

    /**
     * Returns the quest stage ID.
     * @return value which the stage is tracked by (stage_[num])
     */
    public String getID() {
        return this.stageID;
    }

    /**
     * Returns the quest stage title (for now just represented as the ID).
     * @return label for the action
     */
    @JsonIgnore
    @Key("QuestStage")
    public String getTitle() {
        return this.stageID;
    }

    /**
     * Gets the map of quest actions added to this stage.
     * @return list of the action instances
     */
    @JsonIgnore
    public Map<String, QuestAction> getActions() {
        return this.actions;
    }

    /**
     * Adds a new quest action instance to this stage.
     * @param action quest action instance
     * @return the new action id
     */
    @JsonIgnore
    public String addAction(QuestAction action) {
        String actionID = "action_"+this.actions.size(); // get next ID
        action.setID(actionID); // set the ID local to the action
        this.actions.put(action.getID(), action); // add to the actions map
        return actionID;
    }

    /**
     * Removes an action from this stage
     * @param action the quest action to remove from the stage
     */
    public void removeAction(QuestAction action) {
        this.actions.remove(action.getID());
        this.quest.save();
    }

    /**
     * Sets the first action executed when this stage is reached.
     * @param action a quest action id
     */
    public void setEntryPoint(String action) {
        this.entryPoint = action;
    }

    /**
     * Gets the first action executed when this stage is reached.
     * @return a quest action id
     */
    @JsonIgnore
    public QuestAction getEntryPoint() {
        return this.actions.get(this.entryPoint);
    }

    /**
     * Gets the latest action currently in editing.
     * @return the action that is currently set as being edited
     */
    @JsonIgnore
    public String getActionToEdit() {
        return this.actionInEditing;
    }

    /**
     * Sets an action as currently in editing.
     * @param action the action to edit
     */
    public void setActionToEdit(String actionID) {
        this.actionInEditing = actionID;
    }

    public void changeActionType(String currentAction, QuestAction newActionInstance) {
        newActionInstance.setID(currentAction); // update the ID in the action local
        this.actions.replace(currentAction, newActionInstance); // replace in main list

        if (currentAction.equals(this.entryPoint)) {
            this.entryPoint = newActionInstance.getID();
        }
    }
}
