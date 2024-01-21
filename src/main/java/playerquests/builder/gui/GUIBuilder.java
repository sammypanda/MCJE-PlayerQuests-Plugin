package playerquests.builder.gui;

import java.io.IOException; // thrown if a file is not found or invalid
import java.io.InputStream; // stream of file contents
import java.util.HashMap; // holds and manages info about the GUI slots
import java.util.Map; // generic map type
import java.util.Optional; // used to check and work with nullable values
import java.util.function.Consumer; // used to execute code on a result of method

import org.bukkit.Bukkit; // used to refer to base spigot/bukkit methods
import org.bukkit.event.HandlerList; // list of event handlers; used to unload a listener

import com.fasterxml.jackson.core.JsonProcessingException; // thrown if json is invalid
import com.fasterxml.jackson.databind.JsonNode; // type for interpreting json in java
import com.fasterxml.jackson.databind.ObjectMapper; // used to convert json string to jsonnode

import playerquests.Core; // used to get the Plugin/KeyHandler instances
import playerquests.builder.Builder; // builder interface
import playerquests.builder.gui.component.GUIFrame; // the contents of the outer frame
import playerquests.builder.gui.component.GUISlot; // the contents of a slot
import playerquests.client.ClientDirector; // used to control the plugin (for GUI meta functions)
import playerquests.client.gui.listener.GUIListener; // listening for user interaction with the GUI
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
     * Director which is responsible for this GUIBuilder.
     */
    private ClientDirector director;

    /**
     * The GUI product this builder creates.
     */
    private GUI gui;

    /**
     * All the GUI slots.
     */
    private Map<Integer, GUISlot> guiSlots = new HashMap<Integer, GUISlot>();

    /**
     * Outer GUI frame content.
     */
    private GUIFrame guiFrame = new GUIFrame();

    /**
     * handles JSON objects
     */
    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    /**
     * event listener for gui events
     */
    private GUIListener guiListener;

    /**
     * Instantiate a GUIBuilder with default GUI.
     * @param director director for meta actions to utilise.
     */
    public GUIBuilder(ClientDirector director) {
        // set which director instance created this GUIBuilder
        this.director = director;

        // create default GUI product
        this.gui = new GUI(this);

        // adding listening to when gui events occur
        this.guiListener = new GUIListener(this);
        Bukkit.getPluginManager().registerEvents(this.guiListener, Core.getPlugin());

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current instance of gui to be accessed with key-pair syntax

        // set as the current instance in the director
        director.setCurrentInstance(this);
    }

    @Override
    public void reset() {
        // reset values 
        // by replacing with new instances
        this.guiSlots = new HashMap<Integer, GUISlot>();
        this.guiFrame = new GUIFrame();
        this.gui = new GUI(this);
    }

    /**
     * Prepares the GUI window to be closed, in such a way that there
     * are no leftover objects or listeners.
     */
    public void dispose() {
        HandlerList.unregisterAll(this.guiListener); // unregister the listeners, don't need them if there is no GUI
        Core.getKeyHandler().deregisterInstance(this); // remove the current instance from key-pair handler
        
        // nullify class values we are never going to use again
        this.guiFrame = null;
        this.guiSlots = null;
        this.guiListener = null;
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
        // Init variable where the GUITemplate object will be put
        JsonNode template;

        // Convert the JSON string into a GUITemplate object
        // This makes it easier to pull values out of the JSON 
        try {
            // readValue(String content, Class<T> valueType)
            // Method to deserialize JSON content from given JSON content String.
            template = this.jsonObjectMapper.readTree(templateJSONString);

            // flexibly set the values from keys to.. 
            // the GUI screen 
            this.guiFrame.parseTitle(template);
            // the inventory slots size
            this.guiFrame.parseSize(template);
            // the content of the slots
            this.parseMultiple(template.get("slots"), slot -> this.newSlot(slot));

        } catch (JsonProcessingException e) { // Encapsulates all JSON processing errors that could occur
            throw new IllegalArgumentException("the JSON is malformed in the template: " + templateJSONString, e);
        }
    }

    /**
     * Parsing JSON arrays into single objects.
     * <p>
     * Usage:
     * <code>
     * parseMultiple(jsonNodeArray, element -> parseObject(element))     
     * </code>
     * </pre>
     * @param node the json field with multiple elements
     * @param consumer the consumer to execute code on each lone jsonnode element retrieved
     */
    private void parseMultiple(JsonNode node, Consumer<JsonNode> consumer) {
        Optional.ofNullable(node) // tolerate if value is null
            .map(JsonNode::elements) // map to a JsonNode iterator
            .ifPresent(slots -> slots.forEachRemaining(slot -> { // for each slot
                consumer.accept(slot); // do as determined by method caller
            }));
    }

    /**
     * Creates a new GUI slot.
     * @param slot json object for this slot.
     */
    public void newSlot(JsonNode slot) {
        GUISlot guiSlot = new GUISlot(this, -1); // set the pre-prepared gui slot

        Optional.ofNullable(slot.get("slot")) // get slot field if it exists
            .map(JsonNode::asInt) // if exists get it as Int (int)
            .ifPresent(position -> guiSlot.setPosition(position)); // set the slot position in the GUI

        Optional.ofNullable(slot.get("item")) // get item to fill slot if exists
            .map(JsonNode::asText) // if exists get it as Text (String)
            .ifPresent(item -> guiSlot.setItem(item)); // set the GUI slot item

        Optional.ofNullable(slot.get("label")) // get label for slot if exists
            .map(JsonNode::asText) // if exists get it as Text (String)
            .ifPresent(label -> guiSlot.setLabel(label)); // set the GUI slot label

        Optional.ofNullable(slot.get("functions")) // get functions for slot if exists
            .ifPresent(functions -> guiSlot.parseFunctions(functions)); // parse all the functions in the object
    }

    /**
     * Update which slot of the GUI a {@link GUISlot} object shows in.
     * @param position the gui slot position, starting at 1.
     * @param slot the already created {@link GUISlot}  object to put in the slot.
     */
    public void setSlot(Integer position, GUISlot slot) {
        if (this.getSlot(position) != null) { // see if slot already exists in HashMap
            this.removeSlot(position); // remove slot if it already exists
        }

        this.guiSlots.put(position, slot); // put our current GUISlot instead
    }

    /**
     * Gets the GUI slot at the inventory slot position.
     * @param position the gui slot position, starting at 1.
     * @return the gui slot object.
     */
    public GUISlot getSlot(Integer position) {
        return this.guiSlots.get(position);
    }

    /**
     * Removes the {@link GUISlot} at the inventory slot position.
     * @param position the gui slot position, starting at 1.
     */
    public void removeSlot(Integer position) {
        this.guiSlots.remove(position);
    }

    /**
     * Get the GUI instance this builder creates.
     * @return the product gui of this builder.
     */
    @Override
    public GUI getResult() {
        return this.gui;
    }

    /**
     * Get the director instance which owns this builder.
    * @return the client director instance
     */
    public ClientDirector getDirector() {
        return this.director;
    }

    /**
     * Get reference to all the slots.
     * @return the list of gui slots
     */
    public Map<Integer, GUISlot> getSlots() {
        return this.guiSlots;
    }

    /**
     * Get reference to the outer GUI frame.
     * @return the gui frame instance
     */
    public GUIFrame getFrame() {
        return this.guiFrame;
    }

}