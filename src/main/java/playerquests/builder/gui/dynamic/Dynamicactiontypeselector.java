package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Material;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.exception.MissingStageException;

/**
 * GUI for selecting action types.
 */
public class Dynamicactiontypeselector extends GUIDynamic {

    /**
     * The action to change the type of.
     */
    QuestAction<?,?> action;

    /**
     * All the quest action types.
     */
    List<QuestAction<?,?>> actionTypes;

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
    protected void setupCustom() {
        this.action = (QuestAction<?,?>) this.director.getCurrentInstance(QuestAction.class);
        this.stage = this.action.getStage();

        // hmm actions should always have stages!
        if (this.stage == null) {
            throw new MissingStageException(
                "When changing action types, an action had a null stage.", 
                new IllegalStateException("An action is missing a stage. All actions should belong to a stage.")
            );
        }

        // get all annotated action types
        this.actionTypes = QuestAction.getAllTypes()
            .stream()
            .<QuestAction<?, ?>>flatMap(actionClass -> {  // explicit type hint
                try {
                    // create QuestAction instance from class type
                    return Stream.of(actionClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    ChatUtils.message(e.getMessage())
                        .target(MessageTarget.CONSOLE)
                        .type(MessageType.ERROR)
                        .send();
                    return Stream.empty();
                }
            })
            .toList();
    }

    @Override
    protected void executeCustom() {
        int minimumSize = Math.clamp(
            (Integer.valueOf(this.actionTypes.size()) / 9) * 9, 
            9, 
            54
        );

        // set the GUI title
        this.gui.getFrame()
            .setTitle(String.format("%s",
                "Available Action Types"))
            .setSize(minimumSize);

        // add back button
        new GUISlot(this.gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .addFunction(new UpdateScreen( // set function as 'UpdateScreen'
                Arrays.asList(this.previousScreen), // set the previous screen 
                director // set the client director
            ));

        // create the action selection buttons
        this.actionTypes.forEach(actionType -> {
            GUISlot slot = actionType.createSlot(this.gui, this.gui.getEmptySlot());

            if (actionType.getClass().isAssignableFrom(this.action.getClass())) {
                slot
                    .setLabel(Component.join(JoinConfiguration.spaces(), slot.getLabel(), Component.text("(Selected)")))
                    .setItem(Material.FIREWORK_ROCKET);
            }

            // functionality for changing the action type
            slot.onClick(() -> {
                this.changeType(this.action, actionType);
            });
        });                
    }

    /**
     * The logic to actually change the type of the current action.
     * @param oldAction the action being edited.
     * @param newAction the action to replace it with, that has the new type.
     */
    private void changeType(QuestAction<?,?> oldAction, QuestAction<?,?> newAction) {
        // replace the action in the stage
        this.action = this.stage.replaceAction(oldAction, newAction);

        // show the replaced action here
        this.director.setCurrentInstance(newAction, QuestAction.class); // update in director
        this.refresh(); // update the screen
    }
    
}
