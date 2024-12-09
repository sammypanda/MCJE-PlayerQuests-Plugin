package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;

/**
 * GUI for selecting action types.
 */
public class Dynamicactiontypeselector extends GUIDynamic {

    /**
     * The action to change the type of.
     */
    QuestAction action;

    /**
     * All the quest action types.
     */
    List<QuestAction> actionTypes;

    /**
     * The stage the action belongs to.
     */
    QuestStage stage;

    /**
     * Constructor for a GUI that shows all known action types.
     * @param director the client director that handles the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicactiontypeselector(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.action = (QuestAction) this.director.getCurrentInstance(QuestAction.class);
        this.stage = this.action.getStage();

        // hmm actions should always have stages!
        if (this.stage == null) {
            throw new RuntimeException("When changing action types, an action had a null stage.");
        }

        // get all annotated action types
        this.actionTypes = QuestAction.getAllTypes()
            .stream()
            .map(actionClass -> {
                try {
                    // create QuestAction instance from class type
                    return actionClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(Objects::nonNull) // Filter out any nulls resulting from exceptions
            .collect(Collectors.toList());
    }

    @Override
    protected void execute_custom() {
        int minimumSize = Math.clamp(
            Math.round(
                (Integer.valueOf(this.actionTypes.size()) / 9) * 9), 
            9, 
            54);

        // set the GUI title
        this.gui.getFrame()
            .setTitle(String.format("%s",
                "Available Action Types"))
            .setSize(minimumSize);

        // add back button
        new GUISlot(this.gui, 1)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .addFunction(new UpdateScreen( // set function as 'UpdateScreen'
                Arrays.asList(this.previousScreen), // set the previous screen 
                director // set the client director
            ));

        // create the action selection buttons
        this.actionTypes.forEach(action -> {
            GUISlot slot = action.createSlot(this.gui, this.gui.getEmptySlot());

            if (action.getClass().isAssignableFrom(this.action.getClass())) {
                slot
                    .setLabel(String.format("%s (Selected)", slot.getLabel()))
                    .setItem(Material.FIREWORK_ROCKET);
            }

            // functionality for changing the action type
            slot.onClick(() -> {
                this.changeType(this.action, action);
            });
        });                
    }

    /**
     * The logic to actually change the type of the current action.
     * @param oldAction the action being edited.
     * @param newAction the action to replace it with, that has the new type.
     */
    private void changeType(QuestAction oldAction, QuestAction newAction) {
        // replace the action in the stage
        this.action = this.stage.replaceAction(oldAction, newAction);

        // show the replaced action here
        this.director.setCurrentInstance(newAction, QuestAction.class); // update in director
        this.refresh(); // update the screen
    }
    
}
