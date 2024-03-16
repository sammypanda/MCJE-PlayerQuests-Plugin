package playerquests.builder.gui.function;

import java.util.ArrayList; // used to store the params for this meta action

import playerquests.client.ClientDirector; // powers functionality for functions

/**
 * Fully closes the GUI window.
 */
public class CloseScreen extends GUIFunction {

    /**
     * Closes the currently open GUI screen.
     * @param params none required
     * @param director used to control the plugin
     */
    public CloseScreen(ArrayList<Object> params, ClientDirector director) {
        super(params, director);
    }

    /**
     * Calls the close() method in the GUI class.
     */
    @Override
    public void execute() {
        this.director.getGUI().getResult().close();
        this.finished(); // running onFinish code
    }
}