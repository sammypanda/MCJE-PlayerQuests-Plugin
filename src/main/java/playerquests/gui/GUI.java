package playerquests.gui;

import java.util.ArrayList; // Used to hold and manage info about the GUI slots

import org.bukkit.Bukkit; // used to refer to base spigot/bukkit methods
import org.bukkit.ChatColor; // used to customise all kinds of in-game text
import org.bukkit.entity.HumanEntity; // the subject(s) the GUI shows for
import org.bukkit.event.HandlerList; // list of event handlers; used to unload a listener
import org.bukkit.inventory.Inventory; // used to manage the GUI
import org.bukkit.inventory.InventoryView; // used to manage the GUI screen
import org.bukkit.inventory.ItemStack; // used to place an item visually in a slot on the GUI
import org.bukkit.inventory.meta.ItemMeta; // used to edit the label of the slot

import playerquests.Core; // used to get the Plugin instance
import playerquests.utils.GUIUtils; // tools which help reduce the verbosity of GUI classes

/**
 * The interface for creating and opening a GUI.
 * Size can be changed to multiples of 9, from 0 up to 54.
 * The default size is 0.
 * <br>
 * <pre>
 * Usage:
 * <code>
 * getServer().getOnlinePlayers().iterator().forEachRemaining(player -> { // for this example, opening the gui for everyone
 *     GUILoader guiLoader = new GUILoader(player); // helps construct the gui
 *     GUI demo = guiLoader.load("demo"); // demo is a template file (json)
 *     demo.open(); // the method to show the gui on screen
 * });      
 * </code>
 * </pre>
 */
public class GUI {

    private HumanEntity humanEntity; // the subject the screen will open on
    private Inventory inventory; // the empty inventory used as a GUI screen
    private InventoryView inventoryView; // the screen itself when open
    private String title = ""; // the title of the screen (InventoryView)
    private Integer size = 9; // the amount of slots in the GUI screen (Inventory)
    private ArrayList<GUISlot> slots = new ArrayList<GUISlot>(); // the list of each slot with their own unique options
    private GUIListener guiListener = new GUIListener(this);

    {
        // default inventory
        this.inventory = Bukkit.createInventory(this.humanEntity, this.size);

        // disallow taking slot items from GUI
        Bukkit.getPluginManager().registerEvents(this.guiListener, Core.getPlugin());
    }

    /**
     * Constructs a new GUI instance to apply screens to.
     * Used to manage and control the GUI.
     * @param humanEntity   the (usually player) who will view the GUI.
     */
    public GUI(HumanEntity humanEntity) {
        this.humanEntity = humanEntity;
    }

    /**
     * Displays the current GUI on the viewers screen.
     * <p>
     * Comprised of: {@link #display()}, {@link #buildFrame()}, {@link #buildSlots()}.
     */
    public void open() {
        // everything operating on Inventory types
        // [not currently needed].

        display(); // opening the inventory window (InventoryView)

        // everything operating on InventoryView types
        buildFrame(); // populating the GUI frame
        buildSlots(); // populating the GUI slots
    }
    
    /**
     * Prepares the GUI window to be closed, in such a way that there
     * are no leftover objects or listeners.
     * @see #close() for closing GUI on the frontend.
     */
    public void dispose() {
        HandlerList.unregisterAll(this.guiListener); // unregister the listeners, don't need them if there is no GUI
        
        // nullify class values we are never going to use again
        this.humanEntity = null;
        this.inventory = null;
        this.inventoryView = null;
        this.title = null;
        this.size = null;
        this.slots = null;
        this.guiListener = null;
    }

    /**
     * Shuts the {@link #inventoryView} on the player screen, 
     * just the UI side of closing down the GUI.
     * @see #dispose() for unloading GUI on the backend.
     */
    public void close() {
        this.dispose(); // unload GUI on the backend

        this.inventoryView.close(); // close GUI on the frontend.
    }

    /**
     * Creates an InventoryView to show the inventory GUI.
     */
    private void display() {
        this.inventoryView = this.humanEntity.openInventory(this.inventory); // display the inventory
    }

    /**
     * Responsible for creating the base/frame of the GUI.
     * <p>
     * <ul>
     * <li>Sets the title.
     * <li><i>Size already set on init and by {@link #setSize(int)}.</i>
     * </ul>
     */
    private void buildFrame() {
        this.inventoryView.setTitle(this.title); // set the title
    }

    /**
     * Responsible for filling the GUI slots in according to
     * the configuration.
     * <p>
     * Turns the slots list into an iterator. For each key/option it
     * creates the according inventory representation.
     * <p>
     * <ul>
     * <li>Sets the slot position by checking it is not out of bounds
     * and correcting for the index starting at 0.
     * </ul>
     */
    private void buildSlots() {
        this.slots.iterator().forEachRemaining(slot -> {
            Integer position = slot.getSlot(); // for setting the slot position
            ItemStack item = GUIUtils.toItemStack(slot.getItem()); // for setting the slot item
            ItemMeta itemMeta = item.getItemMeta(); // for editing the slot meta such as label
            String errorLabel = "(Error)";

            // Evaluate label for error prefix and avoid malformatting labels
            slot.setLabel(String.format("%s%s%s%s", 
                ChatColor.RESET, // remove the italics set when changing from default item display name
                slot.hasError() ? errorLabel : "", // add an error notice if applicable
                slot.hasError() && !slot.getLabel().equals(" ") ? " " : "", // put whitespace if applicable
                slot.hasError() && slot.getLabel().equals(" ") ? slot.getLabel().trim() : slot.getLabel())); // add the real label if applicable

            // Edit the ItemMeta
            // Set the slot label
            itemMeta.setDisplayName(slot.getLabel());

            // Return the ItemMeta to the ItemStack
            item.setItemMeta(itemMeta);

            // Set the slot item at the slot position
            if (position > 0 && position <= this.size) { // if the slot position is not out of bounds
                this.inventoryView.setItem( // populate the slot
                    position - 1, // set at the index (starting from 0)
                    item // set the item/block representation
                );
            }
        });
    }

    /**
     * Creates a new slot within the GUI system.
     * <p>
     * To address the complexity of the slots array and how slots can be comprised
     * of many parts, this method instantiates a whole new {@link GUISlot} class.
     * Each new GUISlot instance is added to a list in the {@link GUI} 
     * class. This list serves as a centralized repository, ensuring easy access to slot 
     * information when building/opening the GUI.
     * @return a new (and tracked!) instance of GUISlot
     */
    public GUISlot newSlot() {
        GUISlot newSlot = new GUISlot(this);
        this.slots.add(newSlot);
        return newSlot;
    }

    /**
     * Sets the title of the screen.
     * Applies in GUI {@link #open()}, only for the top InventoryView. 
     * @param title The label of the GUI screen.
     */
    public void setTitle(String title) {
        this.title = title; // class variable to set title when InventoryView becomes accessible
    }

    /**
     * Provides the string value for the title of the screen.
     * @return title The label of the GUI screen.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set's the number of slots in the GUI screen.
     * <p>
     * Does this by recreating the inventory, as there is no way to
     * dynamically adjust the size after creation.
     * @param size the number of slots (has to be a multiple of 9. Like 9 or 18
     */
    public void setSize(int size) {
        this.size = size;
        this.inventory = Bukkit.createInventory(humanEntity, size);
    }

    /**
     * Get's the size of the GUI screen (inventory).
     * @return the number of slots in the inventory
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Get's the subject who is set to view the GUI.
     * @return The Human entity which the GUI was set to.
     */
    public HumanEntity getViewer() {
        return this.humanEntity;
    }
}
