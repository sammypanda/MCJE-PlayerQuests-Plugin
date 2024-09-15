package playerquests.product;

import java.util.Arrays;
import java.util.List;
import java.util.Map; // generic map type
import java.util.Optional; // evaluates nullable values

import org.bukkit.Bukkit; // to create the inventory
import org.bukkit.inventory.Inventory; // to modify the inventory
import org.bukkit.inventory.InventoryView; // the view of the GUI
import org.bukkit.inventory.ItemStack; // to visually represent buttons
import org.bukkit.inventory.meta.ItemMeta; // to modify button meta info
import org.bukkit.persistence.PersistentDataType; // tagging GUI items with GUI=true

import playerquests.Core; // getting the GUI NamespacedKey
import playerquests.builder.gui.GUIBuilder; // to control and modify the GUI
import playerquests.builder.gui.component.GUIFrame; // the content of the GUI like the title
import playerquests.builder.gui.component.GUISlot; // GUI buttons
import playerquests.utility.MaterialUtils; // converts string of item to presentable itemstack

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

    private GUIFrame frame;

    private Map<Integer, GUISlot> slots;

    /**
     * Instantiate a GUI with the defaults.
     * @param builder the builder which creates this GUI
     */
    public GUI(GUIBuilder builder) {
        this.builder = builder;

        this.frame = builder.getFrame();
        this.slots = builder.getSlots();
    }
    
    /**
     * Draws and displays a fresh instance of the current GUI on the viewers screen.
     */
    public void open() {
        this.inventory = Bukkit.createInventory( // create inventory
            this.builder.getDirector().getPlayer(), // the player who should see the inventory view
            this.frame.getSize() // the count of slots in the inventory
        );

        this.display(); // opening (and unlocking) the inventory window (InventoryView)

        this.draw(); // function containing all the builder components of the GUI
    }

    /**
     * Checks if the GUI is open by checking if the inventory view is set.
     * @return if the GUI is open
     */
    public Boolean isOpen() {
        return (this.view != null); // if the inventory view is not null
    }

    /**
     * Closes and disposes the GUI and resets builder.
     * @see #minimise() for non-destructive GUI window close.
     */
    public void close() {
        Optional.ofNullable(this.view).ifPresent(view -> view.close()); // close GUI if open

        this.builder.dispose();
    }

    /**
     * Draws all the gui elements assuming it's already open.
     */
    public void draw() {
        if (this.view == null) { // do not draw if there is no view
            throw new IllegalAccessError("Could not draw on a GUI which isn't open.");
        }

        this.frame = this.builder.getFrame();
        this.slots = this.builder.getSlots();

        // everything operating on InventoryView types
        drawFrame(); // populating the GUI frame
        drawSlots(); // populating the GUI slots
    }

    /**
     * Populate the outer GUI window.
     */
    private void drawFrame() {
        this.view.setTitle(this.frame.getTitle()); // set the GUI title
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
        this.slots.forEach((position, slot) -> {
            ItemStack item = MaterialUtils.toItemStack(slot.getItem()); // for setting the slot item
            ItemMeta itemMeta = item.getItemMeta(); // for editing the slot meta such as label

            // Strip the ItemMeta
            itemMeta.setLore(List.of());

            // Edit the ItemMeta
            itemMeta.setDisplayName(slot.getLabel()); // set the slot label

            itemMeta.setLore(null);
            if (!slot.getDescription().isBlank()) {
                itemMeta.setLore( // set the slot description
                    Arrays.asList(slot.getDescription()) // list: each line of the description
                );
            }

            // Determine the glint
            if (slot.isGlinting()) {
                itemMeta.setEnchantmentGlintOverride(true);
            }

            item.setAmount(slot.getCount());

            itemMeta.getPersistentDataContainer().set(Core.getGUIKey(), PersistentDataType.STRING, "true"); // set GUI=true

            // Return the ItemMeta to the ItemStack
            item.setItemMeta(itemMeta);

            // Set the slot item at the slot position
            if (position > 0 && position <= this.frame.getSize()) { // if the slot position is not out of bounds
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
        this.inventory.clear();
    }

    /**
     * Opens the GUI on the screen.
     */
    public void display() {
        this.view = builder.getDirector().getPlayer().openInventory(this.inventory); // open the GUI window

        this.locked = false; // unlock gui for potential deletion
    }

    /**
     * Opens the GUI on the screen.
     */
    public void minimise() {
        this.locked = true; // lock gui from deletion

        Optional.ofNullable(this.view).ifPresent(view -> view.close()); // hide the GUI window if open
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