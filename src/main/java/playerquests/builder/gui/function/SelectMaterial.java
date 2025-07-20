package playerquests.builder.gui.function;

import java.util.List; // used for creating deniedBlocks list

import org.bukkit.Bukkit; // getting the plugin manager
import org.bukkit.Material; // the resulting material (block)
import org.bukkit.block.Block; // spigot block type
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; // registering methods as event handlers
import org.bukkit.event.HandlerList; // unregistering event handlers
import org.bukkit.event.Listener; // listening to in-game events
import org.bukkit.event.inventory.InventoryClickEvent; // for detecting block selections in listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent; // for detecting block hits in listener
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
                this.parentClass.setResponse(new ItemStack(clickedBlock.getType()));
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
                parentClass.setResponse(event.getCurrentItem());
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
        private void onChat(AsyncChatEvent event) {
            // if the event is coming from a different player
            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                String message = PlainTextComponentSerializer.plainText().serialize(event.message());

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
    private ItemStack result;

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
    private boolean wasSetup;

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
    private void setup() {
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
        this.wasSetup = true;

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
            .toList(); // collect into final denylist
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
            .toList(); // collect into final denylist
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
        if (!this.wasSetup) {
            this.setup();
            return;
        }

        // clear the chat
        ChatUtils.clearChat(this.player);

        if (this.cancelled) {
            ChatUtils.message(
                Component.text("exited").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)
            ).player(player).send();
            this.exit();
            return;
        }

        if (this.result == null) {
            ChatUtils.message(Component.empty()
                .append(Component.text(this.prompt).decorate(TextDecoration.UNDERLINED))
                .appendNewline().appendNewline()
                .append(Component.text("or type ").color(NamedTextColor.RED))
                .append(Component.text("exit").color(NamedTextColor.GRAY))
            ).player(player).send();
            return;
        }

        ChatUtils.message(Component.empty()
            .append(Component.text("Selected: " + result.toString()).decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY))
        ).player(player).send();

        this.exit(); // finish
    }

    /**
     * Sets the selected block material.
     * This method validates the material and ensures it is not in the denied blocks list and is a block type.
     *
     * @param material the {@code Material} to set as the selected block
     */
    public void setResponse(ItemStack itemStack) {
        if (this.deniedBlocks.contains(itemStack.getType())) {
            ChatUtils.message("This item is denied from being set as an NPC block.")
                .player(this.player)
                .type(MessageType.WARN)
                .send();
            this.result = null;
            return;
        }

        if (this.blocksOnly && !itemStack.getType().isBlock()) {
            ChatUtils.message("Could not set this item as an NPC block.")
                .player(this.player)
                .type(MessageType.WARN)
                .send();
            this.result = null;
            return; // keep trying
        }

        this.result = itemStack; // set the block the user selected
        this.execute();
    }

    /**
     * Sets the selected block material by name.
     * This method converts the material name to a {@code Material} and validates it.
     *
     * @param material the name of the material to set as the selected block
     */
    public void setResponse(String materialString) {
        Material material = Material.matchMaterial(materialString);

        if (material == null) {
            ChatUtils.message(String.format("Could not find %s block to set, needs to be the exact name.", material))
                .player(this.player)
                .type(MessageType.WARN)
                .send();
        } else {
            setResponse(new ItemStack(material)); // set the block the user selected
        }
    }

    /**
     * Returns the material of the block selected by the user.
     *
     * @return the selected block material
     */
    public ItemStack getResult() {
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
