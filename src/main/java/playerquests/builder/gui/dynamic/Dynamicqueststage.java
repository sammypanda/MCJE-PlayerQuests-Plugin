package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.List; // generic list type
import java.util.stream.IntStream; // used to iterate over a series

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.data.GUIMode; // how the GUI can be interacted with
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.None;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector; // controlling the plugin
import playerquests.utility.singleton.QuestRegistry;

/**
 * Shows a dynamic for modifying the current quest stage.
 */
public class Dynamicqueststage extends GUIDynamic {

    /**
     * The current quest stage
     */
    QuestStage questStage;

    /**
     * Listing current actions
     */
    List<String> actionKeys;

    /**
     * Staging to delete the stage
     */
    Boolean confirm_delete = false;

    /**
     * The builder object for this quest
     */
    QuestBuilder questBuilder;

    /**
     * Specify if actionKeys has already been looped through
     */
    private boolean confirm_actionKeys = false;

    /**
     * Creates a dynamic GUI to edit a quest stage.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicqueststage(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // set the quest stage instance
        this.questStage = (QuestStage) this.director.getCurrentInstance(QuestStage.class);

        // get the quest builder
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        // set actionKeys
        this.actionKeys = new ArrayList<String>(this.questStage.getActions().keySet());

        this.gui.getFrame().setTitle("{QuestStage} Editor");
        this.gui.getFrame().setSize(18);

        // the back button
        GUISlot exitButton = new GUISlot(this.gui, 10);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList("queststages")), // set the previous screen 
            director // set the client director
        ));

        // left side dividers
        new GUISlot(this.gui, 2)
            .setItem("BLACK_STAINED_GLASS_PANE");

        new GUISlot(this.gui, 11)
            .setItem("BLACK_STAINED_GLASS_PANE");

        // sequence editor button
        new GUISlot(this.gui, 1)
            .setItem("STICKY_PISTON")
            .setLabel("Change Sequence")
            .onClick(() -> {
                this.director.setCurrentInstance(this.questStage.getConnections());

                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("connectioneditor")), 
                    director
                ).execute();
            });

        // produce slots listing current actions
        if (!confirm_actionKeys) {
            IntStream.range(0, actionKeys.size()).anyMatch(index -> {

                String action = actionKeys.get(index);
                Integer nextEmptySlot = this.gui.getEmptySlot();
                GUISlot actionSlot = new GUISlot(this.gui, nextEmptySlot);

                // identify which action is the stage entry point
                if (this.questStage.getEntryPoint().getAction().equals(action)) { // if this action is the entry point
                    actionSlot.setLabel(action.toString() + " (Entry Point)");
                    actionSlot.setItem("POWERED_RAIL");
                } else { // if it's not the entry point
                    actionSlot.setLabel(action.toString());
                    actionSlot.setItem("DETECTOR_RAIL");
                }

                actionSlot.onClick(() -> {
                    if (!this.gui.getFrame().getMode().equals(GUIMode.CLICK)) {
                        return;
                    }

                    // set the action as the current action to modify
                    this.questStage.setActionToEdit(actionKeys.get(index));
                    // prep the screen to be updated
                    actionSlot.addFunction(new UpdateScreen(
                        new ArrayList<>(Arrays.asList("actioneditor")), 
                        director
                    ));
                    // manually start the slot functions (updating of the screen)
                    actionSlot.execute(this.director.getPlayer());
                });

                return false; // continue the loop
            });

            // set actionKeys as confirmed
            this.confirm_actionKeys = true;
        }

        // add 'delete stage' button (with confirm)
        if (!this.confirm_delete) { // if delete hasn't been confirmed
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem("RED_DYE")
                .setLabel("Delete Stage")
                .onClick(() -> {
                    this.confirm_delete = true;
                    this.execute();
                });
        } else {
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem("RED_WOOL")
                .setLabel("Delete")
                .onClick(() -> {
                    if (this.questBuilder.removeStage(this.questStage)) { // if quest was removed
                        new UpdateScreen(
                            new ArrayList<>(Arrays.asList(previousScreen)), 
                            this.director
                        ).execute();

                        // update the quest
                        QuestRegistry.getInstance().update(this.questBuilder.build());
                    }
                });
        }

        if (!this.questBuilder.removeStage(this.questStage, true)) { // if cannot delete the stage
            new GUISlot(this.gui, this.gui.getFrame().getSize() - 1)
                .setItem("GRAY_DYE")
                .setLabel("Cannot Delete")
                .setDescription("This stage is connected to other stages and actions.");
        }

        // add 'new action' button
        GUISlot newActionButton = new GUISlot(this.gui, this.gui.getFrame().getSize());
        
        if (this.questStage.getActions().size() < 12) {
            newActionButton.setLabel("Add Action");
            newActionButton.setItem("LIME_DYE");
            newActionButton.onClick(() -> {
                new None(this.questStage).submit(); // create the new action

                // update the quest
                QuestRegistry.getInstance().update(this.questBuilder.build());

                // refresh UI
                this.confirm_actionKeys = false; // set actionKeys to be looped through again
                this.gui.clearSlots();
                this.execute(); // re-run to see new action in list
            });
        } else {
            newActionButton.setLabel("No More Action Slots");
            newActionButton.setItem("BARRIER");
        }
    }
    
}
