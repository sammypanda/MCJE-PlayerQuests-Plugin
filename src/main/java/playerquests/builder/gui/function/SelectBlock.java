package playerquests.builder.gui.function;

import java.util.ArrayList; // array type of list
import java.util.List; // used for creating denylist list
import java.util.stream.Collectors; // transforming stream to data type

import org.bukkit.Bukkit; // getting the plugin manager
import org.bukkit.ChatColor;
import org.bukkit.Material; // the resulting material (block)
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler; // registering methods as event handlers
import org.bukkit.event.HandlerList; // unregistering event handlers
import org.bukkit.event.Listener; // listening to in-game events
import org.bukkit.event.inventory.InventoryClickEvent; // for detecting block selections in listener
import org.bukkit.event.player.AsyncPlayerChatEvent; // handling request to exit
import org.bukkit.event.player.PlayerInteractEvent; // for detecting block hits in listener

import playerquests.Core; // accessing singletons
import playerquests.builder.gui.component.GUISlot; // GUI button
import playerquests.client.ClientDirector; // controls the plugin
import playerquests.utility.ChatUtils; // send error to player if block is invalid
import playerquests.utility.PluginUtils; // used to validate function params 

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
        }

        @EventHandler
        private void onSelect(InventoryClickEvent event) {
            event.setCancelled(true);
            this.parentClass.setResponse(event.getCurrentItem().getType());
        }

        @EventHandler
        private void onChat(AsyncPlayerChatEvent event) {
            event.setCancelled(true);

            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on main thread, instead of async
                if (event.getMessage().toLowerCase().equals("exit")) { // if wanting to exit
                    this.parentClass.setCancelled(true);
                } else { // if trying to set a block using the chat box
                    this.parentClass.setResponse(event.getMessage());
                }
            });
            
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
     * The blocks to denylist.
     */
    private List<Material> denylist;

    /**
     * If the player has cancelled the selection.
     */
    private boolean cancelled;

    /** 
     * Provides input as a user selected block.
     * <ul>
     * <li>By hitting the physical block
     * <li>By selecting the block in an inventory
     * </ul>
     * @param params 1. the prompt to show the user 2. list of denylisted blocks
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
        this.denylist = getDenylist(params.get(1));

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

    /**
     * Cast denylist object to a list of denylisted material strings.
     * @param object object of materials to denylist
     * @return list of materials to denylist
     */
    private List<Material> getDenylist(Object object) {
        List<?> castedList = (List<?>) object; // wildcard generics for cast checking

        // due to java missing reified generics, stream loop to safely validate wildcarded list elements
        // return list of denylisted strings
        return (List<Material>) castedList.stream()
            .filter(item -> item instanceof String) // filter out non-String items
            .map(itemString -> { // transform each item to String
                Material material = Material.matchMaterial((String) itemString);
                if (material == null) { // if material string couldn't be matched
                    ChatUtils.sendError(this.player, String.format("Invalid item in denylist: %s", itemString));
                }
                return material;
            }) 
            .collect(Collectors.toList()); // collect into final denylist
    }

    @Override
    public void execute() {
        // clear the chat
        ChatUtils.clearChat();

        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        if (this.cancelled) {
            this.player.sendMessage(
                ChatColor.GRAY + "" + ChatColor.ITALIC + "exited" + ChatColor.RESET
            );
            this.exit();
            return;
        }

        if (this.result == null) {
            this.player.sendMessage(
                ChatColor.UNDERLINE + this.prompt + ChatColor.RESET
            );
            ChatUtils.clearChat(this.player, 1);
            this.player.sendMessage(
                ChatColor.RED + "or type " + ChatColor.GRAY + "exit" + ChatColor.RESET
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
    public void setResponse(Material material) {
        if (this.denylist.contains(material)) {
            ChatUtils.sendError(this.player, "This item is on the denylist from being set as an NPC block.");
            this.result = null;
            return;
        }

        if (!material.isBlock()) {
            ChatUtils.sendError(this.player, "Could not set this item as an NPC block.");
            this.result = null;
            return; // keep trying
        }

        this.result = material; // set the block the user selected
        this.execute();
    }

    /**
     * Try to convert a string to material and set as the NPC block.
     * @param type the name of the material to use as the NPC block
     */
    public void setResponse(String material) {
        result = Material.matchMaterial(material);

        if (result == null) {
            ChatUtils.sendError(this.player, String.format("Could not find %s block to set, try to be more specific.", material));
        } else {
            setResponse(result); // set the block the user selected
        }
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
        this.director.getGUI().getResult().open(); // re-open GUI
        this.slot.executeNext(this.player); // continue to next slot function
    }

    /**
     * Setting block selection function as cancelled
     */
    private void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }
}
