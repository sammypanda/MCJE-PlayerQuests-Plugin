package playerquests.builder.quest.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

        // send messages
        player.sendMessage(" ");
        player.sendMessage("# Rewarding Items");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");
    }

    @Override
    protected void custom_Listener(QuestClient quester) {
        new RewardItemListener(this, quester);
    }

    /**
     * Just passes the check indiscrimnantly.
     * 
     * @return true as it shouldn't not succeed. Whether
     * the player picks up the item is not considered this
     * actions success condition.
     */
    @Override
    protected Boolean custom_Check(QuestClient quester, ActionListener<?> listener) {
        return true;
    }

    @Override
    protected Boolean custom_Finish(QuestClient quester, ActionListener<?> listener) {
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

        return true;
    }
    
}
