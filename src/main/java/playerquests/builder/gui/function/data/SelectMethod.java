package playerquests.builder.gui.function.data;

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
