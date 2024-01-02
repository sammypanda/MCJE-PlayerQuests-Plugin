package playerquests.quest;

import org.bukkit.entity.HumanEntity;

import playerquests.gui.GUI;
import playerquests.gui.GUILoader;

public class Quest {
    
    public static void display(HumanEntity humanEntity) {
        GUILoader guiLoader = new GUILoader(humanEntity);
        GUI gui = guiLoader.load("empty");
        gui.open();
    }
}
