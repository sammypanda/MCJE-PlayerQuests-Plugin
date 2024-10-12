package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.data.ActionData;

/**
 * Listener for immediately moving on from an action.
 */
public class NoneListener extends ActionListener<NoneAction> {

    /**
     * Constructs a new empty action listener.
     * @param action the quest action this listener is for.
     * @param context the data for the action.
     */
    public NoneListener(NoneAction action, ActionData<? extends ActionListener<?>> context) {
        super(action, context);
    }
    
}
