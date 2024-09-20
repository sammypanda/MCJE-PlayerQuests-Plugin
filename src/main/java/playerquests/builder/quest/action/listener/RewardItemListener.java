package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.RewardItem;
import playerquests.client.quest.QuestClient;

/**
 * Listener for immediately moving on from an action.
 */
public class RewardItemListener extends ActionListener<RewardItem> {

    /**
     * Constructs a new reward item action listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public RewardItemListener(RewardItem action, QuestClient quester) {
        super(action, quester);
    }
    
}
