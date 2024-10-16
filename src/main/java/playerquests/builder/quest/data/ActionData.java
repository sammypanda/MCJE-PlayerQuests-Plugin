package playerquests.builder.quest.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.listener.ActionListener;

/**
 * The entire game context that could possibly 
 * be needed for actions.
 * Especially useful for checking conditionals.
 */
public class ActionData {


    /**
     * The unique identifier of this action.
     */
    @JsonProperty("id")
    private String id;

    /**
     * Useful for pulling in values.
     */
    @JsonIgnore
    private ActionListener<?> listener;

    /**
     * The actions slated to come after this one.
     */
    @JsonProperty("next")
    private List<QuestAction> nextActions = new ArrayList<QuestAction>();

    /**
     * Default constructor for Jackson
     */
    public ActionData() {}

    /**
     * Constructor for providing action context.
     * Args (if you're sure they aren't needed) can be nullified.
     * @param id the unique identifier for the action
     * @param listener the action listener for this action
     * @param nextActions the actions slated to come next
     */
    public ActionData( 
        String id,
        ActionListener<?> listener,
        List<QuestAction> nextActions
    ) {
        this.id = id;
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

    /**
     * Replaces actions slated to come next.
     * @param nextActions the actions slated to come next
     */
    public void setNextActions(List<QuestAction> nextActions) {
        this.nextActions = nextActions;
    }

    /**
     * Sets the unique identifier for this action.
     * @param id the unique identifier
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Gets the unique identifier for this action.
     * @return the unique identifier
     */
    public String getID() {
        return this.id;
    }
}
