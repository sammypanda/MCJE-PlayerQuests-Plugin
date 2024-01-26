package playerquests.builder.quest.component;

import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property

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
    
}
