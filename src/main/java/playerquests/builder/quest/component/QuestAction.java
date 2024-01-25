package playerquests.builder.quest.component;

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
     * @param director
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
    public String getTitle() {
        return this.actionID;
    }
    
}