package playerquests.client.gui;

import org.bukkit.entity.HumanEntity; // managing the player

import playerquests.client.ClientDirector;

/**
 * Controlling the plugin via a GUI.
 */
public class GUIClient {

    /**
     * The player which controls the plugin via the GUIs.
     */
    HumanEntity player;

    /**
     * For executing plugin actions/behaviours.
     */
    ClientDirector director;
    
    /**
     * Create a new instance of GUIClient to control the plugin over a GUI.
     * @param player the player to control the gui/plugin.
     */
    public GUIClient(HumanEntity player) {
        this.player = player; // set the player
        this.director = new ClientDirector(player); // set the director for this client session
    }

    /**
     * Open the main screen.
     */
    public void open() {
        // TODO: implement opening the GUIClient (main GUI)
        throw new UnsupportedOperationException("GUIClient.open() not implemented");
    }
}