package playerquests.client.gui.listener;

import java.util.Optional; // for handling when values may be null

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler; // used to register methods as events for the listener
import org.bukkit.event.Listener; // listener type to implement this class with
import org.bukkit.event.inventory.InventoryClickEvent; // called when player clicks in an inventory
import org.bukkit.event.inventory.InventoryCloseEvent; // called when player closes an inventory
import org.bukkit.event.inventory.InventoryDragEvent; // called when player drags item in inventory
import org.bukkit.event.inventory.InventoryType; // used to check if the clicked area is the GUI itself
import org.bukkit.event.player.PlayerDropItemEvent; // called when player drops item out of inventory
import org.bukkit.inventory.PlayerInventory; // object representing a players inventory
import org.bukkit.persistence.PersistentDataType; // custom data attached to in-game objects

import playerquests.Core; // main class/starting point/global values
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
// TODO: implement different inventory types; then compare to what the gui is set as instead of hardcoding as CHEST.
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
     * Utility to check if the player is clicking the actual GUI itself.
     * @param event details about the inventory click event.
     * @return if the inventory is a GUI
     */
    private Boolean isGUIInventory(InventoryClickEvent event) {
        return Optional.ofNullable(
            event.getClickedInventory()).map(inventory -> { // get the clicked inventory
                return (inventory.getType() == InventoryType.CHEST); // check if the inventory type is a CHEST
            }).orElse(false);
    }

    private Boolean isEmptySlot(Integer slotPosition) {
        return this.builder.getSlot(slotPosition) == null;
    }

    /**
     * Hook to consider what happens when items are dragged in a GUI.
     * @param event details about the inventory drag event.
     */
    @EventHandler 
    public void onDragItem(InventoryDragEvent event) {
        // if is in a GUI, then cancel (see onDropItem)
        if (this.isGUI()) {
            event.setCancelled(true);
            return;
        }
    }

    
    @EventHandler
    public void onGUIClose(InventoryCloseEvent event) {
        // if is not in a GUI, then don't continue
        if (!this.isGUI()) {
            return;
        }

        // otherwise, as a way to fix an issue where 
        // players can forcibly close the inventory,
        // while holding a block. To obtain that block.

        // we can filter through all the players inv items
        // for candidates tagged with GUI=true.
        // taking place one tick after the close event
        // as a safety net.
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
            // if inventory is still open don't continue
            // (performance consideration as we are looping through each inventory itemstack)
            if (event.getView() == null) {
                return;
            }

            PlayerInventory playerInventory = event.getPlayer().getInventory();
            playerInventory.forEach(item -> {
                // don't continue if is an empty slot
                if (item == null) {
                    return;
                }

                // don't continue if candidate is not tagged with 'GUI'
                String guiKey = item.getItemMeta().getPersistentDataContainer().get(Core.getGUIKey(), PersistentDataType.STRING);
                if (guiKey == null) {
                    return;
                }

                // if candidate is tagged with GUI=true
                if (guiKey.equals("true")) {
                    // remove from the inventory
                    event.getPlayer().getInventory().remove(item);
                }
            });
        });
    }

    /**
     * Hook to consider what happens when items are dropped out of inventory in a GUI.
     * @param event details about the drop item event.
     */
    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        
        // if is in a GUI and dropped item tagged as GUI=true, then cancel (see onDragItem)
        if (
            this.isGUI() && 
            event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer().get(
                Core.getGUIKey(),
                PersistentDataType.STRING
            ) == "true"
        ) {
            event.setCancelled(true);
            return;
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
        Integer slotPosition = event.getSlot() + 1; // get the real position of the slot
        GUISlot slot = this.builder.getSlot(slotPosition); // get the slot data

        // if no GUI visible or no slot clicked
        if (!this.isGUI() || slot == null) {
            return;
        }

        switch (this.builder.getFrame().getMode()) {
            case CLICK:

                if (this.isGUIInventory(event) && !this.isEmptySlot(slotPosition)) {
                    event.setCancelled(true); // disallow taking slot items from GUI
                }

                break;

            case ARRANGE:

                if (!this.isGUIInventory(event)) { // if not the GUI..
                    event.setCancelled(true); // disallow placing GUI items into own inventory
                }

                break;

            default:
                break;
        }

        // running GUI functions
        slot.execute(event.getWhoClicked()); // run the functions for this slot
        slot.clicked(); // register that this slot has been pressed
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