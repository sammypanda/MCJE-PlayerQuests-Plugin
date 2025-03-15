package playerquests.builder.quest.action.option;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

/**
 * Reusable value setters for actions.
 * Used to tune the behaviour/settings of an action.
 * @see playerquests.builder.quest.action.QuestAction
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "option")
@JsonSubTypes({
    @JsonSubTypes.Type(value = NPCOption.class, name = "NPC"),
    @JsonSubTypes.Type(value = DialogueOption.class, name = "Dialogue")
})
public abstract class ActionOption {

    /**
     * The quest action that this option belongs to.
     */
    @JsonBackReference
    protected ActionData actionData;

    /**
     * Default constructor for Jackson.
     */
    public ActionOption() {}

    /**
     * Constructor including the QuestAction.
     * @param actionData the parent action
     */
    public ActionOption(ActionData actionData) {
        this.actionData = actionData;
    }
    
    /**
     * Creates the slots in a GUI that would be used
     * to edit this option.
     * @param screen the screen the slot is created in
     * @param gui the GUI to put the slot on
     * @param slot the position to create the slot in on the GUI
     * @param director the director to control the plugin with
     * @return the GUI slot created
     */
    public abstract GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director);

    /**
     * Gets the data of the action this option is for.
     * @return the parent action.
     */
    public ActionData getActionData() {
        return this.actionData;
    }

    /**
     * Sets the action data reference for this option.
     * @param actionData the parent action
     */
    public void setActionData(ActionData actionData) {
        this.actionData = actionData;
    }
}
