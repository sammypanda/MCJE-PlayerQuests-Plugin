package playerquests.builder.quest.action.condition;

import playerquests.builder.quest.data.ActionData;

/**
 * Abstract class to make helpers which determine if 
 * some condition has been met.
 * Used to set start and finish conditions for actions.
 * @see playerquests.builder.quest.action.QuestAction
 */
public abstract class ActionCondition {

    /**
     * All the data needed to create a context 
     * to check the condition is met or not.
     */
    protected ActionData actionData;

    /**
     * Constructor for creating action conditions.
     * @param actionData all relevant data for the condition to use
     */
    public ActionCondition(ActionData actionData) {
        if (actionData == null) {
            throw new IllegalArgumentException("ActionData context cannot be completely null");
        }

        this.actionData = actionData;
    }

    /**
     * Whether the condition is currently met.
     * Implemented by each action condition specific to what 
     * their condition is checking.
     * @return boolean of the condition
     */
    public abstract Boolean isMet();
}
