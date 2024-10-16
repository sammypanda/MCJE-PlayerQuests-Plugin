package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.data.QuesterData;

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
