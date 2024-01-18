package playerquests.builder.gui;

import playerquests.builder.Builder; // builder interface
import playerquests.client.ClientDirector; // used to control the plugin (for GUI meta functions)
import playerquests.product.GUI; // GUI product this class builds

/**
 * The interface for creating and opening a GUI.
 * Size can be changed to multiples of 9, from 0 up to 54.
 * The default size is 0.
 * <br>
 * <pre>
 * Usage:
 * <code>
 * getServer().getOnlinePlayers().iterator().forEachRemaining(player -> { // for this example, opening the gui for everyone
 *     ClientDirector director = new ClientDirector(player); // controlling the plugin
 *     GUIBuilder guiBuilder = director.newGUI("main"); // creating and controlling a gui
 *     GUI gui = guiBuilder.getResult(); // get the created GUI
 *     gui.open(); // open the GUI
 * });      
 * </code>
 * </pre>
 */
public class GUIBuilder implements Builder {

    /**
     * Instantiate a GUIBuilder with default GUI.
     * @param director director for meta actions to utilise.
     */
    public GUIBuilder(ClientDirector director) {
        // TODO: construct the GUIBuilder
    }

    @Override
    public void reset() {
        // TODO: implement reset method
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }

    @Override
    public void load(String templateFile) {
        // TODO: implement load method
        throw new UnsupportedOperationException("Unimplemented method 'load'");
    }

    @Override
    public void parse(String templateJSONString) {
        // TODO: implement parse method
        throw new UnsupportedOperationException("Unimplemented method 'parse'");
    }

    @Override
    public GUI getResult() {
        // TODO: implement getResult method
        throw new UnsupportedOperationException("Unimplemented method 'getResult'");
    }
}