package playerquests.utility.singleton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap; // hash table map
import java.util.List;
import java.util.Map; // generic map type
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player; // representing players

import playerquests.Core;
import playerquests.client.quest.QuestClient;
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
     * The inventories belonging to quests.
     */
    private Map<String, Map<Material, Integer>> inventories = new HashMap<>();

    /**
     * A list of questers that are currently playing.
     */
    private List<QuestClient> questers = new ArrayList<QuestClient>();

    /**
     * Private constructor to prevent instantiation.
     */
    private QuestRegistry() {}

    /**
     * Read and parse quest inventories from the database.
     */
    public void loadQuestInventories() {
        this.inventories = Database.getInstance().getAllQuestInventories();
    }

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
        
        // remove if already exists
        if (this.registry.values().removeIf(registryQuest -> registryQuest.getID().equals(questID))) {
            this.delete(quest, false);
        }

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
                FileUtils.delete(Core.getQuestsPath() + quest.getID() + ".json");

                // refund resources
                quest.refund();

                // remove from database
                Database.getInstance().removeQuest(quest.getID());
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
     * @return if was successful
     */
    public boolean toggle(Quest quest) {
        // check + error for if any NPCs can't be placed
        if (!this.canPlaceNPCs(quest)) {
            return false;
        }

        // toggle in database
        Database.getInstance().setQuestToggled(quest, true);

        // install the quest into the world
        PlayerQuests.install(quest);

        // indicate it was a success
        return true;
    }

    /**
     * Determine whether quest NPCs can be placed at the location 
     * they are specified
     * @param quest the quest containing the NPCs
     * @return false if any NPCs are not placeable
     */
    private boolean canPlaceNPCs(Quest quest) {
        return true;
    }

    /**
     * Hides the physical quest in the world.
     * 
     * @param quest the quest to hide/toggle off.
     */
    public void untoggle(Quest quest) {
        // untoggle in database
        Database.getInstance().setQuestToggled(quest, false);

        // remove the quest from the world
        PlayerQuests.remove(quest);
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
        this.clearQuesters();
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
            System.err.println("Quest registry could not find quest: " + questID + ". It'll now search for it in the quest files.");

            // attempt finding it in the files and uploading to database
            try {
                result = Quest.fromJSONString(FileUtils.get(Core.getQuestsPath() + questID + ".json"));

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

    /**
     * Get the current inventory/stock levels of a quest.
     * 
     * Never returns null, if it doesn't exist, it will 
     * create (and submit) an empty map.
     * 
     * @param quest the quest to get the inventory of.
     * @return the inventory.
     */
    public Map<Material, Integer> getInventory(Quest quest) {
        Map<Material, Integer> inventory = this.inventories.get(quest.getID());

        if (inventory == null) {
            inventory = new HashMap<>();
            this.setInventory(quest, inventory);
        }

        return inventory;
    }

    /**
     * Set the current inventory/stock levels of a quest.
     * @param quest quest to set for.
     * @param inventory the inventory item/quantity map.
     */
    public void setInventory(Quest quest, Map<Material, Integer> inventory) {
        this.inventories.put(quest.getID(), inventory);

        // preserve in database
        Database.getInstance().setQuestInventory(quest, inventory);
    }

    /**
     * Creates a new quester instance and adds it to an index.
     * @param player the player the quest client is on behalf of
     */
    public void createQuester(Player player) {
        QuestClient quester = new QuestClient(player);

        Optional<QuestClient> questClient = this.questers.stream()
            .filter(qc -> qc.getPlayer().equals(player))
            .findFirst();

        if (questClient.isPresent()) {
            this.questers.set(
                questers.indexOf(questClient.get()), 
                quester
            );
            return;
        }

        // add player to Database
        Database.getInstance().addPlayer(player.getUniqueId());

        // add to registry list
        this.questers.add(quester);
    }

    /**
     * Cleans up and clears out questers list.
     */
    public void clearQuesters() {
        this.questers.clear();
    }

    /**
     * Removes a quester from the list.
     * @param player player to remove
     */
    public void removeQuester(Player player) {
        this.questers.removeIf(client -> client.getPlayer().equals(player));
    }

    /**
     * Gets the list of questers
     * @return the list of registered QuestClients
     */
    public List<QuestClient> getAllQuesters() {
        return this.questers;
    }

    /**
     * Gets a quester
     * return a quest client
     */
    public QuestClient getQuester(Player player) {
        // filter the quester list for the quester being seeked
        Optional<QuestClient> quester = this.questers.stream()
            .filter(q -> q.getPlayer().equals(player))
            .findFirst();

        // error on catastrophe
        if (quester.isEmpty()) {
            throw new RuntimeException("Could not find a requested QuestClient, but all players should be assigned a QuestClient");
        }

        // return the quester
        return quester.get();
    }
}