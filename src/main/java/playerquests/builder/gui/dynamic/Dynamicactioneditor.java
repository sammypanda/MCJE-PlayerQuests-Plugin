package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

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
                           .setSize(9);
        
        // the back button
        new GUISlot(this.gui, 1)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .addFunction(new UpdateScreen( // set function as 'UpdateScreen'
                Arrays.asList(this.previousScreen), // set the previous screen 
                director // set the client director
            ));

        // select next actions button
        new GUISlot(this.gui, 2)
            .setItem("HOPPER")
            .setLabel("Next Actions")
            .setDescription(List.of("Select actions to come after this one."))
            .onClick(() -> {
                this.director.setCurrentInstance(action); // set this action as the one to edit
                new UpdateScreen(Arrays.asList("nextactioneditor"), director).execute(); // open 'next action editor' screen
            });

        // change action type button
        new GUISlot(this.gui, 3)
            .setItem(Material.FIREWORK_ROCKET)
            .setLabel("Change action type")
            .setDescription(List.of(
                String.format("Currently: %s", action.getName())
            ))
            .onClick(() -> {
                // TODO: implement action screen
            });

        // options editor button
        new GUISlot(this.gui, 4)
            .setItem(Material.STONE_BUTTON)
            .setLabel("Edit action options")
            .onClick(() -> {
                this.director.setCurrentInstance(this.action.getData());
                new UpdateScreen(List.of("optioneditor"), director).execute();
            });
    }
}
