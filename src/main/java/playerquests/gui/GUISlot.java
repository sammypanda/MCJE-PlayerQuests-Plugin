package playerquests.gui;

import java.util.ArrayList; // used to store a list of GUI Functions ('Meta Actions')
import java.util.Optional; // used to do conditional actions if a param list is null

import org.bukkit.ChatColor; // used to customise all kinds of in-game text
import org.bukkit.entity.HumanEntity; // the type for the identifying the player

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
     * function that can be set to run when this slot is clicked
     */
    private Runnable onClick;

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
    public void execute(HumanEntity player) {
        if (this.functionList.isEmpty()) { return; }

        // get first function (the function will request the next when it is ready)
        GUIFunction function = this.functionList.get(0);
        
        // run repeatable execute tasks
        function.setPlayer(player);
        function.setParentSlot(this);
        function.setParentGUI(this.parentGui);
        function.execute();
    }

    /**
     * Run the NEXT function that is described in the GUI screen expression
     */
    public void executeNext(HumanEntity player) {
        // if no more functions, don't continue
        if (this.functionList.size() == 0) { return; }

        // pop the first function off the list to reveal the next
        this.functionList.remove(0);

        // execute the next function
        this.execute(player);
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
     * <p>
     * Includes some processing/formatting of the label.
     * @param label the desription of what the element does/is for.
     */
    public void setLabel(String label) {
        String errorLabel = "(Error)";

        // Evaluate label for error prefix and avoid malformatting labels
        label = String.format("%s%s%s%s", 
            ChatColor.RESET, // remove the italics set when changing from default item display name
            this.hasError() ? errorLabel : "", // add an error notice if applicable
            this.hasError() && !label.equals(" ") ? " " : "", // put whitespace if applicable
            this.hasError() && label.equals(" ") ? label.trim() : label // add the real label if applicable
        );
        
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

        guiFunction.setParams(paramList); // set the params for the function
        this.functionList.add(guiFunction);
    }

    /**
     * Get the current list of the GUI functions (remaining).
     */
    public ArrayList<GUIFunction> getFunctionList() {
        return this.functionList;
    }

    /**
     * Sets a function to be executed when the slot is clicked.
     * <p>
     * Template functions take precedent (GUIFunction).
     * If a function is to be added from within the code-land, it 
     * should use this to set the function to be executed.
     * @param onClick the function to run when the GUISlot is pressed
     */
    public void onClick(Runnable onClick) {
        this.onClick = onClick;
    }

    /**
     * Run the function set on this slot.
     * @see #onClick(Runnable)
     */
    public void clicked() {
        if (this.onClick != null) {
            onClick.run();
        }
    }
}
