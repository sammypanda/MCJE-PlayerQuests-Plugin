package playerquests.builder.gui.function;

import java.util.List; // used for creating deniedBlocks list
import java.util.stream.Collectors; // transforming stream to data type

import org.bukkit.Bukkit; // getting the plugin manager
import org.bukkit.ChatColor;
import org.bukkit.Material; // the resulting material (block)
import org.bukkit.block.Block; // spigot block type
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; // registering methods as event handlers
import org.bukkit.event.HandlerList; // unregistering event handlers
import org.bukkit.event.Listener; // listening to in-game events
import org.bukkit.event.inventory.InventoryClickEvent; // for detecting block selections in listener
import org.bukkit.event.player.AsyncPlayerChatEvent; // handling request to exit
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent; // for detecting block hits in listener

import playerquests.Core; // accessing singletons
import playerquests.builder.gui.function.data.SelectMethod; // defining which methods to select something
import playerquests.client.ClientDirector; // controls the plugin
import playerquests.utility.ChatUtils; // send error to player if block is invalid
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.PluginUtils; // used to validate function params 

/**
 * Function for the user to select a block.
 */
public class SelectMaterial extends GUIFunction {

    private class SelectMaterialListener implements Listener {
        /**
         * The {@code SelectMaterial} instance.
         */
        private SelectMaterial parentClass;

        /**
         * The player this listener is for.
         */
        private Player player;

        /**
         * Select methods to not allow.
         */
        private List<SelectMethod> deniedMethods;

        /**
         * Constructs a new {@code SelectMaterialListener}.
         *
         * @param parent the parent {@code SelectMaterial} instance
         * @param player the player associated with this listener
         */
        public SelectMaterialListener(SelectMaterial parent, Player player) {
            this.parentClass = parent;
            this.player = player;
            this.deniedMethods = parent.getDeniedMethods();
        }
        
        /**
         * Handles player interactions with blocks.
         *
         * @param event the {@code PlayerInteractEvent} triggered when a player interacts with a block
         */
        @EventHandler
        private void onHit(PlayerInteractEvent event) {
            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }
            
            if (deniedMethods.contains(SelectMethod.HIT) && deniedMethods.contains(SelectMethod.PAT)) {
                return; // do not continue
            }

            event.setCancelled(true);

            Block clickedBlock = event.getClickedBlock();

            if (clickedBlock != null) {
                this.parentClass.setResponse(clickedBlock.getType());
            }
        }

        /**
         * Handles inventory clicks to select materials.
         *
         * @param event the {@code InventoryClickEvent} triggered when a player clicks in an inventory
         */
        @EventHandler
        private void onSelect(InventoryClickEvent event) {
            if (this.player != event.getWhoClicked()) {
                return; // do not capture other players events
            }

            if (deniedMethods.contains(SelectMethod.SELECT)) {
                return; // do not continue
            }

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                    return; // Ignore if the clicked item is null or air
                }
                
                event.getView().close();
                parentClass.setResponse(event.getCurrentItem().getType());
            });
        }

        /**
         * Handles player command inputs to exit the selection.
         *
         * @param event the {@code PlayerCommandPreprocessEvent} triggered when a player issues a command
         */
        @EventHandler
        private void onCommand(PlayerCommandPreprocessEvent event) {
            // do not capture other players events
            if (this.player != event.getPlayer()) {
                return;
            }

            // exit SelectMaterial
            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                this.parentClass.setCancelled(true);
                this.parentClass.execute(); // run with cancellation
            });
        }

        /**
         * Handles player chat inputs for selecting blocks or exiting.
         *
         * @param event the {@code AsyncPlayerChatEvent} triggered when a player sends a chat message
         */
        @EventHandler
        private void onChat(AsyncPlayerChatEvent event) {
            // if the event is coming from a different player
            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                String message = event.getMessage();

                // if wanting to exit (or trying to do another command)
                if (ChatUtils.isExitKeyword(message)) {
                    this.parentClass.setCancelled(true);
                    this.parentClass.execute(); // run with cancellation
                }

                // if selecting block with chat is not allowed
                if (deniedMethods.contains(SelectMethod.CHAT)) {
                    return; // do not continue
                }

                // if trying to set a block using the chat box
                if (!deniedMethods.contains(SelectMethod.CHAT)) { // if CHAT mode enabled
                    this.parentClass.setResponse(message); // set
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
    private Player player;

    /**
     * The starting prompt to give the user.
     */
    private String prompt;

    /**
     * If the function has been set up.
     */
    private boolean wasSetUp;

    /**
     * The blocks to deniedBlocks.
     */
    private List<Material> deniedBlocks;

    /**
     * The methods of selecting blocks to deny.
     */
    private List<SelectMethod> deniedMethods; 

    /**
     * If the player has cancelled the selection.
     */
    private boolean cancelled;

    /**
     * If the material must be a block.
     */
    private Boolean blocksOnly;

    /** 
     * Provides input as a user selected block.
     * <ul>
     * <li>By hitting the physical block
     * <li>By selecting the block in an inventory
     * </ul>
     * @param params 1. the prompt to show the user 2. list of denied blocks 3. list of denied methods
     * @param director to set values
     */
    public SelectMaterial(List<Object> params, ClientDirector director) {
        super(params, director);
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
            PluginUtils.validateParams(this.params, String.class, List.class, List.class, Boolean.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.message(e.getMessage())
                .player(this.player)
                .type(MessageType.ERROR)
                .send();
        }

        // set params
        this.prompt = (String) params.get(0);
        this.deniedBlocks = castDeniedBlocks(params.get(1));
        this.deniedMethods = castDeniedMethods(params.get(2));
        this.blocksOnly = (Boolean) params.get(3);

        // add basics to deniedBlocks
        this.deniedBlocks.add(Material.AIR);

        // get and set the player who is selecting the block
        this.player = this.director.getPlayer();

        // temporarily close the existing GUI but don't dispose
        this.director.getGUI().getResult().minimise();

        // register events and listener
        this.blockListener = new SelectMaterialListener(this, Bukkit.getPlayer(this.player.getUniqueId()));
        Bukkit.getPluginManager().registerEvents(this.blockListener, Core.getPlugin());

        // mark this function class as setup
        this.wasSetUp = true;

        // loop back after setting up
        this.execute();
    }

    /**
     * Cast deniedBlocks object to a list of denied block material strings.
     * @param object object of materials to deny
     * @return list of materials to deny
     */
    private List<Material> castDeniedBlocks(Object object) {
        List<?> castedList = (List<?>) object; // wildcard generics for cast checking

        // due to java missing reified generics, stream loop to safely validate wildcarded list elements
        // return list of deniedBlocksed strings
        return (List<Material>) castedList.stream()
            .filter(item -> item instanceof String) // filter out non-String items
            .map(itemString -> { // transform each item to String
                Material material = Material.matchMaterial((String) itemString);
                if (material == null) { // if material string couldn't be matched
                ChatUtils.message(String.format("Invalid item in deniedBlocks: %s", itemString))
                    .player(this.player)
                    .type(MessageType.ERROR)
                    .send();
                }
                return material;
            }) 
            .collect(Collectors.toList()); // collect into final denylist
    }

    /**
     * Gets the blocks that cannot be set as an NPC.
     * @return a list of materials
     */
    public List<Material> getDeniedBlocks() {
        return this.deniedBlocks;
    }

    /**
     * Check and cast deniedMethods to a list of denied method ENUMs
     * @param object object of methods to deny
     * @return list of methods to deny
     */
    private List<SelectMethod> castDeniedMethods(Object object) {
        List<?> castedList = (List<?>) object; // wildcard generics for cast checking

        return (List<SelectMethod>) castedList.stream()
            .filter(method -> method instanceof SelectMethod) // filter out non-method items
            .map(method -> (SelectMethod) method) // cast safely
            .collect(Collectors.toList()); // collect into final denylist
    }

    /**
     * Gets the select methods that have been denied.
     * @return a list of select method enums
     */
    public List<SelectMethod> getDeniedMethods() {
        return this.deniedMethods;
    }

    @Override
    public void execute() {
        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        // clear the chat
        ChatUtils.clearChat(this.player);

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
     * Sets the selected block material.
     * This method validates the material and ensures it is not in the denied blocks list and is a block type.
     *
     * @param material the {@code Material} to set as the selected block
     */
    public void setResponse(Material material) {
        if (this.deniedBlocks.contains(material)) {
            ChatUtils.message("This item is denied from being set as an NPC block.")
                .player(this.player)
                .type(MessageType.WARN)
                .send();
            this.result = null;
            return;
        }

        if (this.blocksOnly && !material.isBlock()) {
            ChatUtils.message("Could not set this item as an NPC block.")
                .player(this.player)
                .type(MessageType.WARN)
                .send();
            this.result = null;
            return; // keep trying
        }

        this.result = material; // set the block the user selected
        this.execute();
    }

    /**
     * Sets the selected block material by name.
     * This method converts the material name to a {@code Material} and validates it.
     *
     * @param material the name of the material to set as the selected block
     */
    public void setResponse(String material) {
        result = Material.matchMaterial(material);

        if (result == null) {
            ChatUtils.message(String.format("Could not find %s block to set, try to be more specific.", material))
                .player(this.player)
                .type(MessageType.WARN)
                .send();
        } else {
            setResponse(result); // set the block the user selected
        }
    }

    /**
     * Returns the material of the block selected by the user.
     *
     * @return the selected block material
     */
    public Material getResult() {
        return this.result;
    }

    /**
     * Cleans up and finishes the block selection function.
     */
    private void exit() {
        HandlerList.unregisterAll(this.blockListener); // remove private handlers
        this.finished(); // execute onFinish code
    }

    /**
     * Marks the block selection function as cancelled.
     *
     * @param cancelled {@code true} to cancel the selection, {@code false} otherwise
     */
    private void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }
}
