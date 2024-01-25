package playerquests.builder.quest.component;

/**
 * An instance of a quest action.
 */
public class QuestAction {

    /**
     * The id for the action
     */
    private String actionID = "action_-1";

    /**
     * Constructs a new quest stage action.
     * @param id value which the action is tracked by (action_[num])
     */
    public QuestAction(String id) {
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
