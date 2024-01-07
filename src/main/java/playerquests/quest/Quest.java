package playerquests.quest;

import org.bukkit.entity.HumanEntity; // the player to show the GUI to

import playerquests.gui.GUI; // the main class for the gui
import playerquests.gui.GUILoader; // the means to load a gui template from JSON

/**
 * The main class for Quest functionality / the Quest engine.
 */
public class Quest {

    /**
     * Quest should not be instantiated.
     */
    private Quest() {
        throw new AssertionError("Quest class should not be instantiated.");
    }
    
    /**
     * Open the main GUI on the player screen.
     * @param humanEntity the player who should receive/see the GUI.
     */
    public static void display(HumanEntity humanEntity) {
        GUILoader guiLoader = new GUILoader(humanEntity); // create tool to build guis with
        GUI gui = guiLoader.load("main"); // build/load the gui with a template file
        gui.open(); // prepare and open the GUI on the player screen
    }
}
