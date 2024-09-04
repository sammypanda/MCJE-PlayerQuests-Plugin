package playerquests.client;

import java.util.HashMap; // holds the current instances

import org.bukkit.entity.HumanEntity; // the player who controls the client

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
    HumanEntity player;

    /**
     * Constructs a new ClientDirector instance.
     * @param humanEntity the player controlling the client.
     */
    public ClientDirector(HumanEntity humanEntity) {
        // set the player to enact on behalf of
        this.player = humanEntity;

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
     * @param instance type of instance to store (and replace if already exists).
     */
    public void setCurrentInstance(Object instance) {
        // remove if it exists
        if (this.currentInstances.containsKey(instance)) {
            this.currentInstances.remove(instance.getClass());
        }

        // set the current class instance for access
        this.currentInstances.put(instance.getClass(), instance);
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
    public HashMap<Class<?>, Object> getCurrentInstances() {
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
    public HumanEntity getPlayer() {
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
