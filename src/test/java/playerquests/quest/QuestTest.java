package playerquests.quest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.bukkit.entity.HumanEntity;
import org.junit.jupiter.api.Test;

import playerquests.gui.dynamic.Dynamicmyquests;
import playerquests.gui.dynamic.GUIDynamic;

public class QuestTest {
    /**
     * Checks to see if the 'myquests' Dynamic GUI works.
     */
    @Test
    void doesMyQuestsLoad() {
        GUIDynamic myquests = new Dynamicmyquests();

        // Mock values
        HumanEntity humanEntity = mock(HumanEntity.class);

        myquests.setPlayer(humanEntity);

        assertThrows(RuntimeException.class, () -> {
            myquests.execute();
        });
    }
}
