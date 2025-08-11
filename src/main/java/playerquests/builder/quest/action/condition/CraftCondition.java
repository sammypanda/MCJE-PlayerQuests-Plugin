package playerquests.builder.quest.action.condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import com.fasterxml.jackson.annotation.JsonIgnore;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicitemslist;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.event.ActionCompletionEvent;
import playerquests.utility.serialisable.ItemSerialisable;

public class CraftCondition extends ActionCondition {

    private Map<ItemSerialisable, Integer> items = new HashMap<>();

    @JsonIgnore
    private Map<ItemSerialisable, Integer> remainingItems;

    /**
     * Default constructor for Jackson.
     */
    public CraftCondition() {
        // Nothing here
    }

    public CraftCondition(ActionData actionData) {
        super(actionData);
    }

    public Map<ItemSerialisable, Integer> getItems() {
        return this.items;
    }

    public void setItems(Map<ItemSerialisable, Integer> items) {
        this.items = items;
    }

    @Override
    public boolean isMet(QuesterData questerData) {
        // Create a copy of required items to track what's left to find
        if (remainingItems == null) {
            this.remainingItems = new HashMap<>(this.getItems());
        }

        // If remainingItems is empty, all requirements were met
        return this.remainingItems.isEmpty();
    }

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(this.getDescription())
            .setItem(Material.CRAFTING_TABLE);
    }

    @Override
    public String getName() {
        return "Craft";
    }

    @Override
    public void createEditorGUI(GUIDynamic screen, GUIBuilder gui, ClientDirector director) {
        new GUISlot(gui, 3)
            .setItem(Material.CHEST)
            .setLabel("Select items")
            .setDescription(List.of("Items that are to be crafted ", "before the action continues."))
            .onClick(() -> {
                director.setCurrentInstance(new ArrayList<ItemStack>(
                    this.getItems().entrySet().stream()
                        .map(entry -> {
                            ItemStack itemStack = entry.getKey().toItemStack();
                            itemStack.setAmount(entry.getValue());
                            return itemStack;
                        })
                        .toList()
                ));

                new UpdateScreen(List.of("itemslist"), director).onFinish(f -> {
                    UpdateScreen updateScreen = (UpdateScreen) f;
                    Dynamicitemslist itemsList = (Dynamicitemslist) updateScreen.getDynamicGUI();

                    itemsList.onFinish(portableDynamicGUI -> {
                        Dynamicitemslist portableItemsList = (Dynamicitemslist) portableDynamicGUI;
                        this.setItems(portableItemsList.getItems().stream()
                            .collect(Collectors.toMap(
                                ItemSerialisable::fromItemStack, 
                                ItemStack::getAmount
                            ))
                        ); // translate to serialisable
                        screen.refresh();
                        director.removeCurrentInstance(ArrayList.class); // tidy up
                    });
                }).execute();
            });
    }

    @Override
    public List<String> getDetails() {
        List<String> itemStrings = this.getItems().keySet().stream()    
            .map(ItemSerialisable::toString)
            .toList();
        String actionsString = String.join(", ", itemStrings);

        return List.of(
            "Requires",
            String.format("%s",
                ChatUtils.shortenString(actionsString, 22)
            ),
            "to be crafted"
        );
    }

    @Override
    public List<String> getDescription() {
        return List.of(
            "Waits until an ",
            "item is crafted."
        );
    }

    @Override
    public void startListener(QuesterData questerData) {
        new CraftConditionListener(this, questerData);
    }
    
    class CraftConditionListener extends ActionConditionListener<CraftCondition> {

        public CraftConditionListener(CraftCondition actionCondition, QuesterData questerData) {
            super(actionCondition, questerData);
        }

        @EventHandler
        private void onCraft(CraftItemEvent event) {
            ItemStack item = event.getRecipe().getResult();
            ItemSerialisable itemSerialisable = ItemSerialisable.fromItemStack(item);

            if (remainingItems.containsKey(itemSerialisable)) {
                int remainingAmount = remainingItems.get(itemSerialisable) - item.getAmount();
                if (remainingAmount <= 0) {
                    remainingItems.remove(itemSerialisable);
                } else {
                    remainingItems.put(itemSerialisable, remainingAmount);
                }
                this.trigger();
            }
        }
    }
}
