package playerquests.client.gui;

import java.io.IOException; // thrown when files invalid/not found

import org.bukkit.entity.HumanEntity; // managing the player

import playerquests.builder.gui.GUIBuilder; // used to mutate GUIs
import playerquests.client.ClientDirector; // abstracts common controls of the plugin
import playerquests.utility.ChatUtils; // standard in-chat error messages

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
        GUIBuilder guiBuilder = this.director.newGUI(); // create a fresh GUIBuilder/GUI

        try { // try to load the file
            guiBuilder.load("main"); // set the GUI using the main.json template
            guiBuilder.getResult().open(); // open the GUI on the player screen
        } catch (IOException e) {
            ChatUtils.sendError(this.player, e.getMessage()); // send error to player
        }
    }
}