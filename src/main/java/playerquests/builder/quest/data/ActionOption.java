package playerquests.builder.quest.data;

/**
 * Enumeration of action options available in a quest.
 */
public enum ActionOption {
    /**
     * Represents an action option to set an NPC.
     */
    NPC("Set NPC", "VILLAGER_SPAWN_EGG"), 

    /**
     * Represents an action option to set a dialogue.
     */
    DIALOGUE("Set Dialogue", "OAK_SIGN");

    private final String label;
    private final String item;

    /**
     * Constructs an {@code ActionOption} with the specified label and item.
     *
     * @param label the label for this action option
     * @param item  the item associated with this action option
     */
    ActionOption(String label, String item) {
        this.label = label;
        this.item = item;
    }

    /**
     * Returns the label of this action option.
     *
     * @return the label of this action option
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the graphical representation of this action 
     * option.
     *
     * @return the item associated with this action option
     */
    public String getItem() {
        return this.item;
    }
}
