package playerquests.builder.gui.function;

import java.io.IOException; // thrown of replacement GUI screen template file not found
import java.util.ArrayList; // used to store the params for this meta action

import playerquests.builder.gui.GUIBuilder; // used to parse the GUI screen template
import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
import playerquests.client.ClientDirector; // powers functionality for functions
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

        // collect params
        String fileName = (String) params.get(0);        

        // close old GUI
        this.director.getGUI().getResult().close();
        
        // try to load the GUI screen file
        try {
            // replace the GUIBuilder
            GUIBuilder guiBuilder = new GUIBuilder(this.director, false); // create next GUI
            this.director.setCurrentInstance(guiBuilder);
            guiBuilder.load(fileName); // load this next screen
        } catch (IOException e) {
            this.errored = true;
            ChatUtils.sendError(director.getPlayer(), "Could not load GUI screen at: " + fileName + ", will cancel rest of functions", e);
        }

        this.director.getGUI().getResult().open();

        if (!this.errored) { 
            this.slot.executeNext(this.director.getPlayer()); // run the next function
        }
    }

}
