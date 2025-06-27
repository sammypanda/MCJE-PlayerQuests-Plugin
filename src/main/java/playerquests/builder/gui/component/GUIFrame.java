package playerquests.builder.gui.component;

import playerquests.builder.gui.data.GUIMode; // how the GUI can be interacted with
import playerquests.client.ClientDirector; // controlling the plugin

/**
 * The information and content of the outer GUI window.
 */
public class GUIFrame {

    /**
     * The director for this context
     */
    @SuppressWarnings("unused")
    private ClientDirector director;

    /**
     * Title of the GUI.
     */
    String title = "Default";

    /**
     * Number of slots in the GUI.
     */
    Integer size = 9;

    /**
     * How the GUI can currently be interacted with.
     */
    GUIMode mode = GUIMode.CLICK;

    /**
     * Construct a GUI frame with default content.
     * @param director setting and retrieving values
     */
    public GUIFrame(ClientDirector director) {
        this.director = director;
    }

    /**
     * Set the title of the GUI frame.
     * @param title string of the gui title
     * @return the GUI frame
     */
    public GUIFrame setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Get the title of the GUI frame.
     * @return string of the gui title 
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set's the number of slots in the GUI screen.
     * @param size the number of slots (has to be a multiple of 9. Like 9 or 18. Up to 54.
     * @return the GUI frame
     */
    public GUIFrame setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * Get's the size of the GUI screen (inventory slots).
     * @return the number of slots in the inventory
     */
    public Integer getSize() {
        return this.size;
    }

    /**
     * Sets the mode the GUI is in.
     * @param mode how the GUI can be interacted with
     * @return the GUI frame
     */
    public GUIFrame setMode(GUIMode mode) {
        this.mode = mode;
        return this;
    }

/**
     * Retrieves the current mode of interaction for the GUI.
     * @return the {@link GUIMode} that specifies how the GUI can be interacted with.
     * @see GUIMode
     */
    public GUIMode getMode() {
        return this.mode;
    }

}
