package playerquests.utility.singleton;

import java.io.IOException;
import java.util.HashMap; // hash table map
import java.util.Map; // generic map type
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player; // representing players

import playerquests.client.quest.QuestClient; // player quest state
import playerquests.product.Quest; // describes quests
import playerquests.utility.ChatUtils; // utility methods related to chat
import playerquests.utility.FileUtils;
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;


/**
 * Singleton for putting and accessing quest products from anywhere.
 * 
 * This class manages quests and their associated data, such as NPCs and quest clients. It allows for adding, removing,
 * updating, and retrieving quests, as well as handling quests in the filesystem.
 */
public class QuestRegistry {
    
    /**
     * This QuestRegistry singleton.
     */
    private static final QuestRegistry instance = new QuestRegistry();

    /**
     * The map holding the quests.
     */
    private final Map<String, Quest> registry = new HashMap<String, Quest>();

    /**
     * The map holding the quest clients (questers).
     */
    private final Map<Player, QuestClient> questers = new HashMap<Player, QuestClient>();

    /**
     * The resource folder quests are in
     */
    private final String questPath = "quest/templates";

    /**
     * Private constructor to prevent instantiation.
     */
    private QuestRegistry() {}

    /**
     * Returns the QuestRegistry instance.
     * @return singleton instance of the quest registry.
     */
    public static QuestRegistry getInstance() {
        return instance;
    }

    /**
     * Submits a quest to the registry.
     * 
     * This method figures out the conditions of the quest
     * and acts according to the needs.
     * 
     * @param quest the quest to submit.
     */
    public void submit(Quest quest) {
        String questID = quest.getID();

        // stop if quest is invalid
        if (!quest.isValid()) {
            ChatUtils.message("Not installing an invalid quest: " + questID)
                .target(MessageTarget.CONSOLE)
                .style(MessageStyle.PLAIN)
                .type(MessageType.WARN);
            return;
        }

        // TODO: displace NPCs that would be on top of other NPCs
        // ...

        // remove if already exists
        this.registry.values().removeIf(registryQuest -> registryQuest.getID().equals(questID));

        // add to database/lists
        this.add(questID, quest);

        // untoggle and don't continue if quest is not toggled on
        if (!quest.isToggled()) {
            this.untoggle(quest);
            return;
        }

        // add to world
        this.toggle(quest);
    }

    /**
     * Deletes a quest *permanently*.
     * 
     * Hides non-permanently.
     * 
     * @param quest the quest to delete.
     * @return if the quest was successfully deleted
     */
    public boolean delete(Quest quest) {
        return this.delete(quest, true);
    }

    /**
     * Deletes a quest.
     * 
     * If wanting to just make quest uninteractable use {@link #untoggle(Quest)}.
     * 
     * @param quest the quest to delete.
     * @param permanently if should also delete from database/filesystem
     * @return if the quest was successfully deleted
     */
    public boolean delete(Quest quest, Boolean permanently) {
        String questID = quest.getID();
        UUID creator = quest.getCreator(); // get the creator if this quest has one

        // try to delete the quest
        try {
            // remove from registry
            this.registry.remove(questID);

            // remove from world
            PlayerQuests.remove(quest);

            if (permanently) {
                FileUtils.delete(this.questPath + "/" + quest.getID() + ".json");

                // refund resources
                quest.refund();
            }

        } catch (IOException e) {
            MessageBuilder errorMessage = ChatUtils.message("Could not delete the " + quest.getTitle() + " quest. " + e)
                .type(MessageType.ERROR)
                .target(MessageTarget.CONSOLE);

            if (creator != null) { // send the error to the player if there is a creator UUID
                errorMessage
                    .player(Bukkit.getPlayer(creator))
                    .style(MessageStyle.PRETTY);
            }
                
            errorMessage.send();
            return false;
        }

        return true;
    }

    /**
     * Stores the quest as a data point.
     * 
     * @param questID the quest ID as a string
     * @param quest the quest object
     */
    private void add(String questID, Quest quest) {
        // add to registry reference
        this.registry.put(questID, quest);

        // store ref to database
        Database.getInstance().addQuest(questID);
    }

    /**
     * Shows the physical quest in the world.
     * 
     * @param quest the quest to show/toggle on.
     */
    public void toggle(Quest quest) {
        // install the quest into the world
        PlayerQuests.install(quest);
    }

    /**
     * Hides the physical quest in the world.
     * 
     * @param quest the quest to hide/toggle off.
     */
    public void untoggle(Quest quest) {
        // remove the quest from the world
        PlayerQuests.remove(quest);
    }

    /**
     * Adds a quest client to the registry, identified by the player behind the client.
     * 
     * @param quester a quest client
     */
    public void addQuester(QuestClient quester) {
        questers.put(quester.getPlayer(), quester);
    }

    /**
     * Gets a quest client associated with a player.
     * 
     * @param player the player the quest client is for
     * @return a quest client instance
     */
    public QuestClient getQuester(Player player) {
        return questers.get(player);
    }

    /**
     * Gets all the quest clients associated with all the players. 
     * 
     * @return a map of quest client instances
     */
    public Map<Player, QuestClient> getQuesters() {
        return questers;
    }

    /**
     * Gets the map of all quests that have been registered.
     * 
     * @return the map of registered quests
     */
    public Map<String, Quest> getAllQuests() {
        return this.registry;
    }

    /**
     * Clears all quests and questers from the registry.
     */
    public void clear() {
        this.registry.clear();
        this.questers.clear();
    }

    /**
     * Get a quest from the quest registry.
     * 
     * If the quest is not found in the registry, it will search the questPath (resources folder).
     * 
     * @param questID the quest ID
     * @return the quest object
     */
    public Quest getQuest(String questID) {
        return this.getQuest(questID, false);
    }

    /**
     * Get a quest from the quest registry.
     * 
     * If the quest is not found in the registry, you can choose to search the questPath (resources folder).
     * 
     * @param questID the quest ID
     * @param searchFS whether to try searching the filesystem
     * @return the quest object
     */
    public Quest getQuest(String questID, Boolean searchFS) {
        Quest result = this.getAllQuests().get(questID);

        // search in filesystem
        if (result == null && searchFS) {
            System.err.println("Quest registry could not find quest: " + questID + ". It'll now search for it in the resources quest template files.");

            // attempt finding it in the files and uploading to database
            try {
                result = Quest.fromTemplateString(FileUtils.get(this.questPath + "/" + questID + ".json"));

                // if everything is okay, submit the quest to the database 
                // to avoid having to search for it again like this.
                if (result != null) {
                    this.submit(result);
                    result.toggle(false); // toggle off when found (opposite of default)
                }

            } catch (IOException e) {
                throw new RuntimeException("Could not find the quest with ID: " + questID, e);
            }
        }

        return result;
    }
}