package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.List; // generic list type
import java.util.stream.Collectors; // used to turn a stream to a list
import java.util.stream.IntStream; // fills slots procedually

import playerquests.builder.gui.component.GUISlot; // creating each quest button / other buttons
import playerquests.builder.gui.function.UpdateScreen; // used to go back to other screens
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
     * the position of the last slot put on a page
     */
    private Integer lastBuiltSlot = 0; // initiated as the first slot to build

    /**
     * maximum auto-generated slots per page
     */
    private Integer slotsPerPage = 36;

    /**
     * Creates a dynamic GUI with a list of 'my quests'.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactiontypes(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    /**
     * Setting important values for myquests Dynamic GUI.
     * <ul>
     * <li>Gets list of all the quest templates
     *   <ul>
     *   <li>owned by the current player (or no-one)
     *   </ul>
     * <li>Creates and opens the GUI
     * <li>Instigates pagination/page generation
     * </ul>
     */
    public void setUp_custom() {
        // get the list of all actions
        this.actionTypes = ActionType.allActionTypes();

        // modify the new GUI to show the quests in
        this.gui.getFrame().setSize(45);
    }

    /**
     * Operations to run on each page of the myquests Dynamic GUI.
     * <ul>
     * <li>Runs the setup once
     * <li>Filters the templates to show on each page
     * <li>Generates and opens/redraws the screen pages
     * </ul>
     */
    @Override
    public void execute_custom() {
        // filter out the templates (pagination)
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
     * @param remainingTemplates the quest templates to insert
     */
    private void generatePage(ArrayList<String> remainingTemplates) {
        Integer slotCount = remainingTemplates.size() >= this.slotsPerPage // if there are more remaining templates than the default slot limit
        ? this.slotsPerPage // use the default slot limit
        : remainingTemplates.size(); // otherwise use the number of remaining templates

        IntStream.range(0, slotCount).anyMatch(index -> { // only built 42 slots
            if (remainingTemplates.isEmpty() || remainingTemplates.get(index) == null) {
                return true; // exit the loop
            }

            String quest = remainingTemplates.get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot questSlot = new GUISlot(this.gui, nextEmptySlot);
            questSlot.setItem("REDSTONE");
            questSlot.setLabel(quest.split("_")[0]);

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
        if (this.actionTypes.size() != remainingTemplates.size()) { // if the remaining is the same as all 
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
        if (this.slotsPerPage <= remainingTemplates.size()) { // if the remaining is bigger or the same as the default slots per page
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