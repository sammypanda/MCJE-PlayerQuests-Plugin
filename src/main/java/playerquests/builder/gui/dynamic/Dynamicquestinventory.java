package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.client.ClientDirector;
import playerquests.product.Quest;
import playerquests.utility.PluginUtils;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Shows a dynamic GUI of the current quest stock/inventory.
 */
public class Dynamicquestinventory extends GUIDynamic {

    /**
     * The quest inventory.
     */
    Map<Material, Integer> inventory = new HashMap<>();

    /**
     * The quest product.
     */
    Quest quest;

    /**
     * The required inventory.
     */
    Map<Material, Integer> requiredInventory;

    /**
     * Creates a dynamic GUI showing the quest inventory.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquestinventory(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // quest we are setting the inventory of
        this.quest = (Quest) this.director.getCurrentInstance(Quest.class);;

        // retrieve the items
        this.inventory = QuestRegistry.getInstance().getInventory(quest);
        this.requiredInventory = this.quest.getRequiredInventory();
    }

    @Override
    protected void execute_custom() {
        // sort the inventory ascending item amounts
        this.sortInventory();

        // set the GUI style
        GUIFrame frame = this.gui.getFrame();
        frame.setSize(54);
        frame.setTitle("Quest Inventory/Stock");

        // create a back button
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .onClick(() -> {
                new UpdateScreen(
                    Arrays.asList(this.previousScreen), 
                    director
                ).execute();
            });;

        // create restock button
        new GUISlot(gui, 52)
            .setItem(Material.CHEST)
            .setLabel("Restock")
            .onClick(() -> {
                // clear out
                this.gui.clearSlots();

                // ---- create restock screen ---- //
                frame.setTitle("Restock (Drag in items)");
                frame.setSize(54);

                // button to submit
                String displayName = "Done";
                InventoryView inventoryView = this.director.getPlayer().getOpenInventory();
                new GUISlot(gui, 54)
                    .setItem(Material.GREEN_DYE)
                    .setLabel(displayName)
                    .onClick(() -> {
                        for (ItemStack item : inventoryView.getTopInventory().getContents()) {
                            // if no item in the slot
                            if (item == null) {
                                continue;
                            }

                            // if the item is the submit button
                            if (item.getItemMeta().getDisplayName().equals(displayName)) {
                                continue;
                            }

                            Material itemMaterial = item.getType();
                            Integer itemCount = item.getAmount();
                            final Integer inventoryCount = this.inventory.get(itemMaterial);

                            // update inventory item
                            if (inventoryCount == null) {
                                this.inventory.put(itemMaterial, itemCount);
                                continue;
                            }
                            
                            this.inventory.put(itemMaterial, itemCount + inventoryCount);
                        };

                        // save the items!!
                        QuestRegistry.getInstance().setInventory(quest, this.inventory); // set inv and save

                        // go back
                        gui.clearSlots(); // blank the inner screen
                        this.execute(); // re-populate the GUI with the main screen
                    });

                // show this restock inner screen
                gui.getResult().draw();
            });

        // create prev button
        new GUISlot(gui, 53)
            .setItem(Material.GRAY_STAINED_GLASS_PANE)
            .setLabel("Prev");

        // create next button
        new GUISlot(gui, 54)
            .setItem(Material.GRAY_STAINED_GLASS_PANE)
            .setLabel("Next");

        // create inventory of required (and out of stock) and stocked
        Map<Material, Integer> predictiveInventory = PluginUtils.getPredictiveInventory(quest, this.inventory);

        // create slot for each inventory material
        predictiveInventory.entrySet().stream().anyMatch((entry) -> {
            Integer slot = gui.getEmptySlot();      

            if (slot == 45) {
                return true; // exit out early
            }

            Material material = entry.getKey();
            Integer predictedAmount = entry.getValue();
            Integer realAmount = QuestRegistry.getInstance().getInventory(quest).get(material);

            new GUISlot(gui, gui.getEmptySlot())
                .setItem(material)
                .setLabel(
                    realAmount == 0
                    ? ChatColor.RED + "Out of Stock" + ChatColor.RESET + " (" + realAmount + ")"
                    : (predictedAmount >= 0
                        ? Integer.toString(realAmount)
                        : ChatColor.YELLOW + "Not Enough Stock" + ChatColor.RESET + " (" + realAmount + ")")
                )
                .setGlinting(
                    predictedAmount >= 0 ? false : true
                );

            return false; // continue
        });
    }

    private void sortInventory() {
        // convert to sortable form
        List<Entry<Material, Integer>> list = new ArrayList<>(this.inventory.entrySet());

        // sort by amount
        list.sort(Entry.comparingByValue());

        // resubmit as linked list
        Map<Material, Integer> sortedInventory = new LinkedHashMap<>();
        for (Entry<Material, Integer> entry : list) {
            sortedInventory.put(entry.getKey(), entry.getValue());
        }

        this.inventory = sortedInventory;
    }    
}
