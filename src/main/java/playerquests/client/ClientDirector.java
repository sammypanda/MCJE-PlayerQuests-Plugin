package playerquests.client;

import java.util.HashMap; // holds the current instances
import java.util.Map;

import org.bukkit.entity.Player;

import playerquests.builder.gui.GUIBuilder; // class to control and get GUI product
import playerquests.utility.singleton.PlayerQuests; // the main plugin class

/**
 * Class which provides simple abstractions for clients to use.
 * <p>
 * Used to control the plugin and keep reference of client state.
 */
public class ClientDirector extends Director {
    /**
     * One of every key-mutable instance
     */
    HashMap<Class<?>, Object> currentInstances = new HashMap<>();

    /**
     * The player controlling the client to act on behalf of
     */
    Player player;

    /**
     * Constructs a new ClientDirector instance.
     * @param humanEntity the player controlling the client.
     */
    public ClientDirector(Player player) {
        // set the player to enact on behalf of
        this.player = player;

        // validate the current instances map is correct/has everything
        this.validateCurrentInstances();

        // store the director
        PlayerQuests.getInstance().addDirector(this);
    }

    /**
     * Enforces keeping one instance of every key-mutable class.
     * <ul>
     * <li>GUIBuilder
     * </ul>
     */
    private void validateCurrentInstances() {
        // keep a default GUIBuilder available 
        currentInstances.putIfAbsent(GUIBuilder.class, new GUIBuilder(this));
    }

    /**
     * Retrieve the working instance of an eligible class.
     * @param classType type of instance to return
     * @return current working instance
     */
    public Object getCurrentInstance(Class<?> classType) {
        return this.currentInstances.get(classType);
    }

    /**
     * Put the current/working instance of a class.
     * @param instance class instance to store (and replace if already exists).
     */
    public void setCurrentInstance(Object instance) {
        this.setCurrentInstance(instance, instance.getClass());
    }

    /**
     * Put the current/working instance of a class.
     * @param instance class instance to store (and replace if already exists).
     * @param classType type of the instance.
     */
    public void setCurrentInstance(Object instance, Class<?> classType) {
        // remove if it exists
        if (this.currentInstances.containsKey(instance)) {
            this.currentInstances.remove(classType);
        }

        // set the current class instance for access
        this.currentInstances.put(classType, instance);
    }

    /**
     * Remove the working instance of a class.
     * @param classType type of instance to remove
     */
    public void removeCurrentInstance(Class<?> classType) {
        this.currentInstances.remove(classType);
    }

    /**
     * Get all the current/working instance of classes.
     * @return list of current classes for this client
     */
    public Map<Class<?>, Object> getCurrentInstances() {
        return this.currentInstances;
    }

    /**
     * Creating an empty default GUI. Provides a GUIBuilder to control it.
     * @return a new GUIBuilder
     */
    public GUIBuilder newGUI() {
        // get the GUIBuilder
        GUIBuilder guiBuilder = (GUIBuilder) currentInstances.get(GUIBuilder.class);

        // reset to defaults
        guiBuilder.reset();

        // return the new GUIBuilder
        return guiBuilder;
    }

    /**
     * Gets the current GUIBuilder which owns the GUI.
     * <p>
     * use GUIBuilder.getResult() to get the GUI product.
     * @return the current GUIBuilder
     */
    public GUIBuilder getGUI() {
        // get the current GUIBuilder
        return (GUIBuilder) currentInstances.get(GUIBuilder.class);
    }

    /**
     * Gets the player who has control over the client.
     * @return the player behind the client
     */
    public Player getPlayer() {
        // get the player set on this instance
        return this.player;
    }

    /**
     * Remove all from current instances,
     * good clean up step.
     */
    public void clearCurrentInstances() {
        this.currentInstances.clear();
        this.validateCurrentInstances();
    }

    @Override
    public void close() {
        this.getGUI().getResult().close();
    }
}
