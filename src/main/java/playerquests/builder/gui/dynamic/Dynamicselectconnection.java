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

/**
 * A dynamic GUI screen for selecting connections between quest stages and actions.
 * <p>
 * This screen allows users to select a quest stage and, if a stage is selected, choose an associated action.
 * It provides options to either select the stage alone or choose an action associated with the stage.
 * Users can also navigate back to the previous screen or remove the current connection.
 * </p>
 */
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
     * The code to run on connection select.
     */
    private Consumer<Object> onSelect;

    /**
     * Constructs a new {@code Dynamicselectconnection} instance.
     * @param director the client director that manages GUI interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
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
            IntStream.range(rangeOffset, 19).forEach((int slot) -> {
                int i = slot - rangeOffset;

                if (actions.size() < i + 1) {
                    return;
                }

                QuestAction action = selectedStage.getActions().get(actions.get(i));

                new GUISlot(gui, slot)
                    .setLabel(action.getID())
                    .setItem("DETECTOR_RAIL")
                    .onClick(() -> {
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
            // set the title for showing quest stages
            frame.setTitle("Select Stage");

            // show nullify option
            new GUISlot(gui, 1)
                .setLabel("Remove connection")
                .setItem("BARRIER")
                .onClick(() -> {
                    this.select(null);
                });

            // show the quest stages
            int rangeOffset = 2; // the amount to subtract from the slot number, to get an index from 0
            List<String> stages = questBuilder.getStages();
            IntStream.range(rangeOffset, 19).forEach((int slot) -> {
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
        new UpdateScreen(
            new ArrayList<>(Arrays.asList(this.previousScreen)), 
            director
        ).execute();

        if (this.onSelect != null) {
            onSelect.accept(
                path
            );
        }
    }

    /**
     * Sets the code to run when a connection (stage/action) is selected.
     * <p>
     * This method allows for custom handling of the selected connection.
     * </p>
     * @param onSelect a {@link Consumer} that processes the selected connection.
     * @return the connection that was selected.
     */
    public Object onSelect(Consumer<Object> onSelect) {
        this.onSelect = onSelect;
        return onSelect;
    }
}
