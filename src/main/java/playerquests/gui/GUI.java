package playerquests.gui;

import org.bukkit.Bukkit; // used to refer to base spigot/bukkit methods
import org.bukkit.inventory.Inventory; // used to manage the GUI
import org.bukkit.inventory.InventoryView; // used to manage the GUI screen
import org.bukkit.entity.HumanEntity; // the subject(s) the GUI shows for

/**
 * The interface for creating and opening a GUI.
 * Size can be changed to multiples of 9, from 0 up to 54.
 * The default size is 0.
 */
public class GUI {

    private HumanEntity humanEntity; // the subject the screen will open on
    private Inventory inventory; // the empty inventory used as a GUI screen
    private InventoryView inventoryView; // the screen itself when open
    private String title; // the title of the screen (InventoryView)

    {
        this.inventory = Bukkit.createInventory(this.humanEntity, 0);
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
     */
    public void open() {
        // Display the inventory
        this.inventoryView = this.humanEntity.openInventory(this.inventory);

        // Set the title (done here as it requires InventoryView, or to construct a new GUI)
        this.inventoryView.setTitle(this.title);

        // Testing
        System.out.println("Reached via GUI");
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
     * Get's the subject who is set to view the GUI.
     * @return The Human entity which the GUI was set to.
     */
    public HumanEntity getViewer() {
        return this.humanEntity;
    }

    // getScreen() = the JSON layout?
}
