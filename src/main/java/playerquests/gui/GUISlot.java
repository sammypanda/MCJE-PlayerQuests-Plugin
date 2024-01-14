package playerquests.gui;

import java.util.ArrayList; // used to store a list of GUI Functions ('Meta Actions')
import java.util.Optional; // used to do conditional actions if a param list is null

import playerquests.gui.function.GUIFunction; // the class that handles Meta Actions
import playerquests.utils.GUIUtils; // GUI related methods to make this class less verbose

/**
 * The {@link GUISlot} class represents a slot in the inventory as GUI.
 * <p>
 * Each slot instance needs a parent {@link GUI}. But this class would not 
 * yet normally be used directly.
 */
public class GUISlot {

    /**
     * the parent GUI
     */
    private GUI parentGui;

    /**
     * no slot if none passed in // slot = this.parentGui.getNextSlot()?
     */
    private int slot = 0;

    /**
     * label of the item in the slot (requires whitespace to show as empty)
     */
    private String label = " ";

    /**
     * default item/block
     */
    private String item = "GRAY_STAINED_GLASS_PANE";

    /**
     * has this slot encountered a syntax error
     */
    private Boolean errored = false;

    /**
     * where the gui functions are added to and pulled from
     */
    private ArrayList<GUIFunction> functionList = new ArrayList<GUIFunction>();

    /**
     * Constructs a new {@link GUISlot} with the specified parent {@link GUI}.
     * <p>
     * This should not be accessed directly. Use {@link GUI#newSlot()} instead.
     * 
     * @param parentGui a parent GUI which manages the window/screen.
     * @param slotPosition where the slot should be in the GUI window, starting at 1.
     */
    public GUISlot(GUI parentGui, Integer slotPosition) {
        this.parentGui = parentGui;
        this.setPosition(slotPosition);
    }

    /**
     * Run the functions that are described in the GUI screen expression
     */
    public void execute() {
        this.functionList.forEach(function -> {
            function.setParentGUI(this.parentGui);
            function.execute();
        });
    }

    /**
     * Sets the slot position for this instance of {@link GUISlot}.
     * @param position the actual position of the slot, starting from 1
     */
    public void setPosition(Integer position) {
        this.slot = position;

        if (this.parentGui.getSlot(position) != null) { // remove slot if it already exists in HashMap
            this.parentGui.removeSlot(position); // remove at slot position (hashmap key)
        }

        this.parentGui.setSlot(position, this); // put our current GUISlot instead
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

    /**
     * Add a GUI Function ('Meta Action') to be executed when this GUI Slot
     * is used.
     * @param guiFunction the Meta Action name.
     * @param paramList the values for the Meta Action to use.
     */
    public void addFunction(GUIFunction guiFunction, ArrayList<Object> paramList) {
        paramList = Optional.ofNullable(paramList).orElse(new ArrayList<Object>()); // if there is no paramList, create an empty one

        this.functionList.add(guiFunction);
    }
}
