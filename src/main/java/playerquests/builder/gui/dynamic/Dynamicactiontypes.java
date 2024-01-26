package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.List; // generic list type
import java.util.stream.Collectors; // used to turn a stream to a list
import java.util.stream.IntStream; // fills slots procedually

import playerquests.builder.gui.component.GUISlot; // creating each quest button / other buttons
import playerquests.builder.gui.function.UpdateScreen; // used to go back to other screens
import playerquests.builder.quest.component.QuestAction; // modifying a quest stage action
import playerquests.builder.quest.component.action.type.ActionType; // modifying a quest stage action
import playerquests.client.ClientDirector; // for controlling the plugin

/**
 * Shows a dynamic GUI listing the possible quest actions.
 */
public class Dynamicactiontypes extends GUIDynamic {

    /**
     * the GUI title.
     */
    private String guiTitle = "Available Action Types";

    /**
     * The action types available for the action.
     */
    private List<String> actionTypes = new ArrayList<>();

    /**
     * The quest action this action type list is born from
     */
    private QuestAction action;

    /**
     * the position of the last slot put on a page
     */
    private Integer lastBuiltSlot = 0; // initiated as the first slot to build

    /**
     * maximum auto-generated slots per page
     */
    private Integer slotsPerPage = 36;

    /**
     * Creates a dynamic GUI with a list of 'quest actions'.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactiontypes(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    /**
     * Setting important values for actiontypes Dynamic GUI.
     * <ul>
     * <li>Gets list of all the quest actions
     * <li>Instigates pagination/page generation
     * </ul>
     */
    public void setUp_custom() {
        // get the list of all actions
        this.actionTypes = ActionType.allActionTypes();

        // get the current quest stage
        this.action = (QuestAction) this.director.getCurrentInstance(QuestAction.class);

        // modify the new GUI to show the quests in
        this.gui.getFrame().setSize(45);
    }

    /**
     * Operations to run on each page of the actiontypes Dynamic GUI.
     * <ul>
     * <li>Runs the setup once
     * <li>Filters the action types to show on each page
     * </ul>
     */
    @Override
    public void execute_custom() {
        // filter out the types (pagination)
        ArrayList<String> actionTypes_remaining = (ArrayList<String>) this.actionTypes
            .stream()
            .filter(i -> this.actionTypes.indexOf(i) > this.lastBuiltSlot - 1)
            .collect(Collectors.toList());

        // automatically create the page of slots/options
        this.generatePage(actionTypes_remaining);

        // determine the page number
        Integer pageNumber = this.lastBuiltSlot/this.slotsPerPage + 1;

        // set the GUI title (w/ page number feature)
        this.gui.getFrame().setTitle(String.format("%s%s",
            this.guiTitle, // set the default title
            pageNumber != 1 ? " [Page " + pageNumber + "]" : "" // add page number when not page one
        ));
    }

    /**
     * Replaces existing screen with a page listing quests with back/forward/exit buttons.
     * @param actionTypes_remaining the quest actions to insert in the page
     */
    private void generatePage(ArrayList<String> actionTypes_remaining) {
        Integer slotCount = actionTypes_remaining.size() >= this.slotsPerPage // if there are more remaining types than the default slot limit
        ? this.slotsPerPage // use the default slot limit
        : actionTypes_remaining.size(); // otherwise use the number of remaining action types

        IntStream.range(0, slotCount).anyMatch(index -> { // only built 42 slots
            if (actionTypes_remaining.isEmpty() || actionTypes_remaining.get(index) == null) {
                return true; // exit the loop
            }

            // get the type of current item in actions list
            String type = actionTypes_remaining.get(index);

            // set the slot this item goes into
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot typeButton = new GUISlot(this.gui, nextEmptySlot);

            // set currently selected item
            if (this.action.getType() == type) { // compare action type being modified with action type in this loop
                typeButton.setItem("COAL");
                typeButton.setLabel(type + " (Selected)");
            } else {
                typeButton.setItem("REDSTONE");
                typeButton.setLabel(type);
            }
            
            return false; // continue the loop
        });

        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(this.gui, 37);
        exitButton.setLabel("Exit");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList(this.previousScreen)), // set the previous screen 
            director, // set the client director
            exitButton // the origin GUI slot
        ));

        // when the back button is pressed
        GUISlot backButton = new GUISlot(this.gui, 44);
        if (this.actionTypes.size() != actionTypes_remaining.size()) { // if the remaining is the same as all 
            backButton.setLabel("Back");
            backButton.setItem("ORANGE_STAINED_GLASS_PANE");
            backButton.onClick(() -> {
                this.gui.clearSlots(); // unset the old slots
                this.lastBuiltSlot = this.lastBuiltSlot - this.slotsPerPage; // put slots for the remainingSlots
                this.execute();
            });
        }

        // when the next button is pressed
        GUISlot nextButton = new GUISlot(this.gui, 45);
        if (this.slotsPerPage <= actionTypes_remaining.size()) { // if the remaining is bigger or the same as the default slots per page
            nextButton.setLabel("Next");
            nextButton.setItem("GREEN_STAINED_GLASS_PANE");
            nextButton.onClick(() -> {
                this.gui.clearSlots(); // unset the old slots
                this.lastBuiltSlot = this.lastBuiltSlot + this.slotsPerPage; // take slots for the remainingSlots
                this.execute();
            });
        }
    }
}