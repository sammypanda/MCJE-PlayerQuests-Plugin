package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.client.ClientDirector;

public class Dynamicselectconnection extends GUIDynamic {

    /**
     * The quest we are editing.
     */
    QuestBuilder questBuilder;

    public Dynamicselectconnection(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.questBuilder = (QuestBuilder) director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        GUIFrame frame = gui.getFrame();
        
        frame.setTitle("Select Connection");
        frame.setSize(27);

        int rangeOffset = 1; // the amount to subtract from the slot number, to get an index from 0
        List<String> stages = questBuilder.getStages();
        IntStream.range(1, 19).forEach((int slot) -> {
            int i = slot - rangeOffset;

            if (stages.size() < i + 1) {
                return;
            }

            new GUISlot(gui, slot)
                .setLabel("[A Stage]")
                .setItem("CHEST");
        });

        new GUISlot(gui, 19)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .onClick(() -> {
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(previousScreen)),
                    director
                ).execute();
            });
    }
    
}
