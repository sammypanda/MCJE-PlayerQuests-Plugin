package playerquests.product;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity; // for identifying the player
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUIFrame;

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
     * Populate the inner GUI slots.
     */
    private void drawSlots() {
        this.builder.getSlots();
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
}