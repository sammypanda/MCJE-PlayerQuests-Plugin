package playerquests.builder.quest.action.condition;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.Core;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.client.ClientDirector;

/**
 * Abstract class to make helpers which determine if 
 * some condition has been met.
 * Used to set start and finish conditions for actions.
 * @see playerquests.builder.quest.action.QuestAction
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "condition")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TimeCondition.class, name = "Time"),
    @JsonSubTypes.Type(value = CompletionCondition.class, name = "Completion")
})
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
    protected ActionCondition() {}

    /**
     * Constructor for creating action conditions.
     * @param actionData all relevant data for the condition to use
     */
    protected ActionCondition(ActionData actionData) {
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

    /**
     * Creates the slots in a GUI that would be used
     * to edit this condition.
     * @param screen the screen the slot is created in
     * @param gui the GUI to put the slot on
     * @param slot the position to create the slot in on the GUI
     * @param director the director to control the plugin with
     * @return the GUI slot created
     */
    public abstract GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director);

    /**
     * Get the name of the action condition.
     * @return name of the condition
     */
    @JsonIgnore
    public abstract String getName();

    public abstract void createEditorGUI(GUIDynamic screen, GUIBuilder gui, ClientDirector director);

    /**
     * Get the short summary of the condition state.
     * For instance, a time condition may show the times selected.
     * @return lines of a single string about the condition details.
     */
    @JsonIgnore
    public abstract List<String> getDetails();

    /**
     * Get the condition description.
     * A useful short explanation of what the condition is.
     * @return lines of a single string about the condition.
     */
    @JsonIgnore
    public abstract List<String> getDescription();

    /**
     * Get the action data for this condition.
     * @return the action data for the action this condition is assigned to
     */
    @JsonIgnore
    public ActionData getActionData() {
        return this.actionData;
    }

    /**
     * Starts the listener belonging to the action is finished.
     * Listener is a type of ActionConditionListener.
     */
    @JsonIgnore
    public abstract void startListener(QuesterData questerData);

    /**
     * Contains the listener for this action condition.
     * Implementations must run trigger().
     */
    abstract class ActionConditionListener<C extends ActionCondition> implements Listener {
        
        protected final C actionCondition;

        protected final QuesterData questerData;

        protected ActionConditionListener(C actionCondition, QuesterData questerData) {
            this.actionCondition = actionCondition;
            this.questerData = questerData;

            // register the events
            Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());
        }

        /**
         * Trigger a re-start of the action.
         */
        public void trigger() {
            QuestAction<?,?> action = this.actionCondition.getActionData().getAction();

            // start the action
            questerData.getQuester().start(
                List.of(new StagePath(action.getStage(), List.of(action))), 
                action.getStage().getQuest()
            );

            // close the listener
            this.close();
        }

        /**
         * Unregister the listener.
         * Used to stop listening when the action was 
         * a success.
         */
        public void close() {
            // unregister the events
            HandlerList.unregisterAll(this);
        }
    }
}
