package playerquests.builder.quest.component;

import java.util.LinkedHashMap; // hash map type with sequencing
import java.util.Map; // generic map type

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonProperty; // specifiying fields for showing when json serialised

import playerquests.Core; // accessing plugin singeltons
import playerquests.builder.quest.component.action.type.None; // an empty/skippable quest action
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
     * The id for the stage
     */
    private String stageID = "stage_-1";

    /**
     * Entry point for the stage.
     */
    @JsonIgnore
    private QuestAction entryPoint;

    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current quest stage to be accessed with key-pair syntax

        // create the default first action
        QuestAction action = this.newAction();

        // set the default first action as the default entry point
        this.setEntryPoint(action);
    }

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
     * Creates a new instance of a quest actions and adds it to this stage.
     * @return the new action instance
     */
    @JsonIgnore
    public QuestAction newAction() {
        String actionID = "action_"+this.actions.size();

        QuestAction action = new QuestAction(this.director, actionID, new None());
        this.actions.put(actionID, action);
        return action;
    }

    /**
     * Sets the first action executed when this stage is reached.
     * @param action a quest action instance
     */
    public void setEntryPoint(QuestAction action) {
        this.entryPoint = action;
    }

    /**
     * Gets the first action executed when this stage is reached.
     * @return a quest action instance
     */
    public QuestAction getEntryPoint() {
        return this.entryPoint;
    }

    /**
     * Gets the entry point as a string
     */
    @JsonProperty("entry")
    public String getEntryPointAsString() {
        return this.entryPoint.getTitle();
    }
}
