package playerquests.builder.gui.dynamic;

import java.util.Arrays;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI for editing the options in an action.
 */
public class Dynamicoptioneditor extends GUIDynamic {

    /**
     * The current action data.
     */
    ActionData actionData;

    /**
     * Creates a dynamic GUI to edit action options.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicoptioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.actionData = (ActionData) this.director.getCurrentInstance(ActionData.class);
    }

    @Override
    protected void execute_custom() {
        this.gui.getFrame()
            .setTitle(
                String.format("%s Option Editor", this.actionData.getID()))
            .setSize(18);

        // the back button
        new GUISlot(this.gui, 1)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .addFunction(new UpdateScreen( // set function as 'UpdateScreen'
                Arrays.asList(this.previousScreen), // set the previous screen 
                director // set the client director
            ));

        // summon option buttons
        this.actionData.getOptions().forEach(option -> {
            // create the slot to edit options
            option.createSlot(this, this.gui, this.gui.getEmptySlot(), this.director);
        });
    }
    
}
