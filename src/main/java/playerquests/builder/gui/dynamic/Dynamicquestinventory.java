package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.client.ClientDirector;
import playerquests.product.Quest;
import playerquests.utility.PluginUtils;
import playerquests.utility.serialisable.ItemSerialisable;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Shows a dynamic GUI of the current quest stock/inventory.
 */
public class Dynamicquestinventory extends GUIDynamic {

    /**
     * The quest inventory.
     */
    Map<ItemSerialisable, Integer> inventory = new HashMap<>();

    /**
     * The quest product.
     */
    Quest quest;

    /**
     * The required inventory.
     */
    Map<ItemSerialisable, Integer> requiredInventory;

    /**
     * Creates a dynamic GUI showing the quest inventory.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquestinventory(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        // quest we are setting the inventory of
        this.quest = (Quest) this.director.getCurrentInstance(Quest.class);
    }

    @Override
    protected void executeCustom() {
        // retrieve the items
        this.inventory = QuestRegistry.getInstance().getInventory(quest);
        this.requiredInventory = this.quest.getRequiredInventory();

        // sort the inventory ascending item amounts
        this.sortInventory();

        // set the GUI style
        GUIFrame frame = this.gui.getFrame();
        frame.setSize(54);
        frame.setTitle("Quest Inventory/Stock");

        // create a back button
        this.createBackButton(frame);

        // create restock button
        this.createRestockButton(frame);

        // create prev button
        new GUISlot(gui, 53)
            .setItem(Material.GRAY_STAINED_GLASS_PANE)
            .setLabel("Prev");

        // create next button
        new GUISlot(gui, 54)
            .setItem(Material.GRAY_STAINED_GLASS_PANE)
            .setLabel("Next");

        // create inventory of required (and out of stock) and stocked
        Map<ItemSerialisable, Integer> predictiveInventory = PluginUtils.getPredictiveInventory(quest, this.inventory);

        // create slot for each inventory material
        for (var entry : predictiveInventory.entrySet()) {
            if (createInventorySlot(entry)) {
                break; // exit early if indicated to
            }
        }
    }

    private void createBackButton(GUIFrame frame) {
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .onClick(() -> {
                new UpdateScreen(
                    Arrays.asList(this.previousScreen),
                    director
                ).execute();
            });
    }

    private void createRestockButton(GUIFrame frame) {
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
                            // if no item in the slot, or
                            // if the item is the submit button (the button is the same text)
                            Component displayNameComponent = item.getItemMeta().displayName();
                            if (
                                item == null || 
                                displayNameComponent != null && PlainTextComponentSerializer.plainText().serialize(displayNameComponent).equals(displayName)
                            ) {
                                continue;
                            }

                            ItemSerialisable itemSerialisable = ItemSerialisable.fromItemStack(item);
                            Integer itemCount = item.getAmount();

                            // update inventory item
                            QuestRegistry.getInstance().updateInventoryItem(quest, Map.of(itemSerialisable, itemCount));
                        }

                        // go back
                        gui.clearSlots(); // blank the inner screen
                        this.execute(); // re-populate the GUI with the main screen
                    });

                // show this restock inner screen
                gui.getResult().draw();
            });
    }

    private boolean createInventorySlot(Entry<ItemSerialisable, Integer> entry) {
        Integer slot = gui.getEmptySlot();

        if (slot == 45) {
            return true; // exit out early
        }

        ItemSerialisable itemSerialisable = entry.getKey();
        Integer predictedAmount = entry.getValue();
        Integer realAmount = Optional.ofNullable(QuestRegistry.getInstance().getInventory(quest).get(itemSerialisable)).orElse(0);
        
        // Create slot label
        Builder label = Component.text()
            .append(Component.text(realAmount.toString() + "x"))
            .appendSpace()
            .append(Component.text(itemSerialisable.getProperties().getOrDefault("nametag", itemSerialisable.getName())))
            .appendSpace()
            .append(Component.text("("));
        if (realAmount == 0) {
            label
                .append(Component.text("Out of Stock").color(NamedTextColor.RED));
        } else if (predictedAmount >= 0) {
            label
                .append(Component.text("In Stock"));
        } else {
            label
                .append(Component.text("Not Enough Stock").color(NamedTextColor.YELLOW));
        }
        label.append(Component.text(")"));

        new GUISlot(gui, gui.getEmptySlot())
            .setItem(itemSerialisable)
            .setLabel(label.asComponent())
            .setDescription(List.of(itemSerialisable.getName()))
            .setGlinting(predictedAmount <= 0);

        return false; // continue
    }

    private void sortInventory() {
        // convert to sortable form
        List<Entry<ItemSerialisable, Integer>> list = new ArrayList<>(this.inventory.entrySet());

        // sort by amount
        list.sort(Entry.comparingByValue());

        // resubmit as linked list
        Map<ItemSerialisable, Integer> sortedInventory = new LinkedHashMap<>();
        for (Entry<ItemSerialisable, Integer> entry : list) {
            sortedInventory.put(entry.getKey(), entry.getValue());
        }

        this.inventory = sortedInventory;
    }
}
