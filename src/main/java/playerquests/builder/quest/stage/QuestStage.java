package playerquests.builder.quest.stage;

import java.util.LinkedHashMap; // hash map type with sequencing
import java.util.Map; // generic map type

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonProperty; // specifiying fields for showing when json serialised

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.action.None;
import playerquests.builder.quest.action.QuestAction;
import playerquests.client.ClientDirector; // to control the plugin

/**
 * The information and action list for a quest stage.
 */
public class QuestStage {

    /**
     * Director to retrieve values
     */
    private ClientDirector director;

    /**
     * List of the quest actions.
     */
    @JsonProperty("actions")
    private Map<String, QuestAction> actions = new LinkedHashMap<String, QuestAction>();

    /**
     * The latest currently edited action.
     */
    private String actionInEditing;

    /**
     * The id for the stage
     */
    private String stageID = "stage_-1";

    /**
     * Entry point for the stage.
     */
    @JsonIgnore
    private String entryPoint;

    /**
     * Constructs a new quest stage.
     * @param director director for the client
     * @param stageIDNumber value which the stage is tracked by (stage_[num])
     */
    public QuestStage(ClientDirector director, Integer stageIDNumber) {
        this.director = director;
        this.stageID = "stage_"+stageIDNumber;

        // set as the current instance in the director
        director.setCurrentInstance(this);

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        String action = new None(this).submit().getID();

        // set the default first action as the default entry point
        this.setEntryPoint(action);
    }

    /**
     * Returns the quest stage ID.
     * @return value which the stage is tracked by (stage_[num])
     */
    @JsonIgnore
    public String getID() {
        return this.stageID;
    }

    /**
     * Returns the quest stage title (for now just represented as the ID).
     * @return label for the action
     */
    @JsonIgnore
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

        this.actions.put(actionID, action); // add to the actions map
        return actionID;
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
    public String getEntryPoint() {
        return this.entryPoint;
    }

    /**
     * Gets the entry point as a string
     */
    @JsonProperty("entry")
    public String getEntryPointAsString() {
        return this.entryPoint.toString();
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
    }
}
