package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.client.ClientDirector;

/**
 * A dynamic GUI screen for viewing items selected.
 * <p>
 * This screen also allows users to set and remove items. 
 * It provides visualisation of each item and navigation 
 * back to the previous screen.
 * </p>
 */
public class Dynamicitemslist extends GUIDynamic {

    /**
     * The list of items.
     */
    List<ItemStack> items = new ArrayList<ItemStack>();

    /**
     * The maximum number of items.
     */
    Integer maxItems = 8;

    /**
     * Constructs a new {@code Dynamicitemslist} instance.
     * @param director the client director that handles the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicitemslist(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
    }

    @Override
    protected void execute_custom() {
        Integer listSize = this.items.size(); // the amount of items in the list is often checked
        gui.getFrame().setTitle("Item List"); // set the GUI title

        // create back button
        new GUISlot(gui, 1)
            .setItem("OAK_DOOR")
            .setLabel("Back")
            .addFunction(
                new UpdateScreen(new ArrayList<>(
                    Arrays.asList(this.previousScreen)
                ), director)
            );

        // create button for adding items
        if (!listSize.equals(maxItems)) { // as long as the item list isn't the max size yet
            new GUISlot(gui, (this.items.size() + 2))
                .setItem("LIME_DYE")
                .setLabel("Add an item")
                .onClick(() -> {
                    this.items.add(new ItemStack(Material.ACACIA_BOAT)); // add an item to the list TODO: make this get a real item.
                    this.execute(); // update GUI
                });
        }

        this.generateSlots(listSize);
    }
   
    private void generateSlots(Integer listSize) {
        IntStream.range(0, this.maxItems).anyMatch(index -> {
            // don't continue if no item
            if (listSize <= index) {
                return true; // as to mean 'match found, can exit now'
            }

            ItemStack item = items.get(index);

            new GUISlot(gui, (index + 2))
                .setItem("RED_STAINED_GLASS_PANE");

            return false; // continue
        });
    }
}
