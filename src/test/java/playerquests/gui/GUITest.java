package playerquests.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bukkit.entity.HumanEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import playerquests.BukkitTestUtil;

public class GUITest extends BukkitTestUtil {

    private HumanEntity humanEntity;
    private GUILoader guiLoader;

    @BeforeAll
    void setUp() {
        // creating mock humanEntity to start a GUI
        this.humanEntity = Mockito.mock(HumanEntity.class);

        // start a GUI using guiLoader (so we can pass in a template)
        this.guiLoader = new GUILoader(humanEntity);
    }

    @Test 
    void validTemplate() {
        // valid templateFile, should be found in resources
        GUI gui = this.guiLoader.load("demo");

        assertNotNull(gui.getTitle());
    }

    @Test
    void invalidTemplate() {
        // invalid templateFile, an empty json object
        GUI gui = this.guiLoader.load("invalid");

        assertEquals(gui.getTitle(), "");
    }
}
