package playerquests.builder.quest.action.option;

import playerquests.builder.gui.GUIBuilder;

/**
 * Reusable value setters for actions.
 * Used to tune the behaviour/settings of an action.
 * @see playerquests.builder.quest.action.QuestAction
 */
// TODO: plan
public interface ActionOption {
    /**
     * Creates the slots in a GUI that would be used
     * to edit this option.
     * @param gui the GUI to put the slot on
     * @param slot the position to create the slot in on the GUI
     */
    void createSlot(GUIBuilder gui, Integer slot);
}
