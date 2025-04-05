package playerquests.builder.quest.action.option;

import java.util.ArrayList;
import java.util.List;

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
    private ArrayList<ItemStack> items = new ArrayList<>();
    // TODO: implement me

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
        List<String> items = this.items.stream().map( item -> String.format("%s (%d)", item.getType().toString(), item.getAmount()) ).toList();

        return new GUISlot(gui, slot)
            .setLabel(this.getItems().isEmpty() ? "Set items" : String.format("Change the items"))
            .setDescription(items)
            .setItem(Material.CHEST)
            .onClick(() -> {
                director.setCurrentInstance(this.getItems());

                new UpdateScreen(List.of("itemslist"), director).onFinish(f -> {
                    UpdateScreen updateScreen = (UpdateScreen) f;
                    Dynamicitemslist itemsList = (Dynamicitemslist) updateScreen.getDynamicGUI();

                    itemsList.onFinish(_ -> {
                        this.setItems(new ArrayList<ItemStack>(itemsList.getItems()));
                        this.actionData.setOption(this); // set the option
                        director.removeCurrentInstance(ArrayList.class); // tidy up
                        screen.refresh();
                    });
                }).execute();
            });
    }
    
    /**
     * Set the items in totality.
     * @param items the items for this option
     */
    public void setItems(ArrayList<ItemStack> items) {
        this.items = items;
    }

    /**
     * Get all the item.
     * @return the items in the option
     */
    public ArrayList<ItemStack> getItems() {
        return this.items;
    }

    /**
     * Add one item to the list.
     * @param item the item to add to the option
     * @return the items in the option
     */
    public ArrayList<ItemStack> addItems(ItemStack item) {
        this.items.add(item);
        return items;
    }

    /**
     * Remove one item from the list.
     * @param item the item to remove from the option
     * @return the items in the option
     */
    public ArrayList<ItemStack> deleteItems(ItemStack item) {
        this.items.remove(item);
        return items;
    }
}
