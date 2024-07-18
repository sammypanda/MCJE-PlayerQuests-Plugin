package playerquests.builder.gui.component;

import java.util.regex.Matcher; // matching string to regex pattern
import java.util.regex.Pattern; // creating regex pattern

import playerquests.Core; // getting the keyhandler
import playerquests.builder.gui.data.GUIMode; // how the GUI can be interacted with
import playerquests.client.ClientDirector; // controlling the plugin

/**
 * The information and content of the outer GUI window.
 */
public class GUIFrame {

    /**
     * The director for this context
     */
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
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get the title of the GUI frame.
     * @return string of the gui title 
     */
    public String getTitle() {
        String title = this.title; // empty title to receive substitutions
        Pattern replacePattern = Pattern.compile("\\{([^}]+)\\}"); // regex for anything inside curly {} brackets
        Matcher matches = replacePattern.matcher(title); // find replacement pattern in the title

        while (matches.find()) {
            String match = matches.group(1); // get the match string

            try {
                Class<?> classType = Core.getKeyHandler().getClassFromKey(match);
                Object instance = this.director.getCurrentInstance(classType); // get the current in-use instance for the class type
                String response = (String) Core.getKeyHandler().getValue(instance, match); // get the value

                title = title.replace("{"+match+"}", response);

            } catch (SecurityException e) {

                title = title.replace("{"+match+"}", "").trim(); // clear the replacement string
            }
        }

        return title;
    }

    /**
     * Set's the number of slots in the GUI screen.
     * @param size the number of slots (has to be a multiple of 9. Like 9 or 18
     */
    public void setSize(int size) {
        this.size = size;
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
     */
    public void setMode(GUIMode mode) {
        this.mode = mode;
    }

    /**
     * Get the mode the GUI is in.
     * @see #GUIMode
     * @return whether the GUI can be interacted with in a certain way
     */
    public GUIMode getMode() {
        return this.mode;
    }

}
