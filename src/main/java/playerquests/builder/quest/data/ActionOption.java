package playerquests.builder.quest.data;

/**
 * Enumeration of action options available in a quest.
 */
public enum ActionOption {
    /**
     * To set an NPC.
     */
    NPC("Set NPC", "VILLAGER_SPAWN_EGG"), 

    /**
     * To set dialogue text.
     */
    DIALOGUE("Set Dialogue", "OAK_SIGN"),

    /**
     * To set one or more items.
     */
    ITEMS("Select Items", "CHEST"), 
    
    /**
     * To set a message for when the action finishes.
     */
    FINISH_MESSAGE("Finish Message", "NAME_TAG");

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
