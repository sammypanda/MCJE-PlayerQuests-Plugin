package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.TakeItem;
import playerquests.client.quest.QuestClient;

/**
 * Listener for if player has an item to take.
 */
public class TakeItemListener extends ActionListener<TakeItem> {

    /**
     * Constructs a new take item listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public TakeItemListener(TakeItem action, QuestClient quester) {
        super(action, quester);
    }
}
