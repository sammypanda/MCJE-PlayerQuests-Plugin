package playerquests.builder.quest.action.listener;

import java.util.Optional;

import org.bukkit.event.EventHandler;

import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.QuesterData;
import playerquests.utility.event.NPCInteractEvent;

/**
 * Listener for immediately moving on from an action.
 */
public class NoneListener extends ActionListener<NoneAction> {

    /**
     * Constructs a new empty action listener.
     * @param action the quest action this listener is for.
     * @param questerData the data about the quester.
     */
    public NoneListener(NoneAction action, QuesterData questerData) {
        super(action, questerData);
    }
}
