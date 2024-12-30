package playerquests.builder.quest.action.condition;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
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
    @JsonSubTypes.Type(value = TimeCondition.class, name = "Time")
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
}
