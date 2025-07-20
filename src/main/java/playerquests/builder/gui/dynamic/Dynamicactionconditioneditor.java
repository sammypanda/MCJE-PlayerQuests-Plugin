package playerquests.builder.gui.dynamic;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageType;

/**
 * Shows a GUI used for listing action conditions.
 * They are also editable if ActionData is available.
 */
public class Dynamicactionconditioneditor extends GUIDynamic {

    /**
     * The quest action data.
     */
    private ActionData actionData;

    /**
     * The action condition to edit.
     */
    private ActionCondition condition;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactionconditioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.actionData = (ActionData) this.director.getCurrentInstance(ActionData.class);
        this.condition = (ActionCondition) this.director.getCurrentInstance(ActionCondition.class);
    }

    @Override
    protected void executeCustom() {
        // style the GUIs
        this.gui.getFrame()
            .setTitle(
                String.format("%s Condition Editor", this.condition.getName()))
            .setSize(9);

        // create back button
        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .onClick(() -> {
                new UpdateScreen(List.of(this.previousScreen), director).execute();
            });

        // create divider
        new GUISlot(gui, 2)
            .setItem(Material.GRAY_STAINED_GLASS_PANE);

        // ask condition for the rest of the editor
        this.condition.createEditorGUI(this, gui, director);

        // create delete button in last slot
        new GUISlot(gui, this.gui.getFrame().getSize())
            .setItem(Material.RED_DYE)
            .setLabel("Delete Condition")
            .onClick(() -> {
                Optional<String> removalErr = this.actionData.removeCondition(this.condition);

                if (removalErr.isEmpty()) {
                    // go to previous screen if removing condition was successful
                    new UpdateScreen(List.of(this.previousScreen), director).execute();
                    return;
                }

                // send the warning message
                ChatUtils.message(removalErr.get())
                    .player(this.director.getPlayer())
                    .type(MessageType.WARN)
                    .style(MessageStyle.PRETTY)
                    .send();
            });
    }
}
