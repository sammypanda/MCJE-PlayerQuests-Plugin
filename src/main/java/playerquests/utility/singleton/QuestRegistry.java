package playerquests.utility.singleton;

import java.io.IOException;
import java.util.HashMap; // hash table map
import java.util.Map; // generic map type

import org.bukkit.Bukkit; // accessing Bukkit API
import org.bukkit.entity.HumanEntity; // representing players and other humanoid entities
import org.bukkit.entity.Player; // representing players

import playerquests.builder.quest.npc.QuestNPC; // describes quest NPCs
import playerquests.client.quest.QuestClient; // player quest state
import playerquests.product.Quest; // describes quests
import playerquests.utility.ChatUtils; // utility methods related to chat
import playerquests.utility.FileUtils;


/**
 * Singleton for putting and accessing quest products from anywhere.
 */
// TODO: event listeners?
// TODO: (on remove) warn if someone is currently playing the quest
// TODO: (on remove) if called twice, forces removal even if someone is playing the quest (deletion waiting list)
// TODO: (on quest completion) tell quest creators in deletion waiting list the quest is safe to remove
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
     * Adds a quest to the registry.
     * @param quest the quest to add.
     */
    private void add(Quest quest) {
        // add to the registry map
        registry.put(quest.getID(), quest);

        // place the NPCs in the world
        quest.getNPCs().entrySet().stream()
            .forEach(entry -> {
                QuestNPC npc = entry.getValue();
                npc.place();
            });

        questers.values().stream().forEach(quester -> {
            quester.update();
        });
    }

    /**
     * Submits a quest to the registry.
     * @param quest the quest to submit.
     */
    public void submit(Quest quest) {
        String questID = quest.getID();
        HumanEntity creator = Bukkit.getPlayer(quest.getCreator());

        if (registry.get(questID) != null) {
            if (creator != null) {
                creator.sendMessage("[Updating the quest]");
            }
            this.replace(questID, quest);
            return;
        }

        // store ref to database
        Database.addQuest(questID);

        // store ref to registry
        this.add(quest);

        if (creator != null) {
            creator.sendMessage("[Submitted]");
        }
    }

    /**
     * Removes a quest from the registry.
     * @param quest the quest to remove.
     */
    public void remove(Quest quest) {
        // remove ref from database
        Database.removeQuest(quest.getID());

        registry.remove(quest.getID());

        questers.values().stream().forEach(quester -> {
            quester.removeQuest(quest);
        });
    }

    /**
     * Replaces a quest in the registry with a new one.
     * @param originalQuestID the ID of the original quest.
     * @param quest the new quest.
     */
    public void replace(String originalQuestID, Quest quest) {
        this.remove(registry.get(originalQuestID));
        this.add(quest);
    }

    /**
     * Adds a quest client to the registry, identified by the 
     * Player behind the client.
     * @param quester a quest client
     */
    public void addQuester(QuestClient quester) {
        questers.put(Bukkit.getPlayer(quester.getPlayer().getUniqueId()), quester);
        quester.update(); // add quests from registry
    }

    /**
     * Gets a quest client associated with a player.
     * @param player the player the quest client is for
     * @return a quest client instance
     */
    public QuestClient getQuester(Player player) {
        return questers.get(player);
    }

    /**
     * Gets map of all quests that have been registered.
     */
    public Map<String, Quest> getAllQuests() {
        return this.registry;
    }

    public void clear() {
        this.registry.clear();
        this.questers.clear();
    }

    /**
     * Get a quest from the quest registry.
     * If fails from quest registry, it will
     * search the questPath (resources folder).
     * @param questID the quest ID
     * @return the quest object
     */
    public Quest getQuest(String questID) {
        Quest result = this.getAllQuests().get(questID);

        if (result == null) {
            System.err.println("Quest registry could not find quest: " + questID + ". It'll now search for it in the resources quest template files.");

            // attempt finding it in the files and uploading to database
            try {
                result = Quest.fromTemplateString(FileUtils.get(this.questPath + "/" + questID + ".json"));

                // if everything is okay, submit the quest to the database 
                // to avoid having to search for it again like this.
                if (result != null) {
                    this.submit(result);
                }

            } catch (IOException e) {
                throw new RuntimeException("Could not find the quest with ID: " + questID, e);
            }
        }

        return result;
    }
}