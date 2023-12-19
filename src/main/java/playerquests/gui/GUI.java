package playerquests.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.HumanEntity;

/**
 * The interface for creating and opening a GUI.
 */
public class GUI {

    private HumanEntity humanEntity;

    /**
     * Constructs a new GUI instance.
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
        // Create the inventory
        Inventory inventory = Bukkit.createInventory(this.humanEntity, 9);

        // Display the inventory
        this.humanEntity.openInventory(inventory);

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
