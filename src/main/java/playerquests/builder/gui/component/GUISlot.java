package playerquests.builder.gui.component;

import playerquests.builder.gui.GUIBuilder; // the builder which enlists this slot

/**
 * The contents and function list of a slot
 */
public class GUISlot {

    /**
     * the parent gui builder
     */
    private GUIBuilder builder;

    /**
     * Constructs a new GUISlot with the specified parent GUIBuilder.
     * <p>
     * This should not be accessed directly. Use GUIBuilder.newSlot() instead.
     * 
     * @param builder a parent GUI which manages the window/screen.
     * @param slotPosition where the slot should be in the GUI window, starting at 1.
     */
    public GUISlot(GUIBuilder builder, Integer slotPosition) {
        this.builder = builder;
        // this.setPosition(slotPosition);
    }

}