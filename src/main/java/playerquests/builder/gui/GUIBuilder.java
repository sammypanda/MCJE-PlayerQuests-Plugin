package playerquests.builder.gui;

import java.io.IOException; // thrown if a file is not found or invalid
import java.io.InputStream; // stream of file contents

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
     * Director which is responsible for this GUIBuilder
     */
    ClientDirector director;

    /**
     * Instantiate a GUIBuilder with default GUI.
     * @param director director for meta actions to utilise.
     */
    public GUIBuilder(ClientDirector director) {
        // set which director instance created this GUIBuilder
        this.director = director;
    }

    @Override
    public void reset() {
        // TODO: implement reset method
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }

    @Override
    public void load(String templateFile) throws IOException {
        // Init variable where the JSON string will be put
        String templateString = new String();

        // Define the path where screens can be found and
        // Attach the templateFile parameter to the path
        String path = "/gui/screens/" + templateFile + ".json";

        // Pull out the json file as a string
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            
            if (inputStream != null) {
                templateString = new String(inputStream.readAllBytes());
                
                // Process the template into a real GUI screen
                this.parse(templateString);
            } else {
                throw new IOException("nothing to read in " + path);
            }
        } catch (IOException e) { // On an I/O failure such as the file not being found
            throw new IOException("not able to read " + path, e);
        }
    }

    @Override
    public void parse(String templateJSONString) {
        // TODO: implement parse method
        throw new UnsupportedOperationException("Unimplemented method 'parse'");
    }

    @Override
    public GUI getResult() {
        return new GUI(this.director.getPlayer());
    }
}