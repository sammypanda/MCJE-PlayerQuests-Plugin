package playerquests.builder.gui.component;

import java.util.Optional; // handling nullable values
import java.util.regex.Matcher; // matching string to regex pattern
import java.util.regex.Pattern; // creating regex pattern

import com.fasterxml.jackson.databind.JsonNode; // type to interpret json objects

import playerquests.Core; // getting the keyhandler
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
     * Take the 'title' template key and set it as the title for the gui.
     * @param template the template as a jsonnode object
     */
    public void parseTitle(JsonNode template) {
        String title = Optional.ofNullable(template.get("title")) // get title field if it exists
            .map(JsonNode::asText) // if exists get it as text (String)
            .orElse(this.getTitle()); // if not set it as the default title

        this.setTitle(title);
    }

    /**
     * Take the 'size' template key and set as the total slots of the gui.
     * @param template the template as a jsonnode object
     */
    public void parseSize(JsonNode template) {
        int size = Optional.ofNullable(template.get("size")) // get size field if it exists
            .map(JsonNode::asInt) // if exists get it as Int (int)
            .orElse(9); // if not set it as the default size
        
        this.setSize(size);
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

}
