package playerquests.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
