package playerquests.builder.gui.function.data;

/**
 * Enumeration for specifying different methods of block selection.
 * <p>
 * The {@link SelectMethod} enum defines the various ways a player can select a block
 * in the game. This enum helps in distinguishing how the block selection is handled
 * within the game's GUI functions or other relevant processes.
 * </p>
 */
public enum SelectMethod {
    /**
     * Blocks selected via clicking the 
     * block item in inventory.
     */
    SELECT,

    /**
     * Blocks selected by being their placed
     * representation being hit.
     */
    HIT,

    /**
     * Blocks selected by typing their Material
     * in the chatbox.
     */
    CHAT;
}
