package playerquests.client.gui;

import java.util.ArrayList; // param for gui functions
import java.util.Arrays; // listing params in gui function 'ArrayList's

import org.bukkit.entity.HumanEntity; // managing the player

import playerquests.builder.gui.function.UpdateScreen; // used to change the GUI to another screen
import playerquests.client.ClientDirector; // abstracts common controls of the plugin

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
        this.director.clearCurrentInstances();
        this.director.newGUI(); // create a fresh GUIBuilder/GUI
        new UpdateScreen(new ArrayList<>(Arrays.asList("main")), director).execute();
    }
}