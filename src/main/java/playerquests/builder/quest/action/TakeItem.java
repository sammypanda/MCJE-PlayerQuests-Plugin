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

import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;

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
    public void Run(QuestClient quester) {
        Boolean failed = false;
        Player player = quester.getPlayer();

        // check (exit if the player does not have the items to take)
        if (!check(player)) {
            failed = true;
        }

        // take the items (exit if the taking wasn't successful)
        if (!take(player)) {
            failed = true;
        }

        if (failed) {
            player.sendMessage("WIP: could not TakeItem, you don't have the items");
            return;
        }

        // send messages
        player.sendMessage(" ");
        player.sendMessage("# Taking Items");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");

        // continue to next action
        quester.gotoNext(this);
    }

    /**
     * Check if the player has the items to take.
     * @param player the player to check
     * @return if can take items from player
     */
    private Boolean check(Player player) {
        List<ItemStack> inventoryList = new ArrayList<ItemStack>(Arrays.asList(player.getInventory().getContents()));
        Map<Material, Integer> itemsChecked = new HashMap<>();
        List<Material> itemsPassed = new ArrayList<>();

        // check each inventory slot
        // and determine if it passes the check (if the player has the correct items/amounts to take)
        inventoryList.forEach(item -> {
            // if no item in this slot
            if (item == null) {
                return;
            }

            Material material = item.getType();
            Integer count = item.getAmount();

            // if this item isn't one to take
            if (!this.items.containsKey(material)) {
                return;
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
        });

        // check passing condition
        //
        // fail case: if the size of items to take is not the same as size of items 
        // from inventory that match the required quantity and material.
        if (this.items.size() != itemsPassed.size()) {
            return false;
        }

        // ..otherwise successful pass of the check
        return true;
    }

    /**
     * Take the items.
     * It's safe to assume the items can be taken during the func 
     * runtime. Since it's been checked beforehand.
     * @param player the player to take from.
     * @return if taking items was successful
     */
    private Boolean take(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<Material, Integer> remainingItems = new HashMap<>(this.items);

        IntStream.range(0, inventory.getSize())
            .forEach(slot -> {
                ItemStack item = inventory.getItem(slot);
                
                // don't continue if no item
                if (item == null) {
                    return;
                }

                Material material = item.getType(); // the material to identify the item
                Integer count = item.getAmount(); // the amount we have in this slot stack
                Integer takeItemCount = this.items.get(material); // the amount we are wanting to take

                // don't continue if irrelevant item
                if (takeItemCount == null) {
                    return;
                }

                // get how much of this material we have so far
                Integer remainingItemCount = remainingItems.get(material);
                if (remainingItemCount == null) {
                    remainingItemCount = 0;
                }

                // add it to the list, in case, for instance we have 10 ItemStacks with 1 item amount.
                remainingItems.put(material, remainingItemCount + takeItemCount);

                // if count matches the same as how much we want to take
                if (count == takeItemCount) {
                    remainingItems.remove(material);
                    inventory.remove(item); // remove the stack completely
                    return;
                }

                // if count is over how much we want to take
                if (count > takeItemCount) {
                    remainingItems.remove(material);
                    inventory.setItem(slot, new ItemStack(material, count - takeItemCount)); // subtract how much we want to take from the stack
                    return;
                }

                // if count is under how much we want to take
                if (count < takeItemCount) {
                    remainingItems.put(material, takeItemCount - count); // subtract how much we had in this stack from the remaining items list
                    inventory.remove(item);
                    return;
                }
            });

        // mark as failed if couldn't empty remaining items list
        if (!remainingItems.isEmpty()) {
            return false;
        }

        // mark as successful
        return true;
    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }
}
