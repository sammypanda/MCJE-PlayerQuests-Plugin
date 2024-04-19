package playerquests.builder.gui.function;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import playerquests.Core;
import playerquests.builder.quest.data.LocationData; // quest entity locations
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.PluginUtils;

/**
 * Function for the user to select a block.
 */
public class SelectLocation extends GUIFunction {

    private class SelectLocationListener implements Listener {
        /**
         * The SelectBlock instance.
         */
        private SelectLocation parentClass;

        /**
         * Creates a new listener for chat prompt inputs.
         * @param parent the origin ChatPrompt GUI function
         */
        public SelectLocationListener(SelectLocation parent) {
            this.parentClass = parent;
        }

        @EventHandler
        private void onBlockPlace(BlockPlaceEvent event) {
            event.setCancelled(true);
            this.parentClass.setResponse(event.getBlockPlaced().getLocation());
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
     * Provides input as a user selected world location.
     * <ul>
     * <li>By hitting the physical block
     * <li>By selecting the block in an inventory
     * </ul>
     * @param params 1. prompt
     * @param director to set values
     */
    public SelectLocation(ArrayList<Object> params, ClientDirector director) {
        super(params, director);
    }

    /**
     * Creating and validating values for the function.
     */
    private void setUp() {
        try {
            PluginUtils.validateParams(this.params, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.sendError(this.player, e.getMessage());
        }

        // set params
        this.prompt = (String) params.get(0);

        // get and set the player who is selecting the block
        this.player = this.director.getPlayer();

        // temporarily close the existing GUI but don't dispose
        this.director.getGUI().getResult().minimise();

        // register events and listener
        this.locationListener = new SelectLocationListener(this);
        Bukkit.getPluginManager().registerEvents(this.locationListener, Core.getPlugin());

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

        if (this.location == null) {
            this.player.sendMessage(
                ChatColor.UNDERLINE + this.prompt + ChatColor.RESET
            );
            return;
        }

        this.exit();
    }

    /**
     * Setting the location the user decides as PlayerQuests Location object.
     * @param location Bukkit world location the user selected
     */
    public void setResponse(org.bukkit.Location location) {
        this.location = new LocationData(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getPitch(),
            location.getYaw()
        );

        this.execute();
    }

    /**
     * Gets the location the user selected.
     * @return a location object
     */
    public LocationData getResult() {
        return this.location;
    }

    /**
     * Cleaning and finishing the function.
     */
    private void exit() {
        HandlerList.unregisterAll(this.locationListener); // remove listeners
        this.finished(); // execute onFinish code
        this.director.getGUI().getResult().open(); // re-open GUI
    }
    
}
