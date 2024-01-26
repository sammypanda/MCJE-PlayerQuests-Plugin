package playerquests.builder.quest.component;

import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property

import playerquests.client.ClientDirector;

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
     * Constructs a new quest stage action.
     * @param director director for the client
     * @param id value which the action is tracked by (action_[num])
     */
    public QuestAction(ClientDirector director, String id) {
        this.director = director;
        this.actionID = id;
    }

    /**
     * Returns the quest action title (for now just represented as the ID).
     * @return label for the action
     */
    @JsonProperty("name")
    public String getTitle() {
        return this.actionID;
    }
    
}
