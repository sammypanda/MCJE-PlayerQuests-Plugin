package playerquests.product;

import org.bukkit.Bukkit; // to create the inventory
import org.bukkit.inventory.Inventory; // to modify the inventory
import org.bukkit.inventory.InventoryView; // the view of the GUI
import org.bukkit.inventory.ItemStack; // to visually represent buttons
import org.bukkit.inventory.meta.ItemMeta; // to modify button meta info

import playerquests.builder.gui.GUIBuilder; // to control and modify the GUI
import playerquests.builder.gui.component.GUIFrame; // the content of the GUI like the title
import playerquests.utility.GUIUtils; // converts string of item to presentable itemstack

/**
 * The GUI product as it appears on the players screen.
 */
public class GUI {

    /**
     * the builder class where the GUI info is kept and mutated.
     */
    private GUIBuilder builder;

    /**
     * if this gui is allowed to be deleted
     */
    private Boolean locked = false;

    /**
     * the opened inventory window (GUI window view).
     */
    private InventoryView view = null;

    /**
     * the inventory itself (GUI window)
     */
    private Inventory inventory = null;

    /**
     * Instantiate a GUI with the defaults.
     * @param builder the builder which creates this GUI
     */
    public GUI(GUIBuilder builder) {
        this.builder = builder;

        this.inventory = Bukkit.createInventory( // create default inventory
            this.builder.getDirector().getPlayer(), // the player who should see the inventory view
            this.builder.getFrame().getSize() // the count of slots in the inventory
        );
    }
    
    /**
     * Draws and displays a fresh instance of the current GUI on the viewers screen.
     */
    public void open() {
        this.locked = false; 

        this.display(); // opening the inventory window (InventoryView)

        this.draw(); // function containing all the builder components of the GUI
    }

    /**
     * Draws all the gui elements assuming it's already open.
     */
    public void draw() {
        if (this.view == null) { // do not draw if there is no view
            throw new IllegalAccessError("Could not draw on a GUI which isn't open.");
        }

        // everything operating on InventoryView types
        drawFrame(); // populating the GUI frame
        drawSlots(); // populating the GUI slots
    }

    /**
     * Populate the outer GUI window.
     */
    private void drawFrame() {
        GUIFrame frame = this.builder.getFrame();

        this.view.setTitle(frame.getTitle()); // set the GUI title
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
    private void drawSlots() {
        this.builder.getSlots().forEach((position, slot) -> {
            ItemStack item = GUIUtils.toItemStack(slot.getItem()); // for setting the slot item
            ItemMeta itemMeta = item.getItemMeta(); // for editing the slot meta such as label

            // Edit the ItemMeta
            // Set the slot label
            itemMeta.setDisplayName(slot.getLabel());

            // Return the ItemMeta to the ItemStack
            item.setItemMeta(itemMeta);

            // Set the slot item at the slot position
            if (position > 0 && position <= this.builder.getFrame().getSize()) { // if the slot position is not out of bounds
                this.inventory.setItem( // populate the slot
                    position - 1, // set at the index (starting from 0)
                    item // set the item/block representation
                );
            }
        });
    }

    /**
     * Opens the GUI on the screen.
     */
    public void display() {
        this.locked = false; // unlock gui for potential deletion

        this.view = builder.getDirector().getPlayer().openInventory(this.inventory); // open the GUI window
    }

    /**
     * Opens the GUI on the screen.
     */
    public void minimise() {
        this.locked = true; // lock gui from deletion

        this.view.close(); // hide the GUI window but not dispose anything
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

}