package playerquests.builder.quest.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.RewardItemListener;
import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Action for giving items to the quester.
 */
public class RewardItem extends QuestAction {

    /**
     * Default constructor (for Jackson)
     */
    public RewardItem() {}

    /**
     * Gives items to the player.
     * @param stage stage this action belongs to
     */
    public RewardItem(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        return Arrays.asList(
            ActionOption.ITEMS
        );
    }

    @Override
    protected Optional<String> custom_Validate() {
        return Optional.empty();
    }

    @Override
    protected void custom_Run(QuestClient quester) {
        Player player = quester.getPlayer();
        Location location = player.getLocation();
        Quest quest = this.getStage().getQuest();

        // take items from quest inventory and give to player
        Map<Material, Integer> inventory = QuestRegistry.getInstance().getInventory(quest);
        this.items.forEach((material, count) -> {
            ItemStack item = new ItemStack(material, count);

            // subtract from quest inventory            
            Integer inventoryCount = inventory.get(material);
            if (inventoryCount == null) { inventoryCount = 0; }
            Integer finalCount = inventoryCount - count;
            inventory.put(material, finalCount);
            QuestRegistry.getInstance().setInventory(quest, inventory);

            // give to player
            location.getWorld().dropItem(location, item);

            // decide if (out of stock) and needs to be untoggled
            if (finalCount <= 0) {
                QuestRegistry.getInstance().getQuest(quest.getID()).toggle(false);
            }
        });
    }

    @Override
    protected ActionListener<?> custom_Listener(QuestClient quester) {
        return new RewardItemListener(this, quester);
    }

    /**
     * Check if the player has picked up the items.
     * 
     * @param quester the representing class of the quest gamer
     * @param listener instance of the gather item listener to call the check
     * @return if the player has the items.
     */
    @Override
    protected Boolean custom_Check(QuestClient quester, ActionListener<?> listener) {
        Map<Material, Integer> collectedItems = new HashMap<>();
        Player player = quester.getPlayer();

        // establish the inventory (adding the late item; the item which triggered the listener)
        PlayerInventory playerInventory = player.getInventory();
        RewardItemListener rewardListener = (RewardItemListener) listener;
        ArrayList<ItemStack> inventory = new ArrayList<>(Arrays.asList(playerInventory.getContents()));
        inventory.add(rewardListener.lateItem);

        // Collect all the items picked up so far
        for (ItemStack item : inventory) {
            // if nothing in this slot
            if (item == null) {
                continue;
            }

            Material material = item.getType();

            // if item isn't one that was rewarded
            if (!this.items.containsKey(material)) {
                continue;
            }

            Integer currentAmount = collectedItems.get(material);
            if (currentAmount == null) { currentAmount = 0; }

            // put the items in the list, but only go up to matching the original amount
            collectedItems.put(material, Math.clamp(currentAmount + item.getAmount(), 0, this.items.get(material)));
        }

        if (!collectedItems.equals(this.items)) {
            return false;
        }

        return true; // All checks passed
    }

    @Override
    protected Boolean custom_Finish(QuestClient quester, ActionListener<?> listener) {
        Player player = quester.getPlayer();

        // send messages
        player.sendMessage(" ");
        player.sendMessage("# Rewarded Items");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");

        return true;
    }
    
}
