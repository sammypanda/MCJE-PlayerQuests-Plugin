package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.stream.IntStream; // functional loops

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

        // set the gui size
        this.gui.getFrame().setSize(18);

        // the back button
        GUISlot exitButton = new GUISlot(this.gui, 10);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList("queststage")), // set the previous screen 
            director, // set the client director
            exitButton // the origin GUI slot
        ));

        // changing action type button
        GUISlot typeButton = new GUISlot(this.gui, 1);
        typeButton.setItem("FIREWORK_ROCKET");
        typeButton.setLabel("Change Type (" + this.action.getType().toString() + ")");
        typeButton.addFunction(new UpdateScreen(
            new ArrayList<>(Arrays.asList("actiontypes")),
            director,
            typeButton
        ));

        // setting current as stage entry point button
        GUISlot entrypointButton = new GUISlot(this.gui, 2);
        entrypointButton.setItem("ENDER_EYE");
        entrypointButton.setLabel("Set Action As Entry Point");
        entrypointButton.onClick(() -> {
            this.stage.setEntryPoint(this.action); // set this action as the stage entry point
            this.execute(); // re-run to see changes
        });

        IntStream.of(3, 12).forEach((int value) -> {
            GUISlot dividerSlot = new GUISlot(this.gui, value);
            dividerSlot.setItem("BLACK_STAINED_GLASS_PANE");
            dividerSlot.setLabel(" ");
        });

        // TODO: pagination for action params (action options)

        // TODO: replacing entry point setter button with action name changer button

        // TODO: conditional back and forward buttons in slots 10,11 for params list

        // TODO: divider on the right side of the GUI for setting: as entry point, next, current and prev connections
    }
    
}
