package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.ChatPrompt;
import playerquests.builder.gui.function.CloseScreen;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.client.ClientDirector;

/**
 * The main dynamic GUI screen for the PlayerQuests plugin.
 * <p>
 * This screen provides options to exit the GUI, create a new quest, or view existing quests.
 * It sets up the slots with the appropriate items and actions, handling user interactions 
 * such as creating a quest through a chat prompt and navigating to different screens.
 * </p>
 */
public class Dynamicmain extends GUIDynamic {

    /**
     * Constructs a new {@code Dynamicmain} instance.
     * @param director the client director that manages the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicmain(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // ?
    }

    @Override
    protected void execute_custom() {
        GUIFrame guiFrame = this.gui.getFrame();

        guiFrame.setTitle("PlayerQuests");
        guiFrame.setSize(9);

        new GUISlot(gui, 1)
            .setItem("OAK_DOOR")
            .setLabel("Exit")
            .addFunction(
                new CloseScreen(new ArrayList<>(), director)
            );

        new GUISlot(gui, 3)
            .setItem("LIME_DYE")
            .setLabel("Create Quest")
            .onClick(() -> {
                new ChatPrompt(new ArrayList<>(Arrays.asList("Enter quest title", "none")), director)
                    .onFinish((f) -> {
                        ChatPrompt function = (ChatPrompt) f; // retrieve the function state
                        String response = function.getResponse(); // retrieve the 'ChatPrompt' response from the function state

                        // create quest from response
                        if (response == null) {
                            return;
                        }
                        QuestBuilder quest = new QuestBuilder(director).setTitle(response); // create a quest

                        // show quest editor
                        if (quest.getTitle() == "") {
                            return;
                        }
                        new UpdateScreen(new ArrayList<>(Arrays.asList("questeditor")), director) // change screen to the quest editor
                            .execute();
                    })
                    .execute();;
            });

        new GUISlot(gui, 4)
            .setItem("PAINTING")
            .setLabel("View Quests")
            .addFunction(
                new UpdateScreen(new ArrayList<>(Arrays.asList("myquests")), director)
            );
    }
}
