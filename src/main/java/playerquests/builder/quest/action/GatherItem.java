package playerquests.builder.quest.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.GatherItemListener;
import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;

/**
 * Action for players gathering items.
 */
public class GatherItem extends QuestAction {

    /**
     * Default constructor (for Jackson)
    */
    public GatherItem() {}

    /**
     * Produces dialogue from an NPC.
     * @param stage stage this action belongs to
     */
    public GatherItem(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        return Arrays.asList(
            ActionOption.ITEMS,
            ActionOption.FINISH_MESSAGE
        );
    }

    @Override
    public Optional<String> custom_Validate() {
        return Optional.empty();
    }

    @Override
    public void custom_Run(QuestClient quester) {
        Player player = quester.getPlayer();

        // send action/task description
        player.sendMessage(" ");
        player.sendMessage("# Collect items:");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");
    }

    @Override
    protected ActionListener<?> custom_Listener(QuestClient quester) {
        return new GatherItemListener(this, quester);
    }

    /**
     * Check the player inventory for the gathered items.
     * 
     * If yes, it will continue onto finishing the action.
     * @param quester the representing class of the quest gamer
     * @param listener instance of the gather item listener to call the check
     */
    @Override
    public Boolean custom_Check(QuestClient quester, ActionListener<?> listener) {
        // establish type
        if (!(listener instanceof GatherItemListener)) {
            return false;
        }
        GatherItemListener gatherListener = (GatherItemListener) listener;

        // establish values
        Player player = quester.getPlayer();
        Inventory inventory = player.getInventory();
        Map<Material, Integer> itemsCollected = new HashMap<Material, Integer>();

        // set up list of inventory items to loop through (and add the unaccounted for late item)
        List<ItemStack> inventoryList = new ArrayList<>(Arrays.asList(inventory.getContents()));
        if (gatherListener.lateItem != null) { inventoryList.add(gatherListener.lateItem); }

        // check item list against inventory, until fail
        inventoryList.forEach(itemStack -> {
            // exit if no item in this slot
            if (itemStack == null) {
                return;
            }

            // determine what to look for and how much
            Material material = itemStack.getType();
            Integer count = itemStack.getAmount();
            Integer desiredCount = this.items.get(material);

            // if there is no desired count (or the need is met) of this material, then exit
            if (desiredCount == null) {
                return;
            }

            // determine what we already have and need to add
            Integer collectedCount = Optional.ofNullable(itemsCollected.get(material)).orElse(0);
            Integer clamp = Math.clamp((count + collectedCount), collectedCount, desiredCount);

            // submit to list (if already collected some)
            if (collectedCount != 0) {
                itemsCollected.replace(material, clamp);
                return;
            }

            // submit to list if new
            itemsCollected.put(material, clamp);
        });

        // fail if not all desired items are collected
        if (!this.items.equals(itemsCollected)) {
            return false;
        }

        // otherwise finish
        return true;
    }

    protected Boolean custom_Finish(QuestClient quester, ActionListener<?> listener) {
        // establish finish message
        String message = this.getFinishMessage();
        // fallback finish message
        if (message == null) {
            message = "Items gathered!";
        }

        // message style
        final String finishMessage = String.format("%s %s",
            "#",
            message
        );

        // determine if should send
        if (!message.isBlank()) {
            // send finish message
            Player player = quester.getPlayer();
            player.sendMessage(finishMessage);
        }

        return true;
    }
    
}
