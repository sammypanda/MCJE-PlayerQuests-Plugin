package playerquests.gui;

import org.bukkit.entity.HumanEntity;

/**
 * Parses the GUI screen templates as a GUI.
 * The screens are kept as JSON files in resources/gui/screens.
 */
public class GUILoader {

    GUI gui;
    HumanEntity humanEntity;

    /**
     * Constructs a new GUILoader ready to parse the JSON templates.
     * @param    gui An existing GUI to load things into.
     * @see      #load() Function to parse the JSON.
     * @return   An instance of {@link #GUILoader()}.
     */
    public GUILoader(GUI gui) {
        this.gui = gui;
        this.humanEntity = gui.getViewer();
    }

    /**
     * Constructs a new GUILoader and GUI ready to parse the JSON templates.
     * @param    humanEntity The (usually player) who should view the GUI.
     * @see      GUI#getViewer()
     */
    public GUILoader(HumanEntity humanEntity) {
        this.gui = new GUI(humanEntity);
        this.humanEntity = humanEntity;
    }
    
    /**
     * Translating a GUI screen template from JSON into a {@link GUI}.
     * <p>
     * JSON template layout in Specification in README.
     * @param    template The name of the template json file excluding .json
     * @return   An instance of {@link GUI}
     */
    GUI load(String template) {
        return this.gui;
    }
}