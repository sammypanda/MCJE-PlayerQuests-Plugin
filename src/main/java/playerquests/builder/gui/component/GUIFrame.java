package playerquests.builder.gui.component;

import java.util.Optional; // handling nullable values

import com.fasterxml.jackson.databind.JsonNode; // type to interpret json objects

/**
 * The information and content of the outer GUI window.
 */
public class GUIFrame {

    /**
     * Title of the GUI.
     */
    String title;

    /**
     * Number of slots in the GUI.
     */
    Integer size = 9;

    /**
     * Construct a GUI frame with default content.
     */
    public GUIFrame() {
        this.title = "";
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
        return this.title;
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
