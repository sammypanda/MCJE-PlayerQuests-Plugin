package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.stream.IntStream; // used to iterate over a series

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.component.QuestAction; // modifying a quest stage action
import playerquests.builder.quest.component.QuestStage; // modifying the quest stage
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
            director, // set the client director
            exitButton // the origin GUI slot
        ));

        // produce slots listing current actions
        IntStream.range(0, this.questStage.getActions().size()).anyMatch(index -> {
            System.out.println("producting at index = " + index);

            QuestAction action = this.questStage.getActions().get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot actionSlot = new GUISlot(this.gui, nextEmptySlot);
            actionSlot.setItem("DETECTOR_RAIL");
            actionSlot.setLabel(action.getTitle());
            actionSlot.onClick(() -> {
                this.director.setCurrentInstance(action);
            });
            actionSlot.addFunction(new UpdateScreen(
                new ArrayList<>(Arrays.asList("edit-quest-action")), 
                director, 
                actionSlot
            ));

            return false; // continue the loop
        });

        // add new action button
        GUISlot newActionButton = new GUISlot(this.gui, 9);
        newActionButton.setLabel("New Action");
        newActionButton.setItem("LIME_DYE");
        newActionButton.onClick(() -> {
            QuestAction action = this.questStage.newAction(); // create the new action to present
            this.gui.clearSlots(); // clear to prevent duplicates
            this.execute(); // re-run to see new action in list
            System.out.println("the actions list: " + this.questStage.getActions());

            // NOTE: uncomment the following to flick over into editing the action straight away:
            // this.director.setCurrentInstance(action);
        });
        // newActionButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
        //     new ArrayList<>(Arrays.asList("edit-quest-action")), // set the previous screen 
        //     director, // set the client director
        //     newActionButton // the origin GUI slot
        // ));
    }
    
}
