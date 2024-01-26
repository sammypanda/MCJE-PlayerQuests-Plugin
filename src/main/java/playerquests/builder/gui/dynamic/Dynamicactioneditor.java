package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.component.QuestAction; // modifying a quest stage action
import playerquests.client.ClientDirector; // controlling the plugin

public class Dynamicactioneditor extends GUIDynamic {

    /**
     * The current quest action
     */
    QuestAction questAction;

    /**
     * Creates a dynamic GUI to edit a quest stage.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // set the quest action instance
        this.questAction = (QuestAction) this.director.getCurrentInstance(QuestAction.class);
    }

    @Override
    protected void execute_custom() {
        this.gui.getFrame().setTitle("{QuestAction} Editor");

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
        typeButton.setLabel("Change Type");
        typeButton.addFunction(new UpdateScreen(
            new ArrayList<>(Arrays.asList("actiontypes")),
            director,
            typeButton
        ));
    }
    
}

