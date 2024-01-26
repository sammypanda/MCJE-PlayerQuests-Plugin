package playerquests.builder.quest.component;

import java.util.Optional; // handling possibly null/not found values

import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property

import playerquests.Core; // accessing plugin singletons
import playerquests.builder.quest.component.action.type.ActionType; // modifying a quest stage action
import playerquests.builder.quest.component.action.type.None; // an empty/skippable quest action
import playerquests.client.ClientDirector; // controls the plugin

/**
 * An instance of a quest action.
 */
public class QuestAction {

    /**
     * Used to access plugin functionality.
     */
    private ClientDirector director;

    /**
     * The id for the action
     */
    private String actionID = "action_-1";

    /**
     * The type of action
     */
    private ActionType actionType = new None();

    /**
     * Constructs a new quest stage action.
     * @param director director for the client
     * @param id value which the action is tracked by (action_[num])
     * @param type the action type
     */
    public QuestAction(ClientDirector director, String id, ActionType type) {
        this.director = director;
        this.actionID = id;
        this.actionType = type;
    }

    /**
     * Returns the quest action title (for now just represented as the ID).
     * @return label for the action
     */
    @JsonProperty("name")
    public String getTitle() {
        return this.actionID;
    }

    /**
     * Gets the string representation of the type.
     * @return current action type as a string
     */
    public String getType() {
        return this.actionType.toString();
    }

    /**
     * Gets the stage instance this quest action belongs to.
     * @return a QuestStage instance
     */
    public QuestStage getStage() {
        Optional<QuestStage> currentStage = Core.getKeyHandler().getInstances().stream() // get all registered instances
            .filter(instance -> instance instanceof QuestStage) // filter out anything that is not a quest stage
            .map(instance -> (QuestStage) instance) // map the quest stage object to QuestStage class type
            .filter(stage -> stage.getActions().containsValue(this)) // check if the actions list in the stage contains this QuestAction
            .findFirst();

        if (currentStage.isPresent()) {
            return currentStage.get();
        } 
         
        throw new IllegalStateException("Stray action found. Action: " + this.getTitle() + " has no parent stage.");
    }
    
}
