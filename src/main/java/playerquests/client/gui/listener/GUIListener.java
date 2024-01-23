package playerquests.client.gui.listener;

import java.util.Optional; // for handling when values may be null

import org.bukkit.event.EventHandler; // used to register methods as events for the listener
import org.bukkit.event.Listener; // listener type to implement this class with
import org.bukkit.event.inventory.InventoryClickEvent; // called when player clicks in an inventory
import org.bukkit.event.inventory.InventoryCloseEvent; // called when player closes an inventory
import org.bukkit.event.inventory.InventoryType; // used to check if the clicked area is the GUI itself

import playerquests.builder.gui.GUIBuilder; // used to control the GUIs
import playerquests.builder.gui.component.GUISlot; // contains GUI slot functions/content
import playerquests.product.GUI; // the GUI product itself

/**
 * Class used to hook special functionality onto regular 
 * game events.
 * <p>For example:
 * <ul>
 * <li>Inventory Click Events
 * </ul>
 */
public class GUIListener implements Listener {

    /**
     * the GUI builder this listener reports back to
     */
    private GUIBuilder builder;

    /**
     * Constructs a new GUIListener.
     * @param builder the gui builder it should send events to.
     */
    public GUIListener(GUIBuilder builder) {
        System.out.println("created a gui listener for : " + builder);

        this.builder = builder;
    }

    /**
     * Utility to check if the GUI is not null.
     * @return whether there is a GUI open or not.
     */
    private Boolean isGUI() {
        if (this.builder != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Utility to check if the GUI is not null and
     * if the player is clicking the actual GUI itself.
     * @return if in a GUI clicking in the GUI (CHEST inventory) area.
     */
    private Boolean isGUI(InventoryClickEvent event) {

        System.out.println("clickin on : " + this.builder);

        Boolean validInventory = Optional.ofNullable(
            event.getClickedInventory()).map(inventory -> { // get the clicked inventory
                // TODO: implement different inventory types; then compare to what the gui is set as instead of hardcoding as CHEST.
                return (inventory.getType() == InventoryType.CHEST); // check if the inventory type is a CHEST
            }).orElse(false);

        // if the null GUI check and this GUI check pass.
        return (this.isGUI() && validInventory);
    }

    private Boolean isEmptySlot(Integer slotPosition) {
        return this.builder.getSlot(slotPosition) == null;
    }
    
    /**
     * When in a GUI this event is important for registering clicks
     * and to stop items from being moved around like normal in an 
     * inventory.
     * @param event details about the inventory click event.
     */
    @EventHandler
    public void onClickItem(InventoryClickEvent event) {
        Integer slotPosition = event.getSlot() + 1; // get the real position of the slot

        if (this.isGUI(event) && !this.isEmptySlot(slotPosition)) { // if it's valid to register a GUI Slot click.
            event.setCancelled(true); // disallow taking slot items from GUI

            GUISlot slot = this.builder.getSlot(slotPosition);
            slot.execute(event.getWhoClicked()); // run the functions for this slot
            slot.clicked(); // register that this slot has been pressed
        }
    }

    /**
     * Handles what to do if the player closes the inventory window
     * on their own without being closed from {@link GUI} class.
     * @param event details about the inventory close event.
     */
    @EventHandler
    public void onCloseGUI(InventoryCloseEvent event) {
        if (this.isGUI() && !this.builder.getResult().isLocked()) {
            this.builder.dispose(); // remove gui and builder.
        }
    }
}