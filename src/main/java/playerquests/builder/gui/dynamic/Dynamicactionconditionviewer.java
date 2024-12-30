package playerquests.builder.gui.dynamic;

import java.util.List;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI used for listing in-use action conditions.
 */
public class Dynamicactionconditionviewer extends GUIDynamic {

    /**
     * The quest action data.
     */
    private ActionData actionData;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactionconditionviewer(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.actionData = (ActionData) this.director.getCurrentInstance(ActionData.class);
    }

    @Override
    protected void execute_custom() {
        // style the GUIs
        this.gui.getFrame()
            .setTitle(
                String.format("%s Conditions", this.actionData.getAction().getName()))
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

        // create condition buttons
        this.actionData.getConditions().forEach(condition -> {
            new GUISlot(gui, gui.getEmptySlot())
                .setLabel(condition.getName())
                .setDescription(List.of("fix me")) // TODO: fix conditions not saving properly, it might need to not be long.. convert from standard json consumable int to long in getEndTime/getStartTime.. or at least try it
                .setItem(Material.BARRIER)
                .onClick(() -> {
                    this.director.setCurrentInstance(condition, ActionCondition.class);
                    new UpdateScreen(List.of("actionconditioneditor"), director).execute();
                });
        });
    }
}
