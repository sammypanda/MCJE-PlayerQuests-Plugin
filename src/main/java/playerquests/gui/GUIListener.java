package playerquests.gui;

import org.bukkit.Bukkit; // used to broadcast messages to the server
import org.bukkit.event.EventHandler; // used to register methods as events for the listener
import org.bukkit.event.Listener; // listener type to implement this class with
import org.bukkit.event.inventory.InventoryClickEvent; // called when player clicks in an inventory
import org.bukkit.event.inventory.InventoryCloseEvent; // called when player closes an inventory
import org.bukkit.event.inventory.InventoryType; // used to check if the clicked area is the GUI itself

/**
 * Class used to hook special functionality onto regular 
 * game events.
 * <p>For example:
 * <ul>
 * <li>Inventory Click Events
 * </ul>
 */
public class GUIListener implements Listener {

    private GUI gui;

    /**
     * Constructs a new GUIListener.
     * @param gui the gui window it should listen to events on.
     */
    public GUIListener(GUI gui) {
        this.gui = gui;
    }

    /**
     * Utility to check if the GUI is not null.
     * @return whether there is a GUI open or not.
     */
    private Boolean isGUI() {
        if (this.gui != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Utility to check if the GUI is not null and
     * if the player is clicking the actual GUI itself.
     * @return if in a GUI and clicking in the GUI (CHEST inventory) area.
     */
    private Boolean isGUI(InventoryClickEvent event) {
        InventoryType inventoryType = event.getClickedInventory().getType();

        // TODO: implement different inventory types; then compare to what the gui is set as instead of hardcoding as CHEST.

        if (this.isGUI() && inventoryType == InventoryType.CHEST) { 
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * When in a GUI this event is important for registering clicks
     * and to stop items from being moved around like normal in an 
     * inventory.
     * @param event details about the inventory click event.
     */
    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        

        Bukkit.broadcastMessage("test");

        if (this.isGUI(event)) { // if in a GUI and clicking in valid area.
            Bukkit.broadcastMessage(String.valueOf(event.getClickedInventory().getType()));
            Bukkit.broadcastMessage(String.valueOf(event.getSlot() + 1));
            Bukkit.broadcastMessage(gui.getSlot(event.getSlot() + 1).getLabel());

            event.setCancelled(true); // disallow taking slot items from GUI

            //              we can either pass off the ItemStack clicked on with event.getCurrentItem()
            // i prefer --> or the slot number that was clicked with event.getSlot()
        }
    }

    /**
     * Handles what to do if the player closes the inventory window
     * on their own without being closed from {@link GUI} class.
     * @param event details about the inventory close event.
     */
    @EventHandler
    public void onCloseGUI(InventoryCloseEvent event) {
        if (this.isGUI()) {
            this.gui.dispose(); // nullify some values in the GUI.
            this.gui = null; // we don't want the class instance anymore (also probably helps garbage collector)
        }
    }
}
