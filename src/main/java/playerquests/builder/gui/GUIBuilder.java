package playerquests.builder.gui;

import java.util.ArrayList; // array list type
import java.util.HashMap; // holds and manages info about the GUI slots
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.Set; // used to retrieve the key values for this.slots
import java.util.stream.IntStream; // used to find the next possible empty slot 

import org.bukkit.Bukkit; // used to refer to base spigot/bukkit methods
import org.bukkit.event.HandlerList; // list of event handlers; used to unload a listener

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
    private GUIFrame guiFrame;

    /**
     * event listener for gui events
     */
    private GUIListener guiListener;

    /**
     * The names of this screen GUIs, and the ones before.
     */
    private String screenName;

    /**
     * The previous screens the user have been to.
     */
    private List<String> previousScreens = new ArrayList<>();

    /**
     * Instantiate a GUIBuilder with default GUI and set as current GUIBuilder.
     * @param director director for the gui to utilise.
     */
    public GUIBuilder(ClientDirector director) {
        new GUIBuilder(director, true); // create and set as current GUIBuilder
    }

    /**
     * Instantiate a GUIBuilder with default GUI + choice if set as current GUIBuilder.
     * @param director director for the gui to utilise.
     * @param current if to set the builder as the current builder instance.
     */
    public GUIBuilder(ClientDirector director, Boolean current) {
        // set which director instance created this GUIBuilder
        this.director = director;

        // create default gui frame
        this.guiFrame = new GUIFrame(director);

        // create default GUI product
        this.gui = new GUI(this);

        // adding listening to when gui events occur
        this.guiListener = new GUIListener(this);
        Bukkit.getPluginManager().registerEvents(this.guiListener, Core.getPlugin());

        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current instance of gui to be accessed with key-pair syntax

        if (current) {
            GUIBuilder oldGUI = (GUIBuilder) this.director.getCurrentInstance(GUIBuilder.class);
            if (oldGUI != null) {
                oldGUI.getResult().minimise();
            };

            // set as the current instance in the director
            director.setCurrentInstance(this);
        }
    }

    @Override
    public void reset() {
        // reset values 
        // by replacing with new instances
        this.guiSlots = new HashMap<Integer, GUISlot>();
        this.guiFrame = new GUIFrame(this.director);
        this.gui = new GUI(this);
    }

    /**
     * Prepares the GUI window to be closed, in such a way that there
     * are no leftover objects or listeners.
     */
    public void dispose() {
        HandlerList.unregisterAll(this.guiListener); // unregister the listeners, don't need them if there is no GUI
        Core.getKeyHandler().deregisterInstance(this); // remove the current instance from key-pair handler
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
     * Get the next empty slot available.
     * @return next available GUI slot
     */
    public Integer getEmptySlot() {
        Set<Integer> filledSlots = this.guiSlots.keySet(); // get the positions of all currently stored slots

        Integer lowestEmptySlot = IntStream.iterate(1, i -> i + 1) // counter stream, starting at 1
            .filter(slot -> !filledSlots.contains(slot)) // conditional for adding to stream
            .findFirst() // terminate if there is a value in the stream
            .orElse(1); // default value

        return lowestEmptySlot; // the next empty slot
    }

    /**
     * Removes the {@link GUISlot} at the inventory slot position.
     * @param position the gui slot position, starting at 1.
     */
    public void removeSlot(Integer position) {
        this.guiSlots.remove(position);
    }

    /**
     * Responsible for clearing the GUI slots.
     * <p>
     * Useful for Dynamic GUIs which may want to 
     * show a new array of slots.
     */
    public void clearSlots() {
        this.guiSlots.clear();
        this.getResult().clearSlots();
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

    /**
     * Get the real screen name of the GUI
     * @return the dynamic gui name
     */
    public String getScreenName() {
        return this.screenName;
    }

    /**
     * Set the real screen name of the GUI
     * @param name the dynamic gui name
     */
    public void setScreenName(String name) {
        this.screenName = name;
    }

    /**
     * Gets the previous screen names 
     * @return the previous screens to go back to.
     */
    public List<String> getPreviousScreens() {
        return this.previousScreens;
    }

    /**
     * Sets the previous screen names 
     * @param previousScreens the previous screens to go back to.
     */
    public void setPreviousScreens(List<String> previousScreens) {
        this.previousScreens = previousScreens;
    }

}