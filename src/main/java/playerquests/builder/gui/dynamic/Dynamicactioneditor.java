package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI used for editing a quest action.
 */
public class Dynamicactioneditor extends GUIDynamic {

    /**
     * The action being edited.
     */
    private QuestAction action;

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
        this.action = (QuestAction) this.director.getCurrentInstance(QuestAction.class);
    }

    @Override
    protected void execute_custom() {
        // set frame title/style
        this.gui.getFrame().setTitle(String.format("%s Editor", this.action.getID()))
                           .setSize(18);
        
        // the back button
        new GUISlot(this.gui, 10)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .addFunction(new UpdateScreen( // set function as 'UpdateScreen'
                Arrays.asList(this.previousScreen), // set the previous screen 
                director // set the client director
            ));

        // select next actions button
        new GUISlot(this.gui, 1)
            .setItem("HOPPER")
            .setLabel("Next Actions")
            .setDescription(List.of("Select actions to come after this one."))
            .onClick(() -> {
                this.director.setCurrentInstance(action); // set this action as the one to edit
                new UpdateScreen(Arrays.asList("nextactioneditor"), director).execute(); // open 'next action editor' screen
            });

        // left side dividers
        new GUISlot(this.gui, 2)
            .setItem("BLACK_STAINED_GLASS_PANE");

        new GUISlot(this.gui, 11)
            .setItem("BLACK_STAINED_GLASS_PANE");

        // summon option buttons
        this.action.getOptions().forEach(option -> {
            option.createSlot(this.gui, this.gui.getEmptySlot());
        });
    }
}
