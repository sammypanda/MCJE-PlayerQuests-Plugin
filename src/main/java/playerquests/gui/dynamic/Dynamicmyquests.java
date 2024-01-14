package playerquests.gui.dynamic;

import java.io.File; // retrieve the template files
import java.io.IOException; // thrown when the quest templates dir cannot be found
import java.nio.file.Files; // manage multiple template files
import java.util.ArrayList; // stores the quests this player owns
import java.util.Arrays; // working with literal arrays
import java.util.List; // store temporary lists (like: string splitting)
import java.util.stream.Collectors; // used to turn a stream to a list
import java.util.stream.IntStream; // creates 77 fake quest templates to fill slots

import playerquests.Core; // fetching Singletons (like: Plugin)
import playerquests.gui.GUI; // creating the Dynamic GUI on the screen
import playerquests.gui.GUISlot; // creating each quest button / other buttons
import playerquests.gui.function.UpdateScreenFile; // used to go back to the 'main' screen

/**
 * Shows the list of quests associated with this player.
 * <p>
 * Includes quests associated with null.
 */
public class Dynamicmyquests extends GUIDynamic {

    /**
     * if the setup has been ran
     */
    private Boolean wasSetUp = false;

    /**
     * the GUI instance
     */
    private GUI myquestsGUI;

    /**
     * the quest templates belonging to no-one or this player
     */
    private ArrayList<String> myquestTemplates;

    /**
     * the position of the last slot put on a page
     */
    private Integer lastBuiltSlot = 0; // initiated as the first slot to build

    /**
     * maximum auto-generated slots per page
     */
    private Integer slotsPerPage = 36;

    /**
     * Setting important values for myquests Dynamic GUI.
     * <ul>
     * <li>Gets list of all the quest templates
     *   <ul>
     *   <li>owned by the current player (or no-one)
     *   </ul>
     * <li>Creates and opens the GUI
     * <li>Adds 77 fake objects TODO: remove
     * <li>Instigates pagination/page generation
     * </ul>
     */
    private void setUp() {
        this.wasSetUp = true; // mark this instance has been setup

        // exit early if the Plugin is not instantiated
        // -- this is because the process relies on plugin data (files in the server /plugins dir)
        if (Core.getPlugin() == null) {
            throw new RuntimeException("<Could not execute myquests dynamic GUI without final Plugin>");
        }

        // get the list of all quest templates with owner: null or owner: player UUID
        File questTemplatesDir = new File(Core.getPlugin().getDataFolder(), "/quest/templates");
        this.myquestTemplates = new ArrayList<>(); // the quest templates this player owns
        
        // create the new GUI to show the quests in
        this.myquestsGUI = new GUI(this.player);
        this.myquestsGUI.setTitle("My Quests");
        this.myquestsGUI.setSize(45);

        // Testing
        this.myquestTemplates.addAll(IntStream.range(0, 77).mapToObj(i -> "value"+i).collect(Collectors.toList()));

        try { // to access the quest templates dir
            Files.walk(questTemplatesDir.toPath()).forEach(questTemplateFile -> { // get all the quest templates
                // skip if is a directory
                if (questTemplateFile.toFile().isDirectory()) { return; }

                // the quest template filename without .json
                String questTemplateName = questTemplateFile.toString()
                    .replace(".json", "")
                    .split("/templates/")[1];

                // divide up to uncover the concatenated data in the quest template filename
                List<String> questTemplateNameFragments = Arrays.asList(
                    questTemplateName.split("_")
                );

                // list of acceptable quest owners
                List<String> questOwnerList = Arrays.asList(
                    "null", 
                    this.player.getUniqueId().toString()
                );

                // set the quest owner to match best to the template filename (null or the user id) 
                String questOwner; // can be either null or the player UUID
                switch (questTemplateNameFragments.size()) {
                    case 2:
                        questOwner = questTemplateNameFragments.get(1);
                        break;
                    default:
                        questOwner = "null";
                        break;
                }

                // skip if current player is not the owner of the quest template
                if (!questOwnerList.contains(questOwner)) { return; }
                
                // add the quest to our list
                this.myquestTemplates.add(questTemplateName);
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not access the " + questTemplatesDir.toString() + " path. ", e);
        }

        // open the GUI for the first time
        this.myquestsGUI.open();

        // send back to the execute() body to continue
        this.execute();
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
    public void execute() {
        // run setup on first time
        if (!this.wasSetUp) {
            this.setUp();
            return;
        }

        // filter out the templates (pagination)
        ArrayList<String> remainingTemplates = (ArrayList<String>) this.myquestTemplates
            .stream()
            .filter(i -> this.myquestTemplates.indexOf(i) > this.lastBuiltSlot - 1)
            .collect(Collectors.toList());
        
        // automatically create the page of slots/options
        generatePage(this.myquestsGUI, remainingTemplates);

        if (!this.wasSetUp) {
            // open the GUI for the first time
            this.myquestsGUI.open();
        } else {
            // redraw the GUI
            this.myquestsGUI.redraw();
        }
    }

    private void generatePage(GUI theGUI, ArrayList<String> remainingTemplates) {
        Integer slotCount = remainingTemplates.size() >= this.slotsPerPage // if there are more remaining templates than the default slot limit
        ? this.slotsPerPage // use the default slot limit
        : remainingTemplates.size(); // otherwise use the number of remaining templates

        IntStream.range(0, slotCount).anyMatch(index -> { // only built 42 slots
            if (remainingTemplates.isEmpty() || remainingTemplates.get(index) == null) {
                return true; // exit the loop
            }

            String quest = remainingTemplates.get(index);
            Integer nextEmptySlot = theGUI.getEmptySlot();
            GUISlot questSlot = new GUISlot(theGUI, nextEmptySlot);
            questSlot.setItem("BOOK");
            questSlot.setLabel(quest);

            return false; // continue the loop
        });

        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(theGUI, 37);
        exitButton.setLabel("Exit");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreenFile(), new ArrayList<>(Arrays.asList("main"))); // set function as 'UpdateScreenFile'

        // when the back button is pressed
        GUISlot backButton = new GUISlot(theGUI, 44);
        if (this.myquestTemplates.size() != remainingTemplates.size()) { // if the remaining is the same as all 
            backButton.setLabel("Back");
            backButton.setItem("ORANGE_STAINED_GLASS_PANE");
            backButton.onClick(() -> {
                this.myquestsGUI.clearSlots(); // unset the old slots
                this.lastBuiltSlot = this.lastBuiltSlot - this.slotsPerPage; // put slots for the remainingSlots
                this.execute();
            });
        }

        // when the next button is pressed
        GUISlot nextButton = new GUISlot(theGUI, 45);
        if (this.slotsPerPage <= remainingTemplates.size()) { // if the remaining is bigger or the same as the default slots per page
            nextButton.setLabel("Next");
            nextButton.setItem("GREEN_STAINED_GLASS_PANE");
            nextButton.onClick(() -> {
                this.myquestsGUI.clearSlots(); // unset the old slots
                this.lastBuiltSlot = this.lastBuiltSlot + this.slotsPerPage; // take slots for the remainingSlots
                this.execute();
            });
        }
    }
}
