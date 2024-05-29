package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;

public class Dynamicselectconnection extends GUIDynamic {

    /**
     * The quest we are editing.
     */
    QuestBuilder questBuilder;
    
    /**
     * The stage that has been selected.
     */
    QuestStage selectedStage = null;

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

        if (this.selectedStage != null) { // if user has selected a stage
            // show the stage actions //

            new GUISlot(gui, 1)
                .setLabel("Just Select This Stage")
                .setItem("YELLOW_DYE");

            int rangeOffset = 2; // the amount to subtract from the slot number, to get an index from 0
            List<String> actions = new ArrayList<>(selectedStage.getActions().keySet());
            IntStream.range(2, 19).forEach((int slot) -> {
                int i = slot - rangeOffset;

                if (actions.size() < i + 1) {
                    return;
                }

                QuestAction action = selectedStage.getActions().get(actions.get(i));

                new GUISlot(gui, slot)
                    .setLabel(action.getID())
                    .setItem("DETECTOR_RAIL");
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
        } else {
            // show the quest stages //

            int rangeOffset = 1; // the amount to subtract from the slot number, to get an index from 0
            List<String> stages = questBuilder.getStages();
            IntStream.range(1, 19).forEach((int slot) -> {
                int i = slot - rangeOffset;

                if (stages.size() < i + 1) {
                    return;
                }

                QuestStage stage = questBuilder.getQuestPlan().get(stages.get(i));

                new GUISlot(gui, slot)
                    .setLabel(stage.getTitle())
                    .setItem("CHEST")
                    .onClick(() -> {
                        this.selectedStage = stage;
                        gui.clearSlots();
                        this.execute();
                    });
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
    
}
