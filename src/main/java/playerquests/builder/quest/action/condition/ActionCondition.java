package playerquests.builder.quest.action.condition;

import com.fasterxml.jackson.annotation.JsonBackReference;

import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;

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
    @JsonBackReference
    protected ActionData actionData;

    /** 
     * Default constructor for Jackson.
     */
    public ActionCondition() {}

    /**
     * Constructor for creating action conditions.
     * @param actionData all relevant data for the condition to use
     */
    public ActionCondition(ActionData actionData) {
        this.actionData = actionData;
    }

    /**
     * Whether the condition is currently met.
     * Implemented by each action condition specific to what 
     * their condition is checking.
     * @param questerData the data used to check the conditional.
     * @return boolean of the condition
     */
    public abstract Boolean isMet(QuesterData questerData);
}
