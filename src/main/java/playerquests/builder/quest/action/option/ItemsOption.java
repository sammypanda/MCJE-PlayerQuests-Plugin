package playerquests.builder.quest.action.option;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicitemslist;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

public class ItemsOption extends ActionOption {

    /**
     * The list of items specified.
     */
    @JsonProperty("items")
    private Map<Material, Integer> items = new HashMap<>();

    /**
     * Default constructor for Jackson.
     */
    public ItemsOption() {}

    /**
     * Constructor including the QuestAction.
     * @param actionData the parent action
     */
    public ItemsOption(ActionData actionData) {
        super(actionData);
    }

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        List<String> items = this.items.entrySet().stream().map(entry -> String.format("%s (%d)", entry.getKey().toString(), entry.getValue())).toList();

        return new GUISlot(gui, slot)
            .setLabel(this.getItems().isEmpty() ? "Set items" : String.format("Change the items"))
            .setDescription(items)
            .setItem(Material.CHEST)
            .onClick(() -> {
                director.setCurrentInstance(new ArrayList<>(this.toList(this.getItems()))); // translate out of serialisable
                // long story short (for the above line): 
                // ItemStack has a field that doesn't serialise to JSON nicely as it requires a check to see if the field exists 
                // before accessing, this would require having a custom serialiser for it. Nobody likes making or maintaining those.

                new UpdateScreen(List.of("itemslist"), director).onFinish(f -> {
                    UpdateScreen updateScreen = (UpdateScreen) f;
                    Dynamicitemslist itemsList = (Dynamicitemslist) updateScreen.getDynamicGUI();
                    
                    itemsList.onFinish(_ -> {
                        this.setItems(this.toMap(itemsList.getItems())); // translate to serialisable
                        this.actionData.setOption(this); // set the option
                        screen.refresh();
                        director.removeCurrentInstance(ArrayList.class); // tidy up
                    });
                }).execute();
            });
    }
    
    /**
     * Set the items in totality.
     * @param items the items for this option
     */
    public void setItems(Map<Material, Integer> items) {
        this.items = items;
    }

    /**
     * Get all the item.
     * @return the items in the option
     */
    public Map<Material, Integer> getItems() {
        return this.items;
    }

    /**
     * Add one item to the list.
     * @param item the item to add to the option
     * @return the items in the option
     */
    public Map<Material, Integer> addItems(Material material, int amount) {
        this.items.put(material, amount);
        return items;
    }

    /**
     * Remove one item from the list.
     * @param item the item to remove from the option
     * @return the items in the option
     */
    public Map<Material, Integer> deleteItems(Material material) {
        this.items.remove(material);
        return items;
    }

    /**
     * Translate to serialisable items map.
     * @param items
     * @return items in map form
     */
    private Map<Material, Integer> toMap(List<ItemStack> items) {
        return items.stream().collect(Collectors.toMap(ItemStack::getType, ItemStack::getAmount));
    }

    /**
     * Translate out of serialisable items map.
     * @param items
     * @return items in list form
     */
    private List<ItemStack> toList(Map<Material, Integer> items) {
        return items.entrySet().stream().map(entry -> new ItemStack(entry.getKey(), entry.getValue())).toList();
    }

    @Override
    public boolean isValid() {
        return !this.getItems().isEmpty();
    }
}
