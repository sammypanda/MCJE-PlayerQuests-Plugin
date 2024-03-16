package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic array handling

import playerquests.builder.gui.component.GUIFrame; // describes the outer GUI frame/window
import playerquests.builder.gui.component.GUISlot; // describes a GUI button
import playerquests.builder.gui.function.ChatPrompt; // GUI taking input from chat box
import playerquests.builder.gui.function.Save; // saves data via GUI button
import playerquests.builder.gui.function.UpdateScreen; // changing the GUI screen to another
import playerquests.builder.quest.QuestBuilder; // controlling a quest
import playerquests.client.ClientDirector; // accessing the client state

/**
 * Shows a dynamic GUI used for editing a quest.
 */
// TODO: add toggling quest availability
public class Dynamicquesteditor extends GUIDynamic {

    /**
     * The quest being edited.
     */
    private QuestBuilder questBuilder;

    /**
     * Creates a dynamic GUI to edit a quest.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquesteditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // get the current client quest builder
        questBuilder = (QuestBuilder) director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        String questTitle = questBuilder.getTitle();
        GUIFrame guiFrame = gui.getFrame();

        // set the GUI title as: Edit Quest ([quest title])
        guiFrame.setTitle(
            String.format("Edit Quest %s", 
                questTitle != null ? "("+questBuilder.getTitle()+")" : null
            )
        );

        // set the GUI size
        guiFrame.setSize(9);

        // add the buttons
        new GUISlot(gui, 1) // back button
            .setItem("OAK_DOOR")
            .setLabel("Back")
            .addFunction(
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("main")), 
                    director
                )
            );

        new GUISlot(gui, 3) // set quest title button
            .setItem("ACACIA_HANGING_SIGN")
            .setLabel("Set Title")
            .addFunction(
                new ChatPrompt(
                    new ArrayList<>(Arrays.asList("Enter quest title", "quest.title")), 
                    director
                )
            );

        new GUISlot(gui, 4) // view quest stages button
            .setItem("CHEST")
            .setLabel("Quest Stages")
            .addFunction(
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("queststages")), 
                    director
                )
            );

        new GUISlot(gui, 5) // view quest NPCs button
            .setItem("ENDER_CHEST")
            .setLabel("Quest NPCs")
            .addFunction(
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("questnpcs")), 
                    director
                )
            );

        new GUISlot(gui, 9) // save quest button
            .setItem("GREEN_DYE")
            .setLabel("Save")
            .addFunction(
                new Save(
                    new ArrayList<>(Arrays.asList("quest")), 
                    director
                )
            );
    }
}
