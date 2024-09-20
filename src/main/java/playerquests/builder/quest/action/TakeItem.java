package playerquests.builder.quest.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.TakeItemListener;
import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Action for taking an item from the quester.
 */
public class TakeItem extends QuestAction {

    /**
     * Default constructor (for Jackson)
     */
    public TakeItem() {}

    /**
     * Takes items from the player.
     * @param stage stage this action belongs to
     */
    public TakeItem(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        return Arrays.asList(
            ActionOption.ITEMS
        );
    }

    @Override
    public void custom_Run(QuestClient quester) {}

    /**
     * Take the items.
     * It's safe to assume the items can be taken during the func 
     * runtime. Since it's been checked beforehand.
     * @param quester the representing class of the quest gamer
     * @param listener instance of the gather item listener to call the check
     * @return if taking items was successful
     */
    @Override
    protected Boolean custom_Finish(QuestClient quester, ActionListener<?> listener) {
        Player player = quester.getPlayer();
        PlayerInventory inventory = player.getInventory();
        Map<Material, Integer> remainingItems = new HashMap<>(this.items);
        ItemStack emptyItem = new ItemStack(Material.AIR, 0);
        Quest quest = QuestRegistry.getInstance().getQuest(this.getStage().getQuest().getID());
        Map<Material, Integer> questInventory = QuestRegistry.getInstance().getInventory(quest);

        Boolean success = IntStream.range(0, inventory.getSize())
            .anyMatch((slot) -> {
                // case to exit
                if (remainingItems.isEmpty()) {
                    return true;
                }

                ItemStack item = inventory.getItem(slot);
                
                // don't continue if no item
                if (item == null) {
                    return false;
                }

                Material material = item.getType(); // the material to identify the item
                Integer count = item.getAmount(); // the amount we have in this slot stack
                Integer takeItemCount = this.items.get(material); // the amount we are wanting to take

                // don't continue if irrelevant item
                if (takeItemCount == null) {
                    return false;
                }

                // get how much of this material we have so far
                Integer remainingItemCount = remainingItems.get(material);
                if (remainingItemCount == null) {
                    remainingItemCount = 0;
                }

                // add it to the list, in case, for instance we have 10 ItemStacks with 1 item amount.
                remainingItems.put(material, remainingItemCount + takeItemCount);

                // if count is under how much we want to take
                if (count < takeItemCount) {
                    remainingItems.put(material, takeItemCount - count); // subtract how much we had in this stack from the remaining items list
                    inventory.setItem(slot, emptyItem); // remove the stack completely
                    return false;
                }

                // give to inventory
                Integer inventoryCount = questInventory.get(material);
                if (inventoryCount != null) {
                    questInventory.put(material, inventoryCount + takeItemCount);
                } else {
                    questInventory.put(material, count);
                }

                // if count matches the same as how much we want to take
                if (count == takeItemCount) {
                    remainingItems.remove(material);
                    inventory.setItem(slot, emptyItem); // remove the stack completely
                    return false;
                }

                // if count is over how much we want to take
                if (count > takeItemCount) {
                    remainingItems.remove(material);
                    inventory.setItem(slot, new ItemStack(material, count - takeItemCount)); // subtract how much we want to take from the stack
                    return false;
                }

                // continue
                return false;
            });

        // mark as failed if couldn't empty remaining items list
        if (!success) {
            return false;
        }

        // mark as successful and submit to inventory
        QuestRegistry.getInstance().setInventory(quest, questInventory);

        // send messages
        player.sendMessage(" ");
        player.sendMessage("# Taking Items");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");

        // mark as successful
        return true;
    }

    @Override
    protected void custom_Listener(QuestClient quester) {
        new TakeItemListener(this, quester);
    }

    @Override
    protected Optional<String> custom_Validate() {
        return Optional.empty();
    }

    @Override
    protected Boolean custom_Check(QuestClient quester, ActionListener<?> listener) {
        Player player = quester.getPlayer(); // get the questing player
        List<ItemStack> inventoryList = new ArrayList<ItemStack>(Arrays.asList(player.getInventory().getContents())); // ..their inventory
        Map<Material, Integer> itemsChecked = new HashMap<>();
        List<Material> itemsPassed = new ArrayList<>();

        // check each inventory slot
        // and determine if it passes the check (if the player has the correct items/amounts to take)
        Boolean success = inventoryList.stream().anyMatch(item -> {
            // if no item in this slot
            if (item == null) {
                return false;
            }

            Material material = item.getType();
            Integer count = item.getAmount();

            // if this item isn't one to take
            if (!this.items.containsKey(material)) {
                return false;
            }

            // get amount checked/amount to take
            Integer checkedCount = itemsChecked.get(material);
            Integer takeCount = this.items.get(material);

            // if this item isn't in the list yet
            if (checkedCount == null) {
                checkedCount = 0;
            }

            // get amount of item in inventory, up to the amount to take
            Integer clamp = Math.clamp(count+checkedCount, 0, takeCount);
            itemsChecked.put(material, clamp);

            // if the amount is the amount to take
            if (clamp == takeCount) {
                itemsPassed.add(material);
            }

            // if successful
            return this.items.size() == itemsPassed.size();
        });

        // check passing condition
        //
        // fail case: if the size of items to take is not the same as size of items 
        // from inventory that match the required quantity and material.
        if (!success) {
            return false;
        }

        // ..otherwise successful pass of the check
        return true;
    }
}
