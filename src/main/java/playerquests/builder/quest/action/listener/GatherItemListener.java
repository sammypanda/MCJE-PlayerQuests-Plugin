package playerquests.builder.quest.action.listener;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import playerquests.builder.quest.action.GatherItem;
import playerquests.client.quest.QuestClient;

/**
 * Listener for when a player gathers items.
 */
public class GatherItemListener extends ActionListener<GatherItem> {
        
    /**
     * The item that triggered the listener.
     * Important since the listener captures state before the item.
     */
    public ItemStack lateItem;

    /**
     * Constructs a new gather item action listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public GatherItemListener(GatherItem action, QuestClient quester) {
        super(action, quester);
    }

    /**
     * When the player picks up an item
     * @param event triggered by picking up items
     */
    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        Player player = (Player) event.getEntity();

        // don't continue if not matching the player for this listener
        if (this.player != player) {
            return;
        }
        
        this.lateItem = event.getItem().getItemStack();
        action.Check(this.quester, this);
    }

    /**
     * When an item is moved in the players inventory
     * @param event triggered by clicks in the inventory
     */
    @EventHandler
    public void onMoveItem(InventoryClickEvent event) {
        HumanEntity player = (HumanEntity) this.player; 

        // don't continue if not matching the player for this listener
        if (!event.getViewers().contains((player))) {
            return;
        }

        // don't continue not considered a valid inventory action (placing an item into the inventory adjacent)
        Set<InventoryAction> allowedActions = EnumSet.of(
            InventoryAction.PLACE_ALL, 
            InventoryAction.PLACE_ONE, 
            InventoryAction.PLACE_SOME, 
            InventoryAction.MOVE_TO_OTHER_INVENTORY);
        if (!allowedActions.contains(event.getAction())) {
            return;
        }

        // when the item is being moved in from another inventory
        if (!(event.getClickedInventory() instanceof PlayerInventory)) {
            this.lateItem = event.getCurrentItem();
            action.Check(this.quester, this);
        }

        // don't continue if is a shift click but didn't meet ^ above conditional
        if (event.isShiftClick()) {
            return;
        }

        // check if all items have been gathered
        this.lateItem = event.getCursor();
        action.Check(this.quester, this);
    }
}
