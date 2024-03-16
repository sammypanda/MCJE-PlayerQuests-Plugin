package playerquests.builder.gui.dynamic;

import java.io.File; // retrieve the template files
import java.io.IOException; // thrown when the quest templates dir cannot be found
import java.nio.file.Files; // manage multiple template files
import java.util.ArrayList; // stores the quests this player owns
import java.util.Arrays; // working with literal arrays
import java.util.List; // store temporary lists (like: string splitting)
import java.util.stream.Collectors; // used to turn a stream to a list
import java.util.stream.IntStream; // fills slots procedually

import playerquests.Core; // fetching Singletons (like: Plugin)
import playerquests.builder.gui.component.GUISlot; // creating each quest button / other buttons
import playerquests.builder.gui.function.UpdateScreen;// used to go back to the 'main' screen
import playerquests.client.ClientDirector; // for controlling the plugin

/**
 * Shows a dynamic GUI listing the players quests.
 */
public class Dynamicmyquests extends GUIDynamic {

    /**
     * the GUI title
     */
    private String guiTitle = "My Quests";

    /**
     * The quest templates belonging to no-one or this player
     */
    private ArrayList<String> myquestTemplates = new ArrayList<>();

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
    public Dynamicmyquests(ClientDirector director, String previousScreen) {
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
        // get the list of all quest templates with owner: null or owner: player UUID
        File questTemplatesDir = new File(Core.getPlugin().getDataFolder(), "/quest/templates");

        // modify the new GUI to show the quests in
        this.gui.getFrame().setSize(45);

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
                    this.director.getPlayer().getUniqueId().toString()
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
        ArrayList<String> remainingTemplates = (ArrayList<String>) this.myquestTemplates
            .stream()
            .filter(i -> this.myquestTemplates.indexOf(i) > this.lastBuiltSlot - 1)
            .collect(Collectors.toList());

        // automatically create the page of slots/options
        this.generatePage(remainingTemplates);

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
            questSlot.setItem("BOOK");
            questSlot.setLabel(quest.split("_")[0]);

            return false; // continue the loop
        });

        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(this.gui, 37);
        exitButton.setLabel("Exit");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList(this.previousScreen)), // set the previous screen 
            director // set the client director
        ));

        // when the back button is pressed
        GUISlot backButton = new GUISlot(this.gui, 44);
        if (this.myquestTemplates.size() != remainingTemplates.size()) { // if the remaining is the same as all 
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
