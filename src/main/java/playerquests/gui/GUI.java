package playerquests.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.HumanEntity;

/**
 * The interface for creating and opening a GUI.
 * Size can be changed to multiples of 9, from 0 up to 54.
 * The default size is 0.
 */
public class GUI {

    private HumanEntity humanEntity;
    private Inventory inventory;

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
        this.humanEntity.openInventory(this.inventory);

        // Testing
        System.out.println("Reached via GUI");
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
