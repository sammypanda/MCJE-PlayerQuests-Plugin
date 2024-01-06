package playerquests.quest;

import org.bukkit.entity.HumanEntity;

import playerquests.gui.GUI;
import playerquests.gui.GUILoader;

public class Quest {
    
    public static void display(HumanEntity humanEntity) {
        GUILoader guiLoader = new GUILoader(humanEntity); // create tool to build guis with
        GUI gui = guiLoader.load("main"); // build/load the gui with a template file
        gui.open(); // prepare and open the GUI on the player screen
    }
}
