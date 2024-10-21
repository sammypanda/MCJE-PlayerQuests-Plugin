package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
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
            action.createSlot(this.gui, this.gui.getEmptySlot());
        });
                
    }
    
}
