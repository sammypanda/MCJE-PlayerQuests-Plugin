package playerquests.builder.gui.dynamic;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.client.ClientDirector;
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.singleton.QuestRegistry;

/**
 * A dynamic GUI screen for the diary showing questers their ongoing quests.
 */
public class Dynamicquestdiary extends GUIDynamic {

    /**
     * Constructs a new {@code Dynamicquestdiary} instance.
     * @param director the client director that manages the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicquestdiary(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {}

    @Override
    protected void execute_custom() {
        // get and set the outer GUI frame
        GUIFrame frame = this.gui.getFrame()
            .setTitle("Quest Diary")
            .setSize(54);

        // get the player
        Player player = (Player) this.director.getPlayer();

        // add back button
        new GUISlot(gui, 1)
            .setItem(Material.OAK_DOOR)
            .setLabel("Back")
            .addFunction(
                new UpdateScreen(List.of(this.previousScreen), director)
            );

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
        
        // TODO: replace this test code, !! there might not be a 0th elements !!
        // TODO: clamp to 100 pags : 1024 characters per page
        // get the first tracked quest from the QuestClient to use as a sample page
        List<String> bookEntries = quester.getTrackedActions().stream()
            .map(action -> this.formatBookEntry(action))
            .toList();
        
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

        // return formatted page content
        return String.format("Quest: %s\n\nAction: %s (%s)\n\n%s", 
            quest.getTitle(),
            action.getID(),
            action.getName(),
            action.getLocation()
        );
    }
}
