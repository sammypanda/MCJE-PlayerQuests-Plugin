package playerquests.builder.quest.action.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.quest.action.CraftAction;
import playerquests.builder.quest.action.option.ItemsOption;
import playerquests.builder.quest.data.QuesterData;
import playerquests.utility.serialisable.ItemSerialisable;

/**
 * Listener for immediately moving on from an action.
 */
public class CraftListener extends ActionListener<CraftAction> {

    private Map<ItemSerialisable, Integer> progress;

    /**
     * Constructs a new empty action listener.
     * @param action the quest action this listener is for.
     * @param questerData the data about the quester.
     */
    public CraftListener(CraftAction action, QuesterData questerData) {
        super(action, questerData);
        progress = new HashMap<>(action.getData().getOption(ItemsOption.class).get().getItems()); // initialise progress
    }

    @EventHandler
    public void onPlayerCrafted(CraftItemEvent event) {
        Player questPlayer = questerData.getQuester().getPlayer(); // person who is playing the quest
        
        // determine if inventory holder is a player
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if ( ! (inventoryHolder instanceof Player) ) {
            return; // if not a player, exit
        }

        Player craftPlayer = (Player) inventoryHolder; // person who crafted the item

        // determine if questPlayer and craftPlayer are the same player
        if ( ! (craftPlayer.getUniqueId() == questPlayer.getUniqueId()) ) {
            return; // if not the current quester, exit
        }

        // determine if the item crafted is an item sought
        ItemStack itemStack = event.getRecipe().getResult();
        ItemSerialisable item = ItemSerialisable.fromItemStack(event.getRecipe().getResult());
        Integer quantityRemaining = progress.get(item);

        // exit if current wasn't in the list of required items to craft
        if (quantityRemaining == null) {
            return; 
        }

        // update value
        Integer updatedQuantityRemaining = quantityRemaining - itemStack.getAmount();
        if (updatedQuantityRemaining <= 0) {
            progress.remove(item);
            craftPlayer.sendMessage("<Finished crafting " + item.getName() + ">");
        } else {
            progress.put(item, updatedQuantityRemaining);
            craftPlayer.sendMessage("<Craft " + updatedQuantityRemaining + " more " + item.getName() + ">");
        }

        // check if no more else to craft
        if (progress.isEmpty()) {
            action.check(questerData);
        }
    }
}
