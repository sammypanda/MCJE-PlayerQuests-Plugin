package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.List; // generic list type
import java.util.stream.IntStream; // used to iterate over a series

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.action.None;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector; // controlling the plugin

/**
 * Shows a dynamic for modifying the current quest stage.
 */
public class Dynamicqueststage extends GUIDynamic {

    /**
     * The current quest stage
     */
    QuestStage questStage;

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
    }

    @Override
    protected void execute_custom() {
        this.gui.getFrame().setTitle("{QuestStage} Editor");

        // the back button
        GUISlot exitButton = new GUISlot(this.gui, 1);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList("queststages")), // set the previous screen 
            director // set the client director
        ));

        // produce slots listing current actions
        List<String> actionKeys = new ArrayList<String>(this.questStage.getActions().keySet());
        IntStream.range(0, actionKeys.size()).anyMatch(index -> {

            String action = actionKeys.get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot actionSlot = new GUISlot(this.gui, nextEmptySlot);

            // identify which action is the stage entry point
            if (this.questStage.getEntryPoint().getID().equals(action)) { // if this action is the entry point
                actionSlot.setLabel(action.toString() + " (Entry Point)");
                actionSlot.setItem("POWERED_RAIL");
            } else { // if it's not the entry point
                actionSlot.setLabel(action.toString());
                actionSlot.setItem("DETECTOR_RAIL");
            }

            actionSlot.onClick(() -> {
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

        // add new action button
        GUISlot newActionButton = new GUISlot(this.gui, 8);
        newActionButton.setLabel("New Action");
        newActionButton.setItem("LIME_DYE");
        newActionButton.onClick(() -> {
            new None(this.questStage).submit(); // create the new action to present
            this.gui.clearSlots(); // clear to prevent duplicates
            this.execute(); // re-run to see new action in list
        });

        // re-arrange button
        new GUISlot(gui, 9)
            .setItem("STICKY_PISTON")
            .setLabel("Re-arrange");
    }
    
}
