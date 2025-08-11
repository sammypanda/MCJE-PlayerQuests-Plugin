package playerquests.builder.gui.dynamic;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.ConditionType;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

/**
 * Shows a GUI used for listing action conditions.
 * They are also editable if ActionData is available.
 */
public class Dynamicactionconditionselector extends GUIDynamic {

    /**
     * The quest action data.
     */
    private ActionData actionData;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactionconditionselector(ClientDirector director, String previousScreen) {
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
            .setTitle("Select A Condition")
            .setSize(9);

        // create back button
        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .onClick(() -> new UpdateScreen(List.of(this.previousScreen), director).execute());

        // create divider
        new GUISlot(gui, 2)
            .setItem(Material.GRAY_STAINED_GLASS_PANE);

        // define logic for summoning options buttons
        Consumer<ConditionType> createOptionButtons = conditionClass -> {
            ActionCondition condition;
            
            // get an instance of the condition
            try {
                condition = conditionClass.getConditionClass().getDeclaredConstructor(ActionData.class).newInstance(this.actionData);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                return;
            }

            // create the slot to edit condition
            condition.createSlot(this, this.gui, this.gui.getEmptySlot(), this.director)
                .setDescription(condition.getDescription()) // override the description with the description of the condition, regardless
                .onClick(() -> {
                    this.actionData.addCondition(condition); // add the condition to the action
                    this.director.setCurrentInstance(condition, ActionCondition.class); // set the condition to edit
                    new UpdateScreen(List.of("actionconditioneditor"), director).execute();
                });
        };

        // summon option buttons
        Arrays.stream(ConditionType.values()).forEach(createOptionButtons);
    }
}
