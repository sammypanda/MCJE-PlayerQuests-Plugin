package playerquests.builder.gui.function;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block; // data object representing a placed block
import org.bukkit.block.data.BlockData; // data object representing the metadata of a block
import org.bukkit.entity.HumanEntity; // usually the player
import org.bukkit.entity.Player; // refers to the player
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent; // event which captures what block was placed
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import playerquests.Core;
import playerquests.builder.quest.data.LocationData; // quest entity locations
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.PluginUtils;

/**
 * Function for the user to select a block.
 */
public class SelectLocation extends GUIFunction {

    private class SelectLocationListener implements Listener {
        /**
         * The SelectMaterial instance.
         */
        private SelectLocation parentClass;

        /**
         * The player this listener is for.
         */
        private Player player;

        /**
         * Constructs a new listener for block and command events.
         * 
         * @param parent The parent {@link SelectLocation} instance.
         * @param player The player whose actions are being listened to.
         */
        public SelectLocationListener(SelectLocation parent, Player player) {
            this.parentClass = parent;
            this.player = player;
        }

        /**
         * Handles command events to allow exiting the selection process.
         * 
         * @param event The {@link PlayerCommandPreprocessEvent} event.
         */
        @EventHandler
        private void onCommand(PlayerCommandPreprocessEvent event) {
            // do not capture other players events
            if (this.player != event.getPlayer()) {
                return;
            }

            // exit SelectLocation
            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                this.parentClass.exit();
            });
        }

        /**
         * Handles block placement events to set the location and block data.
         * 
         * @param event The {@link BlockPlaceEvent} event.
         */
        @EventHandler
        private void onBlockPlace(BlockPlaceEvent event) {
            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }

            event.setCancelled(true);

            Block blockPlaced = event.getBlockPlaced();
            this.parentClass.setResponse(blockPlaced.getLocation(), blockPlaced.getBlockData());
        }
    }

    /**
     * Text prompt to instruct the user
     */
    private String prompt;

    /**
     * The user selecting the world location
     */
    private HumanEntity player;

    /**
     * If this function has been set up
     */
    private boolean wasSetUp;

    /**
     * The location chosen
     */
    private LocationData location;

    /**
     * Events for determining the location
     */
    private Listener locationListener;

    /**
     * The block chosen
     */
    private BlockData blockData;

    /**
     * Constructs a {@link SelectLocation} instance.
     * <p>
     * The constructor initializes the prompt message and sets up the function
     * to allow the user to select a block and location in the world.
     * </p>
     * 
     * @param params A list of parameters for initializing the function. Expected
     *               to contain a single {@link String} representing the prompt message.
     * @param director The {@link ClientDirector} used to interact with the GUI and player.
     */
    public SelectLocation(ArrayList<Object> params, ClientDirector director) {
        super(params, director);
    }

    /**
     * Initializes and validates values for the function.
     * <p>
     * Sets up the prompt, registers the event listener, and prepares the function
     * for execution.
     * </p>
     */
    private void setUp() {
        try {
            PluginUtils.validateParams(this.params, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.message(e.getMessage())
                .player(this.player)
                .type(MessageType.ERROR)
                .send();
        }

        // set params
        this.prompt = (String) params.get(0);

        // get and set the player who is selecting the block
        this.player = this.director.getPlayer();

        // temporarily close the existing GUI but don't dispose
        this.director.getGUI().getResult().minimise();

        // register events and listener
        this.locationListener = new SelectLocationListener(this, Bukkit.getPlayer(this.player.getUniqueId()));
        Bukkit.getPluginManager().registerEvents(this.locationListener, Core.getPlugin());

        // mark this function class as setup
        this.wasSetUp = true;

        // loop back after setting up
        this.execute();
    }

    @Override
    public void execute() {
        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        // clear the chat
        ChatUtils.clearChat(this.player);

        if (this.location == null) {
            this.player.sendMessage(
                ChatColor.UNDERLINE + this.prompt + ChatColor.RESET
            );
            return;
        }

        this.exit();
    }

    /**
     * Sets the location and block data based on user selection.
     * 
     * @param location The {@link org.bukkit.Location} selected by the user.
     * @param blockData The {@link BlockData} representing the block at the selected location.
     */
    public void setResponse(org.bukkit.Location location, BlockData blockData) {
        // create the location data
        this.location = new LocationData(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getPitch(),
            location.getYaw()
        );

        // add the block represented
        this.blockData = blockData;

        // finish line
        this.execute();
    }

    /**
     * Retrieves the location chosen by the user.
     * 
     * @return The {@link LocationData} object representing the selected location.
     */
    public LocationData getResult() {
        return this.location;
    }

    /**
     * Retrieves the block data of the block used to select the location.
     * 
     * @return The {@link BlockData} of the selected block.
     */
    public BlockData getBlockData() {
        if (this.blockData == null) {
            System.err.println("The block was requested from LocationData, without a block having been set.");
            
        }
        
        return this.blockData;
    }

    /**
     * Cleans up and finishes the function.
     * <p>
     * Unregisters the event listener and reopens the GUI.
     * </p>
     */
    private void exit() {
        HandlerList.unregisterAll(this.locationListener); // remove listeners
        this.finished(); // execute onFinish code
        this.director.getGUI().getResult().open(); // re-open GUI
    }
    
}
