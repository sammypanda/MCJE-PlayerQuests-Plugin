package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;

public class Dynamicselectconnection extends GUIDynamic {

    /**
     * The quest we are editing.
     */
    private QuestBuilder questBuilder;
    
    /**
     * The stage that has been selected.
     */
    private QuestStage selectedStage = null;

    /**
     * The action that has been selected.
     */
    private QuestAction selectedAction = null;

    /**
     * The code to run on connection select.
     */
    private Consumer<Object> onSelect;

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
        
        frame.setSize(27);

        if (this.selectedStage != null) { // if user has selected a stage
            // show the stage actions //
            frame.setTitle("Select Action");

            new GUISlot(gui, 1)
                .setLabel("Just Select This Stage")
                .setItem("YELLOW_DYE")
                .onClick(() -> {
                    this.select(new StagePath(selectedStage, null)); // just select a stage
                });

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
                    .setItem("DETECTOR_RAIL")
                    .onClick(() -> {
                        this.selectedAction = action;
                        this.select(new StagePath(selectedStage, action));
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
        } else {
            // show the quest stages //
            frame.setTitle("Select Stage");

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

    /**
     * Called when a connection is selected.
     * @param object the selected stage/action
     */
    private void select(StagePath path) {
        if (this.onSelect != null) {
            onSelect.accept(
                new StagePath(selectedStage, selectedAction)
            );
        }

        new UpdateScreen(
            new ArrayList<>(Arrays.asList(this.previousScreen)), 
            director
        ).execute();
    }

    /**
     * Code to run when a connection 
     * (QuestAction or QuestStage) is selected.
     * @param onSelect code operation
     * @return the connection that was selected
     */
    public Object onSelect(Consumer<Object> onSelect) {
        this.onSelect = onSelect;
        return onSelect;
    }
    
}
