package playerquests.builder.quest.action.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.quest.action.RewardItem;
import playerquests.client.quest.QuestClient;

/**
 * Listener for immediately moving on from an action.
 */
public class RewardItemListener extends ActionListener<RewardItem> {

    /**
     * The item that triggered the listener.
     * Important since the listener captures state before the item.
     */
    public ItemStack lateItem;

    /**
     * Constructs a new reward item action listener.
     *
     * @param action the quest action this listener is for.
     * @param quester the quest client for the player.
     */
    public RewardItemListener(RewardItem action, QuestClient quester) {
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
        action.Check(this.quester, this);
    }
}
