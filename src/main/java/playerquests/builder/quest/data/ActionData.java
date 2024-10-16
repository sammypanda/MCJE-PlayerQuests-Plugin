package playerquests.builder.quest.data;

import java.util.ArrayList;
import java.util.List;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.listener.ActionListener;

/**
 * The entire game context that could possibly 
 * be needed for actions.
 * Especially useful for checking conditionals.
 */
public class ActionData {

    /**
     * Useful for pulling in values.
     */
    private ActionListener<?> listener;

    /**
     * The actions slated to come after this one.
     */
    private List<QuestAction> nextActions = new ArrayList<QuestAction>();

    /**
     * Constructor for providing action context.
     * Args (if you're sure they aren't needed) can be nullified.
     * @param listener the action listener for this action
     * @param nextActions the actions slated to come next
     */
    public ActionData( 
        ActionListener<?> listener,
        List<QuestAction> nextActions
    ) {
        this.listener = listener;
        this.nextActions = nextActions;
    }

    /**
     * Gets the action listener associated with this action.
     * @return the listener
     */
    public ActionListener<?> getListener() {
        return listener;
    }

    /**
     * Sets the action listener for this action.
     * @param actionListener the listener that will trigger action checking
     * @return the passed in listener
     */
    public ActionListener<?> setListener(ActionListener<?> actionListener) {
        this.listener = actionListener;
        return actionListener;
    }

    /**
     * Gets the action listener for this action.
     * @return the listener that will trigger action checking
     */
    public List<QuestAction> getNextActions() {
        return this.nextActions;
    }

    /**
     * Adds an action to the list of those slated to come next.
     * @param action a quest action
     */
    public void addNextAction(QuestAction action) {
        this.nextActions.add(action);
    }
}
