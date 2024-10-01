package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.SelectMaterial;
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
    List<ItemStack> items = new ArrayList<>();

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
        // if list is in director, pull it in
        ArrayList<?> aList = (ArrayList<?>) this.director.getCurrentInstance(ArrayList.class);

        // if there is a list..
        // filter out any non-item and submit it as the items list
        if (aList != null) {
            this.setItems(aList.stream()
                .filter(ItemStack.class::isInstance) // just a list of items
                .map(ItemStack.class::cast)
                .collect(Collectors.toList()));
        }
    }

    @Override
    protected void execute_custom() {
        Integer listSize = this.items.size(); // the amount of items in the list is often checked
        gui.getFrame().setTitle("Item List"); // set the GUI title

        // create back button
        new GUISlot(gui, 1)
            .setItem("OAK_DOOR")
            .setLabel("Back")
            .onClick(() -> {
                // clear the item list from the director
                this.director.removeCurrentInstance(ArrayList.class);

                // run finish code
                this.finish();

                // go to the previous screen
                new UpdateScreen(
                    Arrays.asList(this.previousScreen), 
                    director
                ).execute();
            });

        // create button for adding items
        if (!listSize.equals(maxItems)) { // as long as the item list isn't the max size yet
            new GUISlot(gui, (this.items.size() + 2))
                .setItem("LIME_DYE")
                .setLabel("Add an item")
                .onClick(() -> {
                    new SelectMaterial(
                        Arrays.asList(
                            "Select or type a block", // the prompt message
                            List.of(), // denied block strings (empty)
                            List.of(), // denied SelectMethods (empty)
                            false // doesn't have to be a block
                        ), 
                        director
                    ).onFinish((func) -> {
                        SelectMaterial function = (SelectMaterial) func;
                        Material result = function.getResult();

                        // re-open the gui
                        this.gui.getResult().display();

                        // if no result just exit
                        if (result == null) {
                            return;
                        }

                        // add the selected item to the list
                        this.addItem(new ItemStack(result));

                        // show it added
                        this.execute();
                    }).execute(); // run the function
                });
        }

        // show the items
        this.generateSlots(listSize);
    }
   
    private void generateSlots(Integer listSize) {
        IntStream.range(0, this.maxItems).anyMatch(index -> {
            // don't continue if no item
            if (listSize <= index) {
                return true; // as to mean 'match found, can exit now'
            }

            ItemStack item = items.get(index);
            Integer itemCount = item.getAmount();
            String itemName = item.getType().toString(); 
            // ^ this could do with being a localised 
            // string (but afaik spigot doesn't have any way to 
            // show the client-side localised values; aka another 
            // simple thing that has to be over-complicated).

            new GUISlot(gui, (index + 2))
                .setItem(item.getType())
                .setLabel(itemName)
                .setDescription(
                    String.format("%s", 
                        itemCount.equals(1) ? "Press me to set quantity" : "Amount: " + itemCount.toString()
                    )
                )
                .onClick(() -> {
                    this.director.setCurrentInstance(item); // set the item for consumption by the itemeditor
                    this.director.setCurrentInstance(items); // set this items list for the next time we want to see them in a fresh item list

                    // show the item editor
                    new UpdateScreen(
                        Arrays.asList("itemeditor"), director
                    ).onFinish(func -> {
                        UpdateScreen function = (UpdateScreen) func;
                        Dynamicitemeditor editor = (Dynamicitemeditor) function.getDynamicGUI();

                        // delete
                        editor.onRemove((i) -> {
                            // remove the item from our list
                            this.removeItem(i);

                            // go back
                            this.goBack(function);
                        });

                        editor.onFinish((v) -> {
                            // go back
                            this.goBack(function);
                        });
                    }).execute();
                });

            return false; // continue
        });
    }

    /**
     * Go back after item editor (should be to this itemlist screen).
     * @param function the function gone back from.
     */
    private void goBack(UpdateScreen function) {
        new UpdateScreen(
            new ArrayList<>(Arrays.asList(function.getPreviousScreen())), director
        ).onFinish((f) -> {
            // find the new/next instance
            UpdateScreen nextfunction = (UpdateScreen) f;
            Dynamicitemslist list = (Dynamicitemslist) nextfunction.getDynamicGUI();

            // migrate the onFinish code to the new instance
            list.onFinish(this.onFinish);
        }).execute();
        // ^ this works (despite being a completely fresh instance of the gui), because at the top
        // of this class, it pulls the list of items from the director. For safety it filters
        // out anything that isn't an ItemStack and then uses whatever is there as the item list.
        // This is issue prone, as it's not strictly the same data but it would mean data overflow
        // issues if it wasn't anyway.
    }

    /**
     * Completely overwrite the item list shown.
     * @param items the items (up to maxItems)
     */
    private void setItems(List<ItemStack> items) {
        this.items = items;
    }

    /**
     * Try to add another item to the list.
     * @param item the item to add
     */
    private void addItem(ItemStack item) {
        this.items.add(item);
    }

    /**
     * Remove an item from the list of items.
     * @param item the item to remove
     */
    private void removeItem(ItemStack item) {
        this.items.remove(item);
    }

    /**
     * Get items list
     * @return a list of ItemStack objects
     */
    public List<ItemStack> getItems() {
        return this.items;
    }
}
