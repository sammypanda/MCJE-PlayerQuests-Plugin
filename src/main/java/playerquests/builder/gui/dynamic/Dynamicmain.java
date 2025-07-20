package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

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
    protected void executeCustom() {
        GUIFrame guiFrame = this.gui.getFrame();

        guiFrame.setTitle("PlayerQuests");
        guiFrame.setSize(9);

        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Exit")
            .addFunction(
                new CloseScreen(List.of(), director)
            );

        new GUISlot(gui, 3)
            .setItem(Material.LIME_DYE)
            .setLabel("Create Quest")
            .onClick(() -> {
                new ChatPrompt(Arrays.asList("Enter quest title", "none"), director)
                    .onFinish((f) -> {
                        ChatPrompt function = (ChatPrompt) f; // retrieve the function state
                        String response = function.getResponse(); // retrieve the 'ChatPrompt' response from the function state

                        // create quest from response
                        if (response == null || response.isBlank()) {
                            return;
                        }
                        QuestBuilder questBuilder = new QuestBuilder(director).setTitle(response); // create a quest
                        questBuilder.build().save();

                        // show quest editor
                        new UpdateScreen(Arrays.asList("questeditor"), director) // change screen to the quest editor
                            .execute();
                    })
                    .execute();
            });

        new GUISlot(gui, 4)
            .setItem(Material.PAINTING)
            .setLabel("Edit Quests")
            .addFunction(
                new UpdateScreen(Arrays.asList("myquests"), director)
            );

        new GUISlot(gui, 5)
            .setItem(Material.WRITABLE_BOOK)
            .setLabel("Quest Diary")
            .addFunction(
                new UpdateScreen(List.of("questdiary"), director)
            );
    }
}
