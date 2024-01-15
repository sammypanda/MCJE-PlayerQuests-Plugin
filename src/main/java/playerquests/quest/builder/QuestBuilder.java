package playerquests.quest.builder;

import javax.annotation.Nullable; // annotating a param as null

import org.bukkit.entity.HumanEntity; // owner/creator of this quest

import playerquests.Core; // access the KeyHandler Singleton

/**
 * Main class for managing and building the functionality of a quest.
 */
public class QuestBuilder {

    /**
     * Anonymous initialiser which:
     * <ul>
     * <li>Adds this QuestBuilder instance to the KeyHandler
     * </ul>
     */
    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this); // add the current instance of QuestBuilder to be accessed with key-pair syntax
    }

    /**
     * Create a quest which only one player owns.
     * @param title quest title, used as ID
     * @param creator owner/creator of this quest
     */
    public QuestBuilder(String title, @Nullable HumanEntity creator) {
        String owner = creator == null ? "null" : creator.getUniqueId().toString(); 
    }

    /**
     * Create a quest which everyone owns.
     * @param title quest title, used as ID
     * @see #QuestBuilder(String, HumanEntity)
     */
    public QuestBuilder(String title) {
        this(title, null);
    }
}
