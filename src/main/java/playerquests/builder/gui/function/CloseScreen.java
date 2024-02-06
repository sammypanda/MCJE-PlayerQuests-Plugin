package playerquests.builder.gui.function;

import java.util.ArrayList; // used to store the params for this meta action

import playerquests.builder.gui.component.GUISlot; // holds information about the GUI slot
import playerquests.client.ClientDirector; // powers functionality for functions

/**
 * Fully closes the GUI window.
 */
public class CloseScreen extends GUIFunction {

    /**
     * Not intended to be created directly.
     * <p>
     * Closes the currently open GUI screen.
     * @param params none required
     * @param director used to control the plugin
     * @param slot slot this function belongs to
     */
    public CloseScreen(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    /**
     * Calls the close() method in the GUI class.
     */
    @Override
    public void execute() {
        this.director.getGUI().getResult().close();
        this.finished(); // onFinish runnable
    }
}