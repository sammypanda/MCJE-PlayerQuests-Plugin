package playerquests.gui;

import playerquests.utils.GUIUtils; // GUI related methods to make this class less verbose

/**
 * The {@link GUISlot} class represents a slot in the inventory as GUI.
 * <p>
 * Each slot instance needs a parent {@link GUI}. But this class would not 
 * yet normally be used directly.
 */
public class GUISlot {

    private GUI gui; // the parent GUI
    private int slot = 0; // no slot if none passed in // slot = this.gui.getNextSlot()?
    private String label = " "; // label of the item in the slot (requires whitespace to show as empty)
    private String item = "GRAY_STAINED_GLASS_PANE"; // default item/block
    private Boolean errored = false; // has this slot encountered a syntax error

    /**
     * Constructs a new {@link GUISlot} with the specified parent {@link GUI}.
     * <p>
     * This should not be accessed directly. Use {@link GUI#newSlot()} instead.
     * 
     * @param gui a parent GUI which manages the window/screen.
     * @param slotPosition where the slot should be in the GUI window, starting at 1.
     */
    public GUISlot(GUI gui, Integer slotPosition) {
        this.gui = gui;
        this.setPosition(slotPosition);
    }

    /**
     * Sets the slot position for this instance of {@link GUISlot}.
     * @param position the actual position of the slot, starting from 1
     */
    public void setPosition(Integer position) {
        this.slot = position;

        if (gui.getSlot(position) != null) { // remove slot if it already exists in HashMap
            gui.removeSlot(position); // remove at slot position (hashmap key)
        }

        gui.setSlot(position, this); // put our current GUISlot instead
    }

    /**
     * Gets the intended slot position for this instance of {@link GUISlot}.
     * @return slot the slot position.
     */
    public Integer getPosition() {
        return this.slot;
    }
    
    /**
     * Sets the hover label for this instance of {@link GUISlot}.
     * @param label the desription of what the element does/is for.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets the hover label for this instance of {@link GUISlot}.
     * @return label the label when hovering over the slot.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Sets the item/block that will fill the slot for this instance of {@link GUISlot}.
     * @param item the closest string representation to the {@link org.bukkit.Material} ENUM.
     */
    public void setItem(String item) {
        try { // check if the item would create a valid ItemStack (the Material exists and isn't legacy)
            GUIUtils.toItemStack(item);
            this.item = item; // if no issue caught overwrite the default slot item
        } catch (IllegalArgumentException exception) { // this means the ItemStack failed to construct
            this.errored = true;
            this.item = "RED_STAINED_GLASS_PANE"; // express that there was a problem visually by using an alarming item
            System.err.println(exception.getMessage());
        }
    }

    /**
     * Gets the item/block that will fill the slot for this instance of {@link GUISlot}.
     * @return item the raw {@link String} representation of the item.
     */
    public String getItem() {
        return this.item;
    }

    /**
     * Determine whether the instance has encountered an error.
     * @return errored true or false value for if it has errored.
     */
    public Boolean hasError() {
        return this.errored;
    }
}
