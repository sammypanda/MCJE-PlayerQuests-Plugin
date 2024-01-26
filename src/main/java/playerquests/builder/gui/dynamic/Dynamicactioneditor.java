package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.component.QuestAction; // modifying a quest stage action
import playerquests.builder.quest.component.QuestStage; // modifying the quest stage
import playerquests.client.ClientDirector; // controlling the plugin

public class Dynamicactioneditor extends GUIDynamic {

    /**
     * The current quest action.
     */
    QuestAction action;

    /**
     * The parent quest stage for this quest action.
     */
    QuestStage stage;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // set the quest action instance
        this.action = (QuestAction) this.director.getCurrentInstance(QuestAction.class);

        // get the quest action stage parent
        this.stage = this.action.getStage();
    }

    @Override
    protected void execute_custom() {
        // set label
        if (this.stage.getEntryPoint() == this.action) { // if this action is the entry point
            this.gui.getFrame().setTitle("{QuestAction} Editor (Entry Point)");
        } else {
            this.gui.getFrame().setTitle("{QuestAction} Editor");
        }

        // the back button
        GUISlot exitButton = new GUISlot(this.gui, 1);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList("queststage")), // set the previous screen 
            director, // set the client director
            exitButton // the origin GUI slot
        ));

        // changing action type button
        GUISlot typeButton = new GUISlot(this.gui, 3);
        typeButton.setItem("FIREWORK_ROCKET");
        typeButton.setLabel("Change Type (" + this.action.getType().toString() + ")");
        typeButton.addFunction(new UpdateScreen(
            new ArrayList<>(Arrays.asList("actiontypes")),
            director,
            typeButton
        ));

        // setting current as stage entry point button
        GUISlot entrypointButton = new GUISlot(this.gui, 4);
        entrypointButton.setItem("ENDER_EYE");
        entrypointButton.setLabel("Set Action As Entry Point");
        entrypointButton.onClick(() -> {
            this.stage.setEntryPoint(this.action); // set this action as the stage entry point
            this.execute(); // re-run to see changes
        });
    }
    
}

