package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.component.QuestStage; // modifying the quest stage
import playerquests.client.ClientDirector; // controlling the plugin

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
    }
    
}
