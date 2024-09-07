package playerquests.builder.quest.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import playerquests.Core;
import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;

/**
 * Action for players gathering items.
 */
public class GatherItem extends QuestAction {

    private class GatherItemListener implements Listener {
        
        /**
         * The class that owns this listener.
         */
        private GatherItem parentClass;

        /**
         * The player to listen to item gathering on.
         */
        private Player player;

        /**
         * The quest client for the player.
         */
        private QuestClient quester;

        /**
         * Constructs a new {@code SelectBlockListener}.
         *
         * @param parent the parent {@code SelectBlock} instance
         * @param player the player associated with this listener
         */
        public GatherItemListener(GatherItem parent, QuestClient quester) {
            this.parentClass = parent;
            this.player = quester.getPlayer();
            this.quester = quester;

            Bukkit.getPluginManager().registerEvents(this, Core.getPlugin());

            // check quester inventory from beginning
            parent.check(quester, this);
        }

        @EventHandler
        public void onPickupItem(EntityPickupItemEvent event) {
            Player player = (Player) event.getEntity();

            // don't continue if not matching the player for this listener
            if (this.player != player) {
                return;
            }

            parentClass.check(this.quester, this, event.getItem().getItemStack());
        }

        @EventHandler
        public void onCraftItem(CraftItemEvent event) {
            Player player = (Player) event.getWhoClicked();

            // don't continue if not matching the player for this listener
            if (this.player != player) {
                return;
            }

            parentClass.check(this.quester, this, event.getRecipe().getResult());
        }

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

            ItemStack lateItem = event.getCursor(); // get item that was just placed

            // when the item is being moved in from another inventory
            if (!(event.getClickedInventory() instanceof PlayerInventory)) {
                parentClass.check(this.quester, this, event.getCurrentItem());
            }

            // don't continue if is a shift click but didn't meet ^ above conditional
            if (event.isShiftClick()) {
                return;
            }

            // check if all items have been gathered
            parentClass.check(this.quester, this, lateItem);
        }

        public void close() {
            HandlerList.unregisterAll(this);
        }
    }

    /**
     * Default constructor (for Jackson)
    */
    public GatherItem() {}
    
    /**
     * Check the player inventory for the gathered items.
     * 
     * If yes, it will continue onto finishing the action.
     * @param quester the representing class of the quest gamer
     * @param listener instance of the gather item listener to call the check
     * @param lateItem the item that is added to the inventory after this is called by the listener
     */
    public void check(QuestClient quester, GatherItemListener listener, ItemStack lateItem) {
        Player player = quester.getPlayer();
        Inventory inventory = player.getInventory();
        Map<Material, Integer> itemsCollected = new HashMap<Material, Integer>();

        // set up list of inventory items to loop through (and add the unaccounted for late item)
        List<ItemStack> inventoryList = new ArrayList<ItemStack>(Arrays.asList(inventory.getContents()));
        if (lateItem != null) { inventoryList.add(lateItem); }

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

        // finish if all desired items are collected
        if (this.items.equals(itemsCollected)) {
            this.finish(quester, listener);
            return;
        }
    }

    /**
     * Check the player inventory for the gathered items.
     * 
     * If yes, it will continue onto finishing the action.
     * @param quester the representing class of the quest gamer
     * @param listener instance of the gather item listener to call the check
     */
    public void check(QuestClient quester, GatherItemListener listener) {
        check(quester, listener, null);
    }

    private void finish(QuestClient quester, GatherItemListener listener) {
        Player player = quester.getPlayer();
        player.sendMessage("# Items gathered!");

        // close listener
        listener.close();

        // continue in quest sequence
        quester.gotoNext(this);
    }

    /**
     * Produces dialogue from an NPC.
     * @param stage stage this action belongs to
     */
    public GatherItem(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        List<ActionOption> options = new ArrayList<ActionOption>();

        options.add(ActionOption.ITEMS);

        return options;
    }

    @Override
    public void Run(QuestClient quester) {
        Player player = quester.getPlayer();

        // send action/task description
        player.sendMessage(" ");
        player.sendMessage("# Collect items:");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");

        // create (and register) listener that will trigger checks/finishes
        new GatherItemListener(this, quester);
    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }
    
}
