package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.QuestAction;
import playerquests.client.quest.QuestClient;

/**
 * Listener for immediately moving on from an action.
 */
public class EmptyActionListener extends ActionListener<QuestAction> {

    /**
     * Constructs a new empty action listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public EmptyActionListener(QuestAction action, QuestClient quester) {
        super(action, quester);
    }
    
}
