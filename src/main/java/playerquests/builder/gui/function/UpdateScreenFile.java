package playerquests.builder.gui.function;

import java.io.IOException; // thrown of replacement GUI screen template file not found
import java.util.ArrayList; // used to store the params for this meta action

import org.bukkit.Bukkit; // to access the scheduler

import playerquests.Core; // to access the Plugin singleton
import playerquests.builder.gui.GUIBuilder; // used to parse the GUI screen template
import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.product.GUI; // to open a new GUI
import playerquests.utility.ChatUtils; // used to send error message in-game

/**
 * Changes the GUI screen to a different template file.
 */
public class UpdateScreenFile extends GUIFunction {

    /**
     * Not intended to be created directly.
     * <p>
     * Switches the GUI screen to another template.
     * @param params 1. the template file
     * @param director to control the plugin
     * @param slot slot this function belongs to
     */
    public UpdateScreenFile(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    /**
     * Replaces an old GUI window with a new one as described by a template file.
     */
    @Override
    public void execute() {
        validateParams(this.params, String.class);

        GUIBuilder guiBuilder_previous = this.director.getGUI(); // the builder for the gui
        GUI gui_previous = guiBuilder_previous.getResult(); // the gui product

        // close the existing GUI with the main thread so we can swap to a new one
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // async request an event to occur
            if (gui_previous.isOpen()) { // if gui inventory view exists
                gui_previous.minimise(); // gently close the GUI in case needed later (fully closed on replace)
            }
        });

        // collect params
        String fileName = (String) params.get(0);

        // open the new GUI on the main thread
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // async request an event to occur
            GUIBuilder guiBuilder = new GUIBuilder(this.director);

            try { // to load a new GUI screen from file
                guiBuilder.load(fileName);
                guiBuilder.getResult().open();
                this.slot.executeNext(this.director.getPlayer()); // run the next function
            } catch (IOException e) {
                guiBuilder.getResult().open(); // fallback on previous GUI
                ChatUtils.sendError(director.getPlayer(), "Could not load GUI screen at: " + fileName + ", will cancel rest of functions");
            }

            gui_previous.close();// destroy the last gui
        });
    }

}
