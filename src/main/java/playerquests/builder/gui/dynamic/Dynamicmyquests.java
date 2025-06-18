package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // stores the quests this player owns
import java.util.Arrays; // working with literal arrays
import java.util.LinkedHashSet; // hash set, but with order :D
import java.util.List;
import java.util.Set; // generic set type
import java.util.UUID; // for working with in-game player IDs
import java.util.concurrent.CompletableFuture; // async methods
import java.util.stream.Collectors; // used to turn a stream to a list
import java.util.stream.IntStream; // fills slots procedually

import org.bukkit.Bukkit;
import org.bukkit.Material;

import playerquests.Core; // fetching Singletons (like: Plugin)
import playerquests.builder.gui.component.GUISlot; // creating each quest button / other buttons
import playerquests.builder.gui.function.UpdateScreen;// used to go back to the 'main' screen
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.product.Quest; // a quest product used to play and track quests
import playerquests.utility.singleton.QuestRegistry; // centralised hub backend for quests/questers

/**
 * Shows a dynamic GUI listing the players quests.
 */
public class Dynamicmyquests extends GUIDynamic {

    /**
     * the GUI title
     */
    private String guiTitle = "My Quests";

    /**
     * The quests belonging to no-one or this player
     */
    private Set<String> myQuests = new LinkedHashSet<>();

    /**
     * the position of the last slot put on a page
     */
    private Integer lastBuiltSlot = 0; // initiated as the first slot to build

    /**
     * maximum auto-generated slots per page
     */
    private Integer slotsPerPage = 36;

    /**
     * count of malformed quests
     */
    private Integer invalidQuests = 0;

    /**
     * indicate when the quests can start to load in
     */
    private Boolean myquestLoaded = false;

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
     * <li>Gets list of all the quests
     *   <ul>
     *   <li>owned by the current player (or no-one)
     *   </ul>
     * <li>Creates and opens the GUI
     * <li>Instigates pagination/page generation
     * </ul>
     */
    public void setUp_custom() {
        // get list of quests
        if (!this.myquestLoaded) {
            CompletableFuture.runAsync(() -> {
                // get quests from database
                this.myQuests.addAll(Core.getQuestRegistry().getAllQuests().keySet());

            // do when quests have been gotten
            }).thenRun(() -> {
                this.myquestLoaded = true;

                // return to the main thread
                Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                    // refresh the screen/remove the 'load'/show the quests; continue
                    this.gui.clearSlots(); // fresh!
                    this.execute();
                });
            });

            return;
        }
    }

    /**
     * Operations to run on each page of the myquests Dynamic GUI.
     * <ul>
     * <li>Runs the setup once
     * <li>Filters the quests to show on each page
     * <li>Generates and opens/redraws the screen pages
     * </ul>
     */
    @Override
    public void execute_custom() {
        // determine the page number
        Integer pageNumber = this.lastBuiltSlot/this.slotsPerPage + 1;

        // set the GUI title (w/ page number feature)
        this.gui.getFrame().setTitle(String.format("%s%s%s",
            this.guiTitle, // set the default title
            pageNumber != 1 ? " [Page " + pageNumber + "]" : "", // add page number when not page one
            !this.myquestLoaded ? " (Loading)" : "" // indicate that the page is loading
        ));


        // false button for amount of invalid quests (malformed json or otherwise malformed data)
        new GUISlot(this.gui, 43)
            .setItem(Material.RED_STAINED_GLASS)
            .setLabel(String.format("Unreadable Quests: %s", 
                this.invalidQuests >= 64 ? "(More than 64)" : this.invalidQuests
            ))
            .setDescription(List.of("An unreadable quest, is a quest that is corrupt/malformed/incorrect."))
            .setCount(this.invalidQuests);

        // modify the new GUI to show the quests in
        this.gui.getFrame().setSize(45);

        // automatically create the page of slots/options (when ready)
        if (this.myquestLoaded) {
            // filter out the quests (pagination)
            ArrayList<String> remainingQuests = (ArrayList<String>) this.myQuests
                .stream()
                .filter(i -> new ArrayList<String>(this.myQuests).indexOf(i) > this.lastBuiltSlot - 1)
                .collect(Collectors.toList());

            // generate the paginated slots
            this.generatePage(remainingQuests);
        }
    }

    /**
     * Replaces existing screen with a page listing quests with back/forward/exit buttons.
     * @param remainingQuests the quests to insert
     */
    private void generatePage(ArrayList<String> remainingQuests) {
        UUID playerUUID = this.director.getPlayer().getUniqueId();

        // when the exit button is pressed
        GUISlot exitButton = new GUISlot(this.gui, 37);
        exitButton.setLabel("Back");
        exitButton.setItem(Material.OAK_DOOR);
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            Arrays.asList(this.previousScreen), // set the previous screen 
            director // set the client director
        ));

        // when the back button is pressed
        GUISlot backButton = new GUISlot(this.gui, 44);
        if (this.myQuests.size() != remainingQuests.size()) { // if the remaining is the same as all 
            backButton.setLabel("Back");
            backButton.setItem(Material.ORANGE_STAINED_GLASS_PANE);
            backButton.onClick(() -> {
                this.gui.clearSlots(); // unset the old slots
                this.lastBuiltSlot = this.lastBuiltSlot - this.slotsPerPage; // put slots for the remainingSlots
                this.execute();
            });
        }

        // when the next button is pressed
        GUISlot nextButton = new GUISlot(this.gui, 45);
        if (this.slotsPerPage <= remainingQuests.size()) { // if the remaining is bigger or the same as the default slots per page
            nextButton.setLabel("Next");
            nextButton.setItem(Material.GREEN_STAINED_GLASS_PANE);
            nextButton.onClick(() -> {
                this.gui.clearSlots(); // unset the old slots
                this.lastBuiltSlot = this.lastBuiltSlot + this.slotsPerPage; // take slots for the remainingSlots
                this.execute();
            });
        }

        Integer slotCount = remainingQuests.size() >= this.slotsPerPage // if there are more remaining quests than the default slot limit
        ? this.slotsPerPage // use the default slot limit
        : remainingQuests.size(); // otherwise use the number of remaining quests

        IntStream.range(0, slotCount).anyMatch(index -> { // only built 42 slots
            if (remainingQuests.isEmpty() || remainingQuests.get(index) == null) {
                return true; // exit the loop (marked as 'found match' to exit)
            }

            String questID = remainingQuests.get(index);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot questSlot = new GUISlot(this.gui, nextEmptySlot);
            List<Object> screen;

            Quest quest = QuestRegistry.getInstance().getQuest(questID);

            if (quest == null) { // cannot parse quest at all
                this.gui.removeSlot(nextEmptySlot);
                this.invalidQuests+= 1;
                return false;
            }

            // Don't show if user is not the creator (unless it's null, then it's probably a global quest).
            if (!playerUUID.equals(quest.getCreator()) && quest.getCreator() != null) {
                this.gui.removeSlot(nextEmptySlot); // remove slot, no need to show in this case
                return false;
            }

            // ---- If the quest is considered invalid ---- //
            if (quest == null || !quest.isValid()) {
                questSlot.setLabel(
                    String.format("%s (Invalid)",
                        quest.getTitle() != null ? quest.getTitle() : "Quest"
                    )
                );

                questSlot.setItem(Material.RED_STAINED_GLASS_PANE);

                return false; // return if this quest is broken (false for match not found, continue to check next)
            }
            // --------;

            questSlot.setLabel(questID.split("_")[0]);

            if (playerUUID.equals(quest.getCreator())) {
                screen = Arrays.asList("myquest");
                questSlot.setItem(Material.BOOK);
            } else {
                screen = Arrays.asList("theirquest");
                questSlot.setItem(Material.ENCHANTED_BOOK);
                questSlot.setLabel(questSlot.getLabel() + " (Shared)");
            }

            questSlot.onClick(() -> {
                this.director.setCurrentInstance(quest);

                // update the GUI screen
                new UpdateScreen(
                    screen, 
                    director
                ).execute();;
            });

            return false; // continue the loop (as in match not found, continue)
        });
    }
}
