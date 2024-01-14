package playerquests.gui;

import java.util.HashMap; // used to hold and manage info about the GUI slots
import java.util.Optional; // used to check and work with nullable values
import java.util.Set; // used to retrieve the key values for this.slots
import java.util.stream.IntStream; // used to find the next possible empty slot 

import org.bukkit.Bukkit; // used to refer to base spigot/bukkit methods
import org.bukkit.entity.HumanEntity; // the subject(s) the GUI shows for
import org.bukkit.event.HandlerList; // list of event handlers; used to unload a listener
import org.bukkit.inventory.Inventory; // used to manage the GUI
import org.bukkit.inventory.InventoryView; // used to manage the GUI screen
import org.bukkit.inventory.ItemStack; // used to place an item visually in a slot on the GUI
import org.bukkit.inventory.meta.ItemMeta; // used to edit the label of the slot

import playerquests.Core; // used to get the Plugin instance
import playerquests.annotations.Key; // used to correspond setters to a key-value pattern
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

    /**
     * subject the screen will open on
     */
    private HumanEntity humanEntity;
    
    /**
     * empty inventory used as a GUI screen
     */
    private Inventory inventory;
    
    /**
     * screen itself when open
     */
    private InventoryView inventoryView;
    
    /**
     * title of the screen (InventoryView)
     */
    private String title = "";
    
    /**
     * amount of slots in the GUI screen (Inventory)
     */
    private Integer size = 9;
    
    /**
     * event listener for gui events
     */
    private GUIListener guiListener = new GUIListener(this);
    
    /**
     * list of slots keyed by the position in the inventory/GUI they occupy
     */
    private HashMap<Integer, GUISlot> slots = new HashMap<Integer, GUISlot>();

    /**
     * if this gui is allowed to be deleted
     */
    private Boolean locked = false;

    {
        // default inventory
        this.inventory = Bukkit.createInventory(this.humanEntity, this.size);

        // default title 
        this.title = "";

        // adding listening to when gui events occur
        Bukkit.getPluginManager().registerEvents(this.guiListener, Core.getPlugin());

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current instance of gui to be accessed with key-pair syntax
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
     * Displays a fresh instance of the current GUI on the viewers screen.
     */
    public void open() {
        this.locked = false; // unlock it for potential deletion

        display(); // opening the inventory window (InventoryView)

        draw(); // function containing all the builder components of the GUI
    }

    /**
     * Builds the gui without opening it in InventoryView.
     */
    private void draw() {
        if (inventoryView == null) {
            return;
        }

        // everything operating on InventoryView types
        buildFrame(); // populating the GUI frame
        buildSlots(); // populating the GUI slots
    }

    /**
     * Unsets any values if needed and calls on {@link #draw()}.
     */
    public void redraw() {
        // draw even if previous InventoryView is still open
        draw();
    }
    
    /**
     * Prepares the GUI window to be closed, in such a way that there
     * are no leftover objects or listeners.
     * @see #close() for closing GUI on the frontend.
     */
    public void dispose() {
        HandlerList.unregisterAll(this.guiListener); // unregister the listeners, don't need them if there is no GUI
        Core.getKeyHandler().deregisterInstance(this); // remove the current instance from key-pair handler
        
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
        Optional.ofNullable(this.inventoryView).ifPresent(view -> view.close()); // close GUI on the frontend.

        this.dispose(); // unload GUI on the backend
    }

    /**
     * Creates an InventoryView to show the inventory GUI.
     */
    private void display() {
        this.inventoryView = this.humanEntity.openInventory(this.inventory); // display the inventory
    }

    /**
     * Close but do not dispose of this GUI, opposite of {@link #display()}.
     */
    public void minimise() {
        this.locked = true;
        this.humanEntity.closeInventory();
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
        this.slots.forEach((position, slot) -> {
            ItemStack item = GUIUtils.toItemStack(slot.getItem()); // for setting the slot item
            ItemMeta itemMeta = item.getItemMeta(); // for editing the slot meta such as label

            // Edit the ItemMeta
            // Set the slot label
            itemMeta.setDisplayName(slot.getLabel());

            // Return the ItemMeta to the ItemStack
            item.setItemMeta(itemMeta);

            // Set the slot item at the slot position
            if (position > 0 && position <= this.size) { // if the slot position is not out of bounds
                this.inventory.setItem( // populate the slot
                    position - 1, // set at the index (starting from 0)
                    item // set the item/block representation
                );
            }
        });
    }

    /**
     * Responsible for clearing the GUI slots.
     * <p>
     * Useful for Dynamic GUIs which may want to 
     * show a new array of slots.
     */
    public void clearSlots() {
        this.slots.clear();
        this.inventory.clear();
    }

    /**
     * Creates a new slot within the GUI system.
     * <p>
     * Where the position of the slot is it's key.
     * <p>
     * To address the complexity of the slots array and how slots can be comprised
     * of many parts, this method instantiates a whole new {@link GUISlot} class.
     * Each new GUISlot instance is added to a list in the {@link GUI} 
     * class. This list serves as a centralized repository, ensuring easy access to slot 
     * information when building/opening the GUI.
     * @param position the inventory position this slot is
     * @return a new (and tracked!) instance of GUISlot
     */
    public GUISlot newSlot(Integer position) {
        GUISlot newSlot = new GUISlot(this, position);
        this.slots.put(position, newSlot);
        return newSlot;
    }

    /**
     * Creates a new slot at the next possible slot in the GUI system.
     * <p>
     * Be sure to use setPosition() on the GUISlot or it will
     * remain outside of the view.
     * @see #newSlot(Integer)
     * @return a new (and tracked!) instance of GUISlot
     */
    public GUISlot newSlot() {
        // if position is not passed in, place out of view
        return this.newSlot(0);
    }

    /**
     * Gets the GUI slot at the inventory slot position.
     * @param position the gui slot position, starting at 1.
     * @return the gui slot object.
     */
    public GUISlot getSlot(Integer position) {
        return this.slots.get(position);
    }

    /**
     * Removes the {@link GUISlot} at the inventory slot position.
     * @param position the gui slot position, starting at 1.
     */
    public void removeSlot(Integer position) {
        this.slots.remove(position);
    }

    /**
     * Update which slot of the GUI a {@link GUISlot} object shows in.
     * @param position the gui slot position, starting at 1.
     * @param slot the already created {@link GUISlot}  object to put in the slot.
     */
    public void setSlot(Integer position, GUISlot slot) {
        this.slots.put(position, slot);
    }

    /**
     * Sets the title of the screen.
     * Applies in GUI {@link #open()}, only for the top InventoryView. 
     * @param title The label of the GUI screen.
     */
    @Key("gui.title")
    public void setTitle(String title) {
        this.title = title; // class variable to set title when InventoryView becomes accessible
        redraw();
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
        redraw();
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

    /**
     * Protection from unintentional deletion
     * <ul>
     * <li>used when the inventory window is minimised.
     * </ul>
     * @return whether the gui can be deleted it or not
     */
    public Boolean isLocked() {
        return this.locked;
    }

    /**
     * Get the next empty slot available.
     */
    public Integer getEmptySlot() {
        Set<Integer> filledSlots = this.slots.keySet(); // get the positions of all currently stored slots

        Integer lowestEmptySlot = IntStream.iterate(1, i -> i + 1) // counter stream, starting at 1
            .filter(slot -> !filledSlots.contains(slot)) // conditional for adding to stream
            .findFirst() // terminate if there is a value in the stream
            .orElse(1); // default value

        return lowestEmptySlot; // the next empty slot
    }
}
