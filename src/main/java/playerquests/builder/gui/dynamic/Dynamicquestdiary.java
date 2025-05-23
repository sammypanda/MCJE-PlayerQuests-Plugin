package playerquests.builder.gui.dynamic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import playerquests.Core;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.LocationData;
import playerquests.client.ClientDirector;
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.singleton.QuestRegistry;

/**
 * A dynamic GUI screen for the diary showing questers their ongoing quests.
 */
public class Dynamicquestdiary extends GUIDynamic {

    /**
     * The player viewing this GUI.
     */
    Player player;

    /**
     * The player as a quester/questclient
     */
    QuestClient quester;

    /**
     * The tracked and untracked quest actions
     */
    Map<QuestAction, Boolean> actionState = new HashMap<>();

    /**
     * Constructs a new {@code Dynamicquestdiary} instance.
     * @param director the client director that manages the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicquestdiary(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.player = (Player) this.director.getPlayer();
        this.quester = Core.getQuestRegistry().getQuester(player);

        // map tracked quest actions
        this.actionState.putAll(
            this.quester.getTrackedActions().stream() // tracked
            .collect(Collectors.toMap(action -> action, action -> true, (prev, curr) -> curr))
        );

        // map untracked quest actions, based on the difference from the tracked
        this.actionState.putAll(
            this.quester.getDiary().getQuestProgress().entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream()
                    .flatMap(path -> path.getActions(entry.getKey()).stream()))
                .filter(action -> !this.actionState.containsKey((QuestAction) action))
                .collect(Collectors.toMap(action -> (QuestAction) action, action -> false, (prev, curr) -> curr))
        );
    }

    @Override
    protected void execute_custom() {
        // get and set the outer GUI frame
        this.gui.getFrame()
            .setTitle("Quest Diary")
            .setSize(54);

        // get the player
        Player player = (Player) this.director.getPlayer();

        // add dividers
        IntStream.of(8, 17, 26, 35, 44, 53).forEach(position -> {
            this.createDivider(position);
        });

        // add back button
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .addFunction(
                new UpdateScreen(List.of(this.previousScreen), director)
            );

        // add quest action buttons
        this.generateActionButtons();

        // add diary book button
        new GUISlot(gui, 9)
            .setItem(Material.WRITTEN_BOOK)
            .setLabel("View Diary Entries")
            .onClick(() -> {
                // get the GUI out of the way
                this.gui.getResult().minimise();

                // create a book
                ItemStack book = this.createBook(player);

                // show the player the book
                player.openBook(book);
            });
    }

    /**
     * Generate a slot for each action and discriminate
     * whether it is currently being tracked or is untracked.
     */
    private void generateActionButtons() {
        this.actionState.forEach((action, state) -> {
            new GUISlot(this.gui, this.gui.getEmptySlot())
                .setLabel(action.getStage().getQuest().getTitle())
                .setDescription(List.of(
                    String.format("%s (%s)",
                        action.getID(),
                        state ? "Tracking" : "Untracked"
                    ))
                )
                .setItem(
                    state ? Material.GREEN_WOOL : Material.RED_WOOL
                );
        });
    }

    /**
     * Helper method for the divider slot template.
     * @param position the position to put the divider
     * @return the divider GUI slot
     */
    private GUISlot createDivider(int position) {
        return new GUISlot(gui, position)
            .setItem(Material.GRAY_STAINED_GLASS_PANE);
    }

    /**
     * Create a quest diary book by.
     * - Generating book pages
     * @param player
     * @return
     */
    private ItemStack createBook(Player player) {
        // create the book
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

        // set the book data
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setAuthor(player.getName());
        bookMeta.setTitle("Quest Diary");
        bookMeta.setPages(this.generateBookPages(player));

        // apply the meta
        book.setItemMeta(bookMeta);

        // return the resulting book
        return book;
    }

    /**
     * Generate quest diary book pages by.
     * - Formatting book entries (quest action entries)
     * @param player the player to generate book pages for
     * @return a list of book pages (up to 100 pages : 1024 characters per page)
     */
    private List<String> generateBookPages(Player player) {
        // get the QuestClient that represents the player
        QuestClient quester = QuestRegistry.getInstance().getQuester(player);

        // get the first tracked quest from the QuestClient to use as a sample page
        List<String> bookEntries = quester.getTrackedActions().stream()
            .map(action -> this.formatBookEntry(action))
            .toList();

        // clamp to 100 entries
        bookEntries
            .subList(0, Math.clamp(bookEntries.size(), 0, 99));

        // return a series of generated pages
        return bookEntries;
    }

    /**
     * Format book entries (quest action entries).
     *
     * Example entry:
     * Quest: B
     *
     * Action: action_0 (Speak)
     *
     * Location(world='world',
     * x=1213.0, y=69.0,
     * z=808.0, pitch=0.0,
     * yaw=0.0)
     *
     * @param quest the quest the action belongs to
     * @param action the action to format the page for
     * @return a formatted page explaining a quest action
     */
    private String formatBookEntry(QuestAction action) {
        // get the attached quest
        Quest quest = action.getStage().getQuest();
        LocationData location = action.getLocation();

        // return formatted page content
        return String.format("Quest: %s\n\nAction: %s (%s)\n\n%s",
            quest.getTitle(),
            action.getID(),
            action.getName(),
            location != null ? location.toString() : "Unknown location"
        );
    }
}
