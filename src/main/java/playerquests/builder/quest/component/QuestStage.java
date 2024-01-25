package playerquests.builder.quest.component;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.client.ClientDirector;

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
    private List<QuestAction> actions = new ArrayList<QuestAction>();

    /**
     * The id for the stage
     */
    private String stageID = "stage_-1";

    /**
     * Constructs a new quest stage.
     * @param id value which the stage is tracked by (stage_[num])
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
     * Returns the list of quest actions added to this stage.
     * @return list of the action instances
     */
    @JsonIgnore
    public List<QuestAction> getActions() {
        return this.actions;
    }

    /**
     * Creates a new instance of a quest actions and adds it to this stage.
     * @return the new action instance
     */
    @JsonIgnore
    public QuestAction newAction() {
        QuestAction action = new QuestAction("action_"+this.actions.size());
        this.actions.add(action);
        return action;
    }
}
