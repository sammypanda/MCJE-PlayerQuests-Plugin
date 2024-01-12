package playerquests.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import playerquests.BukkitTestUtil;

public class GUITest extends BukkitTestUtil {

    private HumanEntity humanEntity;
    private GUILoader guiLoader;

    @BeforeAll
    void setUp() {
        // creating mock Bukkit and getPluginManager for the GUI initializer
        PluginManager pluginManager = Mockito.mock(PluginManager.class);
        when(Bukkit.getPluginManager()).thenReturn(pluginManager);
        
        // creating mock UnsafeValues for getTranslationKey() 
            // this should be deprecated but it's still being called internally by the Spigot API
        UnsafeValues unsafeValues = Mockito.mock(UnsafeValues.class);
        when(Bukkit.getUnsafe()).thenReturn(unsafeValues);

        // creating mock humanEntity to start a GUI
        this.humanEntity = Mockito.mock(HumanEntity.class);

        // start a GUI using guiLoader (so we can pass in a template)
        this.guiLoader = new GUILoader(humanEntity);
    }

    @Test
    void validTemplate() {
        // Load the GUI with the resource named "demo"
        GUI gui = this.guiLoader.load("demo");
            
        // Perform assertions on the loaded GUI
        assertEquals("Demo", gui.getTitle());
        assertEquals(18, gui.getSize());
    }
    
    @Test
    void emptyTemplate() {
        // invalid templateFile, an empty json object
        GUI gui = this.guiLoader.load("empty");

        assertEquals("", gui.getTitle());
        assertEquals(9, gui.getSize());
    }
}
