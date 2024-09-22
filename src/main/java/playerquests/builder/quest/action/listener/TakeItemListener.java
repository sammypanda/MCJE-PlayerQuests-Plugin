package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import playerquests.Core;
import playerquests.builder.quest.action.TakeItem;
import playerquests.client.quest.QuestClient;

/**
 * Listener for if player has an item to take.
 */
public class TakeItemListener extends ActionListener<TakeItem> {

    /**
     * The item that triggered the listener.
     * Important since the listener captures state before the item.
     */
    public ItemStack lateItem;

    /**
     * Constructs a new take item listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public TakeItemListener(TakeItem action, QuestClient quester) {
        super(action, quester);
    }

    /**
     * When the player picks up an item.
     * @param event triggered by picking up items.
     */
    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        Player player = (Player) event.getEntity();

        // don't continue if not matching the player for this listener
        if (this.player != player) {
            return;
        }

        this.lateItem = event.getItem().getItemStack();

        // check after picking up
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            action.Check(this.quester, this);
        });
    }
}
