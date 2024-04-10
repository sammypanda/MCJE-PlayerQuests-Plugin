package playerquests.client.quest;

import java.util.ArrayList; // array list type
import java.util.HashMap; // hash table map type
import java.util.HashSet; // hash table set type
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.Set; // generic set type
import java.util.stream.Collectors; // translates a stream to data type

import org.bukkit.Bukkit; // bukkit api
import org.bukkit.Particle; // particle effects (FX)
import org.bukkit.entity.HumanEntity; // represents players and other humanoid entities
import org.bukkit.entity.Player; // represents just players
import org.bukkit.scheduler.BukkitScheduler; // schedules tasks/code/jobs on plugin
import org.bukkit.scheduler.BukkitTask; // object for scheduled tasks

import playerquests.Core; // access to singletons
import playerquests.builder.quest.action.QuestAction; // represents quest actions
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.data.LocationData; // data object containing all location info
import playerquests.builder.quest.npc.QuestNPC; // represents quest npcs
import playerquests.builder.quest.stage.QuestStage; // represents quest stages 
import playerquests.product.Quest; // represents a player quest
import playerquests.utility.singleton.Database; // where game data is stored
import playerquests.utility.singleton.QuestRegistry; // where available quests are stored

/**
 * Quest tracking and interactions for each player.
 */
// TODO: create persistent quest diary for the quester
// TODO: replace available quests with their ongoing versions from this quester
// TODO: add playing/starting quests
public class QuestClient {

    /**
     * The player who is using this quest client.
     */
    private HumanEntity player;

    /**
     * If the user has world effects on.
     */
    private Boolean fx;

    /**
     * The quests available to play (which haven't been started already).
     */
    private Map<String, Quest> availableQuests = new HashMap<String, Quest>();

    /**
     * The entry stage for each quest.
     */
    private Map<Quest, QuestStage> entryStages = new HashMap<Quest, QuestStage>();

    /**
     * The entry action for each entry stage. 
     */
    private Map<QuestStage, QuestAction> entryActions = new HashMap<QuestStage, QuestAction>();
    
    /**
     * The NPC associated with each entry action.
     */
    private Map<QuestNPC, QuestAction> npcActions = new HashMap<QuestNPC, QuestAction>();

    /**
     * The particles associated with NPC.
     */
    private Map<QuestNPC, BukkitTask> npcParticles = new HashMap<QuestNPC, BukkitTask>();

    /**
     * Quest diary API for this player.
     */
    private QuestDiary diary;

    /**
     * Creates a new quest client to act on behalf of a player.
     * <p>
     * A quest client enables interactions. It keeps track of 
     * player quest progress and other quest-related information.
     * @param player user of the quest client
     */
    public QuestClient(Player player) {
        this.player = player;

        // put player in database (and store the established ID)
        Integer dbPlayerID = Database.getInstance().addPlayer(player.getUniqueId());

        // create and/or establish quest diary for this player
        this.diary = new QuestDiary(dbPlayerID);

        // initiate personal quest world state
        this.showFX(); // visual quest indicators
    }

    /**
     * Gets the player who is using the quest client.
     * @return human entity object representing the player
     */
    public HumanEntity getPlayer() {
        return player;
    }

    /**
     * Add a list of quests for this quest client.
     * @param questList a list of quest products.
     */
    public void addQuests(ArrayList<Quest> questList) {
        questList.stream().forEach(quest -> {
            String questID = quest.getID();

            if (this.availableQuests.containsKey(questID)) {
                return; // don't continue
            }

            // put the quests
            this.availableQuests.put(questID, quest);
        });

        this.update();
    }

    /**
     * Adds the quest effects in the world for this quester.
     */
    public void showFX() {
        this.fx = true;

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        Player player = Bukkit.getServer().getPlayer(this.player.getUniqueId());

        // remove particles from quest NPCs by the quests which
        // are no longer 'available'
        this.npcParticles.keySet().stream()
            .filter(quest -> !this.availableQuests.containsKey(quest.getID()))
            .forEach(quest -> scheduler.cancelTask(this.npcParticles.get(quest).getTaskId()));

        // add interact sparkle to each starting/entry-point NPC
        this.npcActions.keySet().stream().forEach(npc -> {
            // TODO: remove logging
            System.out.println("sparkles for " + npc.getName());
            System.out.println("entry npcs: " + this.npcActions);

            LocationData location = npc.getLocation();

            BukkitTask task = scheduler.runTaskTimer(Core.getPlugin(), () -> {
                player.spawnParticle(
                    Particle.WAX_ON,
                    (double) location.getX() + 0.5,
                    (double) location.getY() + 1.5,
                    (double) location.getZ() + 0.5,
                    5
                );
            }, 0, 20);

            npcParticles.put(npc, task);
        });
    }

    /**
     * Removes the quest effects from the world for this quester.
     */
    public void hideFX() {
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        this.fx = false;

        // remove particles already in world
        this.npcParticles.values().stream().forEach(task -> {
            scheduler.cancelTask(task.getTaskId());
        });
    }

    /**
     * Refresh all values.
     */
    public void update() {
        // create a set for local available quests and questregistry available quests
        List<Quest> registryList = new ArrayList<>(QuestRegistry.getInstance().getAllQuests().values());
        List<Quest> localList =  new ArrayList<>(this.availableQuests.values());

        Set<Quest> questSet = new HashSet<Quest>();
        questSet.addAll(registryList);
        questSet.addAll(localList);

        // update helper maps
        questSet.stream().forEach(quest -> {
            // put the entry stages
            QuestStage stage = quest.getStages().get(quest.getEntry());
            this.entryStages.put(quest, stage);

            // put the actions from entry stages
            QuestAction action = stage.getEntryPoint();
            this.entryActions.put(stage, action);

            // put the NPCs from entry actions
            QuestNPC npc = action.getNPC();
            this.npcActions.put(npc, action);

            // add quest to diary
            this.diary.addQuest(quest.getID());
        });

        // update main map of all quests available to this quester
        this.availableQuests = questSet.stream()
                                .collect(Collectors.toMap(quest -> quest.getID(), quest -> quest));

        if (this.fx) {
            this.showFX();
        }
    }

    public void removeQuest(Quest quest) {
        this.availableQuests.remove(quest.getID());

        this.update();
    }

    public void interact(QuestNPC npc) {
        QuestAction action = this.npcActions.get(npc);
        String next_action = action.getConnections().getNext();
        QuestStage stage = action.getStage();
        Quest quest = stage.getQuest();

        // read current position in quest
        ConnectionsData diaryConnections = this.diary.getQuestProgress(quest.getID());
        String current = diaryConnections.getCurr();
        
        // if action or stage associated with NPC is the same as the current
        if (current.equals(action.getID()) || current.equals(stage.getID())) {
            this.npcActions.get(npc).Run(this);

            if (next_action != null) {
                // move forward through connections
                ConnectionsData updatedConnections = new ConnectionsData();
                updatedConnections.setPrev(current);
                updatedConnections.setCurr(next_action);

                // update the diary with progress
                this.diary.setQuestProgress(quest.getID(), updatedConnections);
                stage.setEntryPoint(next_action);

                // remove from list of npcs who would be pending a sparkle
                npcActions.remove(npc);

                // continue
                this.update();
            }
        }
    }

    /**
     * Get the quest diary for this quest client.
     * @return a quest diary
     */
    public QuestDiary getDiary() {
        return this.diary;
    }
}
