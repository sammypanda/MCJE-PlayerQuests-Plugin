package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.ChatPrompt;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageType;

/**
 * A dynamic GUI screen for viewing items selected.
 * <p>
 * This screen also allows users to set and remove items. 
 * It provides visualisation of each item and navigation 
 * back to the previous screen.
 * </p>
 */
public class Dynamicitemeditor extends GUIDynamic {

    /**
     * The item being edited.
     */
    ItemStack item;

    /**
     * Code to run when the item quantity is updated.
     */
    private Consumer<Void> onAmountUpdate;

    /**
     * Constructs a new {@code Dynamicitemeditor} instance.
     * @param director the client director that handles the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicitemeditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        item = (ItemStack) this.director.getCurrentInstance(ItemStack.class);
    }

    @Override
    protected void execute_custom() {
        Integer itemAmount = this.item.getAmount();
        this.gui.getFrame().setTitle("Edit " + item.getType().toString());
        
        // create back button
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .addFunction(
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(this.previousScreen)), director
                )
            );

        // edit quantity/amount/count button
        new GUISlot(gui, 2)
            .setItem(Material.BUCKET)
            .setLabel(String.format(
                "%s", 
                itemAmount.equals(1) ? "Set amount" : "Change amount (" + itemAmount.toString() + ")"
            ))
            .onClick(() -> {
                new ChatPrompt(
                    new ArrayList<>(Arrays.asList("Type a number under 65", "none")), 
                    director
                ).onFinish((func) -> {
                    ChatPrompt function = (ChatPrompt) func;
                    String response = function.getResponse();
                    Integer responseAsInteger;

                    // try to convert to integer
                    try {
                        responseAsInteger = Integer.parseInt(response);
                    } catch (NumberFormatException e) {
                        responseAsInteger = null;
                    }

                    // check if is valid
                    Player player = Bukkit.getPlayer(director.getPlayer().getUniqueId());
                    MessageBuilder err = ChatUtils.message("").player(player).type(MessageType.WARN);

                    if (responseAsInteger == null) {
                        err.content("That might've been an invalid number.").send();
                        return;
                    }
                    
                    if (responseAsInteger > 64) {
                        err.content("Make sure the amount is under 65.").send();
                        return;
                    }

                    // set the amount
                    this.item.setAmount(responseAsInteger);
                    this.execute(); // refresh gui

                    // run consumable
                    this.onAmountUpdate.accept(null);;
                }).execute(); // run chat prompt function
            });
    }

    /**
     * Retrieves the current {@link ItemStack} being edited.
     * 
     * <p>This method returns the {@link ItemStack} instance that represents the item currently
     * being processed or manipulated. It provides access to the state of the item as stored
     * in the {@link #item} field.</p>
     * 
     * @return The {@link ItemStack} that is currently being edited.
     */
    public ItemStack getResult() {
        return this.item;
    }

    /**
     * Sets the code to run when a the ItemStack amount is updated.
     * 
     * @param onAmountUpdate a {@link Consumer} that runs when the item quantity is updated.
     */
    public void onAmountUpdate(Consumer<Void> onAmountUpdate) {
        this.onAmountUpdate = onAmountUpdate;
    }
}
