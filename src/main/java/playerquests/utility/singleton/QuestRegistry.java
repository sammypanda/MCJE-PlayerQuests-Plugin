package playerquests.utility.singleton;

import java.io.IOException;
import java.util.Arrays; // generic array type
import java.util.HashMap; // hash table map
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.Optional; // handling if a value may be null
import java.util.UUID; // used for in-game player UUIDs
import java.util.concurrent.atomic.AtomicBoolean; // modify boolean state in a stream operation
import java.util.stream.Collectors; // used to turn one type of list to another

import org.bukkit.Bukkit; // accessing Bukkit API
import org.bukkit.entity.HumanEntity; // representing players and other humanoid entities
import org.bukkit.entity.Player; // representing players

import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.npc.QuestNPC; // describes quest NPCs
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
     * Adds a quest to the registry.
     * 
     * This method adds a quest to the internal registry and places its NPCs in the world. It also updates questers with
     * the new quest.
     * 
     * @param quest the quest to add.
     */
    private void add(Quest quest) {
        // add to the registry map
        registry.put(quest.getID(), quest);

        // store ref to database
        Database.getInstance().addQuest(quest.getID());

        // place the NPCs in the world
        quest.getNPCs().entrySet().stream()
            .forEach(entry -> {
                QuestNPC npc = entry.getValue();
                npc.setQuest(quest);
                npc.place();
            });

        questers.values().stream().forEach(quester -> {
            quester.addQuests(Arrays.asList(quest));
        });
    }

    /**
     * Submits a quest to the registry.
     * 
     * This method handles both adding a new quest and updating an existing one. It checks for collisions with existing
     * NPCs and handles them accordingly.
     * 
     * @param quest the quest to submit.
     */
    public void submit(Quest quest) {
        String questID = quest.getID();
        HumanEntity creator = null;
        AtomicBoolean isQuestValid = new AtomicBoolean(true); // start with assumption the quest is valid
        
        // get the creator if it's not a universal quest (missing or null creator)
        if (quest.getCreator() != null) {
            creator = Bukkit.getPlayer(quest.getCreator());
        }

        // replace if already found in registry
        if (registry.get(questID) != null) {
            if (creator != null) {
                creator.sendMessage("[Updating the quest]");
            }
            this.replace(questID, quest);
            return;
        }

        // load up a list of NPC locations to help figure if an npc already exists at the npc.location
        List<LocationData> registryNPCLocations = this.registry.values().stream().flatMap(
            registryQuest -> registryQuest.getNPCs().values().stream().map(QuestNPC::getLocation)
        ).collect(Collectors.toList());

        // check each registry NPC location against current NPC in submitted quest
        Optional<QuestNPC> collidingNPC = quest.getNPCs().values().stream()
            .filter(questNPC -> registryNPCLocations.stream()
                .anyMatch(existingLocation -> {
                    LocationData submittedLocation = questNPC.getLocation();

                    if (!questNPC.isValid()) {
                        isQuestValid.set(false);
                        return false; // invalid, so no use displacing
                    }

                    return existingLocation.collidesWith(submittedLocation);
                })
            ).findFirst();

        // do not continue if npc has no LocationData
        if (!isQuestValid.get()) {
            ChatUtils.message("Invalid quest submitted: " + questID)
                .target(MessageTarget.CONSOLE)
                .style(MessageStyle.PLAIN)
                .type(MessageType.ERROR)
                .send();
            return;
        }

        // if submitted quest has an NPC found colliding with existing,
        if (collidingNPC.isPresent()) {
            LocationData questNPCLocation = collidingNPC.get().getLocation();
            questNPCLocation.setY(questNPCLocation.getY() + 1); // put the NPC above the existing one
            this.submit(quest); // resubmit the quest to test if new location collides

            // do not continue if an npc already exists at the crucial npc.location
            return;
        }

        // store ref
        this.add(quest);

        if (creator != null) {
            creator.sendMessage("[Submitted]");
        }
    }

    /**
     * Removes a quest from the registry.
     * 
     * @param quest the quest to remove.
     */
    public void remove(Quest quest) {
        this.remove(quest, false);
    }

    /**
     * Removes a quest from the registry.
     * 
     * @param quest the quest to remove
     * @param preserveInDatabase whether to keep the quest in the database; just remove from the world if true
     */
    public void remove(Quest quest, Boolean preserveInDatabase) {
        // remove ref from database
        if (!preserveInDatabase) {
            Database.getInstance().removeQuest(quest.getID());

            // remove ref from registry (needed when not preserved, otherwise editors cannot see their own quest)
            registry.remove(quest.getID());
        }

        // remove traces from world
        PlayerQuests.getInstance().remove(quest);

        // remove ref from questers
        questers.values().stream().forEach(quester -> {
            quester.removeQuest(quest);
        });
    }

    /**
     * Deletes a quest from the plugin.
     * 
     * This method removes the quest from the filesystem, registry, and optionally refunds resources.
     * 
     * @param quest the quest to delete
     * @param refund whether to refund resources associated with the quest
     * @return whether the operation was successful or not
     */
    public Boolean delete(Quest quest, Boolean refund) {
        UUID creator = quest.getCreator(); // get the creator if this quest has one

        // try to remove from files
        try {
            FileUtils.delete(this.questPath + "/" + quest.getID() + ".json");
            
            // remove from registry
            this.remove(quest);

            // refund resources
            if (refund) {
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
     * Replaces a quest in the registry with a new one.
     * 
     * This method deletes the old quest (if it exists) and stores the new one.
     * 
     * @param originalQuestID the ID of the original quest.
     * @param quest the new quest.
     */
    public void replace(String originalQuestID, Quest quest) {
        Quest existingQuest = registry.get(originalQuestID); // find the original quest

        // if quest is already stored, delete it
        if (existingQuest != null) {
            this.delete(existingQuest, false);
        }

        // store the quest
        quest.save();

        // let the server know a quest has been replaced
        ChatUtils.message("A quest was reloaded: " + quest.getID())
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN)
            .type(MessageType.NOTIF)
            .send();
    }

    /**
     * Shortcut for replacing a quest with itself.
     * 
     * This method is used to update a quest without changing its ID.
     * 
     * @param quest the quest to update
     */
    public void update(Quest quest) {
        this.replace(quest.getID(), quest);
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