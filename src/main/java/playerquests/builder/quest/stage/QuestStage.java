package playerquests.builder.quest.stage;

import java.util.LinkedHashMap; // hash map type with sequencing
import java.util.Map; // generic map type

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonProperty; // specifiying fields for showing when json serialised

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.None;
import playerquests.builder.quest.action.QuestAction;
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * The information and action list for a quest stage.
 */
public class QuestStage {

    /**
     * The quest this stage belongs to.
     */
    @JsonIgnore
    private QuestBuilder quest;

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
     * Entry point action for the stage.
     */
    @JsonProperty("entry")
    private String entryPoint;

    /**
     * Constructs a new quest stage.
     * @param questBuilder which quest this stage is in
     * @param stageIDNumber value which the stage is tracked by (stage_[num])
     */
    public QuestStage(QuestBuilder questBuilder, Integer stageIDNumber) {
        this.stageID = "stage_"+stageIDNumber;

        // set which quest this stage belongs to
        this.quest = questBuilder;

        // set as the current instance in the director
        questBuilder.getDirector().setCurrentInstance(this);

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        String action = new None(this).submit().getID();

        // set the default first action as the default entry point
        this.setEntryPoint(action);
    }

    /**
     * Set what quest this stage belongs to.
     * @param quest a quest builder instance
     */
    public void setQuest(QuestBuilder quest) {
        this.quest = quest;
    }

    /**
     * Get what quest this stage belongs to.
     * @return a quest builder instance
     */
    @JsonIgnore
    public QuestBuilder getQuest() {
        return this.quest;
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
