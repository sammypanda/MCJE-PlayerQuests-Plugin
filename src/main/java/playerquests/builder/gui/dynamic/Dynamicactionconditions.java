package playerquests.builder.gui.dynamic;

import java.util.List;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI used for listing action conditions.
 * They are also editable if ActionData is available.
 */
public class Dynamicactionconditions extends GUIDynamic {

    /**
     * The quest action data.
     */
    private ActionData actionData;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactionconditions(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        this.actionData = (ActionData) this.director.getCurrentInstance(ActionData.class);
    }

    @Override
    protected void executeCustom() {
        // style the GUIs
        this.gui.getFrame()
            .setTitle(
                String.format("%s Conditions", this.actionData.getAction().getID()))
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

        // view conditions button
        new GUISlot(gui, 3)
            .setLabel("View")
            .setItem(Material.CHEST)
            .onClick(() -> {
                this.director.setCurrentInstance(this.actionData);
                new UpdateScreen(List.of("actionconditionviewer"), director).execute();
            });

        // add new condition button
        new GUISlot(gui, 4)
            .setLabel("Create New")
            .setItem(Material.BLUE_DYE)
            .onClick(() -> {
                this.director.setCurrentInstance(this.actionData);
                new UpdateScreen(List.of("actionconditionselector"), director).execute();
            });
    }
}
