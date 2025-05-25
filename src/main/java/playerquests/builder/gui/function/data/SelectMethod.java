package playerquests.builder.gui.function.data;

/**
 * Enumeration for specifying different methods of physical selection.
 * <p>
 * The {@link SelectMethod} enum defines the various ways a player can select a thing 
 * in the game. This enum helps in distinguishing how the selection is handled
 * within the game's GUI functions or other relevant processes.
 * </p>
 */
public enum SelectMethod {
    /**
     * Selected via clicking in inventory.
     */
    SELECT,

    /**
     * Selected by their in-world representation being hit.
     */
    HIT,

    /**
     * Selected by typing it in the chatbox.
     */
    CHAT,

    /**
     * Selected by opening/right-clicking/patting; secondary click.
     */
    PAT;
}
