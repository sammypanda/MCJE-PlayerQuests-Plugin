package playerquests.gui;

import java.io.IOException; // used if the JSON GUI template file can't be loaded
import java.io.InputStream; // to load the JSON GUI template file
import java.util.Optional; // tolerates if a json field is or is not set

import org.bukkit.entity.HumanEntity; // the human entity (usually player) which will see the GUI

import com.fasterxml.jackson.core.JsonProcessingException; // catch-all erroring for the JSON files
import com.fasterxml.jackson.databind.JsonNode; // the java-friendly object for holding the JSON info
import com.fasterxml.jackson.databind.ObjectMapper; // reads the JSON

/**
 * Parses the GUI screen templates as a GUI.
 * The screens are kept as JSON files in resources/gui/screens.
 */
public class GUILoader {

    private ObjectMapper jsonObjectMapper = new ObjectMapper();
    private GUI gui;
    private HumanEntity humanEntity;
    private String path;

    /**
     * Constructs a new GUILoader ready to parse the JSON templates.
     * @param    gui An existing GUI to load things into.
     */
    public GUILoader(GUI gui) {
        this.gui = gui;
        this.humanEntity = gui.getViewer();
    }

    /**
     * Constructs a new GUILoader and GUI ready to parse the JSON templates.
     * @param    humanEntity The (usually player) who should view the GUI.
     */
    public GUILoader(HumanEntity humanEntity) {
        this.gui = new GUI(humanEntity);
        this.humanEntity = humanEntity;
    }
    
    /**
     * Translating a GUI screen template from JSON file into a {@link GUI}.
     * <p>
     * Expects the file name without .json on the end.
     * JSON template layout in Specification section of README.
     * @param    templateFile The name of the template json file excluding .json
     * @return   An instance of {@link GUI}
     */
    public GUI load(String templateFile) {
        // Init variable where the JSON string will be put
        String templateString = new String();

        // Define the path where screens can be found and
        // Attach the templateFile parameter to the path
        this.path = "/gui/screens/" + templateFile + ".json";

        // Pull out the json file as a string
        try (InputStream inputStream = getClass().getResourceAsStream(this.path)) {
            
            if (inputStream != null) {
                templateString = new String(inputStream.readAllBytes());
                
                // Process the template into a real GUI screen
                parse(templateString);
            } else {
                System.err.println(this.path + " is not a valid path");
            }
        } catch (IOException e) { // On an I/O failure such as the file not being found
            System.err.println("not able to read " + this.path);
        }

        return this.gui;
    }

    /**
     * Translating a GUI screen template from a JSON string into a {@link GUI}.
     * <p>
     * JSON template layout in Specification section of README.
     * @param    templateString the gui template json as a string
     * @return   An instance of {@link GUI}
     */
    public GUI parse(String templateString) {
        // Init variable where the GUITemplate object will be put
        JsonNode template;

        // Convert the JSON string into a GUITemplate object
        // This makes it easier to pull values out of the JSON 
        try {
            // readValue(String content, Class<T> valueType)
            // Method to deserialize JSON content from given JSON content String.
            template = this.jsonObjectMapper.readTree(templateString);

            // flexibly set the values from keys to.. 
            // the GUI screen 
            parseTitle(template);
            // the inventory slots size
            parseSize(template);
            // the content of the slots
            parseSlots(template);

        } catch (JsonProcessingException e) { // Encapsulates all JSON processing errors that could occur
            System.err.println("The template passed into parse(here) failed to map, JSON: " + templateString);
        }

        return this.gui;
    }

    /**
     * Take the 'title' template key and set it as the title for the gui.
     * @param node
     */
    private void parseTitle(JsonNode node) {
        String title = Optional.ofNullable(node.get("title")) // get title field if it exists
            .map(JsonNode::asText) // if exists get it as text (String)
            .orElse(""); // if not set it as an empty string

        this.gui.setTitle(title);
    }

    /**
     * Take the 'size' template key and set as the total slots of the gui.
     * @param node
     */
    private void parseSize(JsonNode node) {
        int size = Optional.ofNullable(node.get("size")) // get size field if it exists
            .map(JsonNode::asInt) // if exists get it as Int (int)
            .orElse(9); // if not set it as the default size
        
        this.gui.setSize(size);
    }

    /**
     * Take the slots array and add each slot with it's custom described function(s).
     * @param node
     */
    private void parseSlots(JsonNode node) {
        Optional.ofNullable(node.get("slots")) // get slots field if it exists
            .map(JsonNode::elements) // map to a JsonNode Iterator
            .ifPresent(slots -> slots.forEachRemaining(e -> { // iterate over slots array
                GUISlot guiSlot = gui.newSlot();

                System.out.println("raw entry: " + e); // shows all the slots and their details
                
                Optional.ofNullable(e.get("slot")) // get slot field if it exists
                .map(JsonNode::asInt) // if exists get it as Int (int)
                .ifPresent(slot -> guiSlot.setSlot(slot)); // set the slot position in the GUI

                Optional.ofNullable(e.get("item")) // get item to fill slot if exists
                .map(JsonNode::asText) // if exists get it as Text (String)
                .ifPresent(item -> guiSlot.setItem(item)); // set the GUI slot item

                Optional.ofNullable(e.get("label")) // get label for slot if exists
                .map(JsonNode::asText) // if exists get it as Text (String)
                .ifPresent(label -> guiSlot.setLabel(label)); // set the GUI slot label
            }));
    }
}