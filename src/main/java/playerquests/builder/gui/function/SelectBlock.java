package playerquests.builder.gui.function;

import java.util.ArrayList; // array type of list
import java.util.List;

import org.bukkit.Bukkit; // getting the plugin manager
import org.bukkit.ChatColor;
import org.bukkit.Material; // the resulting material (block)
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler; // registering methods as event handlers
import org.bukkit.event.HandlerList; // unregistering event handlers
import org.bukkit.event.Listener; // listening to in-game events
import org.bukkit.event.inventory.InventoryClickEvent; // for detecting block selections in listener
import org.bukkit.event.player.PlayerInteractEvent; // for detecting block hits in listener

import playerquests.Core; // accessing singletons
import playerquests.builder.gui.component.GUISlot; // GUI button
import playerquests.client.ClientDirector; // controls the plugin
import playerquests.utility.ChatUtils; // send error to player if block is invalid
import playerquests.utility.PluginUtils;

/**
 * Function for the user to select a block.
 */
public class SelectBlock extends GUIFunction {

    private class SelectBlockListener implements Listener {
        /**
         * The SelectBlock instance.
         */
        private SelectBlock parentClass;

        /**
         * Creates a new listener for chat prompt inputs.
         * @param parent the origin ChatPrompt GUI function
         */
        public SelectBlockListener(SelectBlock parent) {
            this.parentClass = parent;
        }

        @EventHandler
        private void onHit(PlayerInteractEvent event) {
            event.setCancelled(true);
            this.parentClass.setResponse(event.getClickedBlock().getType());
            this.parentClass.execute();
        }

        @EventHandler
        private void onSelect(InventoryClickEvent event) {
            event.setCancelled(true);
            this.parentClass.setResponse(event.getCurrentItem().getType());
            this.parentClass.execute();
        }
    }

    /**
     * Detecting which block the user selects.
     */
    private Listener blockListener;

    /**
     * The resulting block selected.
     */
    private Material result;

    /**
     * The player selecting the block.
     */
    private HumanEntity player;

    /**
     * The starting prompt to give the user.
     */
    private String prompt;

    /**
     * If the function has been set up.
     */
    private boolean wasSetUp;

    /**
     * The blocks to blacklist.
     */
    private List blacklist;

    /** 
     * Provides input as a user selected block.
     * <ul>
     * <li>By hitting the physical block
     * <li>By selecting the block in an inventory
     * </ul>
     * @param params 1. the prompt to show the user 2. list of blacklisted blocks
     * @param director to set values
     * @param slot slot this function belongs to
     */
    public SelectBlock(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    /**
     * Creating and validating values for the chat prompt.
     * <ul>
     * <li>Validates passed in params.
     * <li>Sets the prompt and key as class values.
     * <li>Creates and registers an instance of a chat event listener.
     * <li>Minimises the GUI (closing without disposing).
     * <li>Marks the prompt as successfully setup.
     * <li>Re-runs {@link #execute()}.
     * </ul>
     */
    private void setUp() {
        try {
            PluginUtils.validateParams(this.params, String.class, List.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.sendError(this.player, e.getMessage());
        }

        // set params
        this.prompt = (String) params.get(0);
        this.blacklist = (List) params.get(1);

        // get and set the player who is selecting the block
        this.player = this.director.getPlayer();

        // temporarily close the existing GUI but don't dispose
        this.director.getGUI().getResult().minimise();

        // register events and listener
        this.blockListener = new SelectBlockListener(this);
        Bukkit.getPluginManager().registerEvents(this.blockListener, Core.getPlugin());

        // mark this function class as setup
        this.wasSetUp = true;

        // loop back after setting up
        this.execute();
    }

    @Override
    public void execute() {
        // clear the chat
        ChatUtils.clearChat();

        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        if (this.result == null) {
            this.player.sendMessage(
                ChatColor.UNDERLINE + this.prompt + ChatColor.RESET
            );
            ChatUtils.clearChat(this.player, 1);
            this.player.sendMessage(
                ChatColor.RED + "or type " + ChatColor.GRAY + "exit" + ChatColor.RESET + " [unimplemented]"
            );
            return;
        }

        this.player.sendMessage(
            ChatColor.GRAY + "" + ChatColor.ITALIC + "Selected: " + result.toString()
        );

        this.exit(); // finish
    }

    /**
     * Try to set a material as the NPC block.
     * @param type the material to use as the NPC block
     */
    public void setResponse(Material type) {
        // TODO: add block blacklist (and add air to it by default)

        if (!type.isBlock()) {
            ChatUtils.sendError(this.player, "Could not set this item as an NPC block.");
            this.result = null;
            return; // keep trying
        }

        this.result = type; // set the block the user selected
    }

    /**
     * Gets the block the user selected.
     * @return a block material
     */
    public Material getResult() {
        return this.result;
    }

    /**
     * Cleaning and finishing the function.
     */
    private void exit() {
        HandlerList.unregisterAll(this.blockListener); // remove listeners
        this.finished(); // execute onFinish code
        this.slot.executeNext(this.player); // continue to next slot function
    }
}
