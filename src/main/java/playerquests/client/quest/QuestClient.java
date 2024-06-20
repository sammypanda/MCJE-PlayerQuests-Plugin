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
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.action.None; // empty quest action
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
     * These are called 'helper maps' here,
     */
    public void update() {
        // clear the helper maps
        entryStages.clear();
        entryActions.clear();
        npcActions.clear();

        // create a set for local available quests and questregistry available quests
        List<Quest> registryList = new ArrayList<>(QuestRegistry.getInstance().getAllQuests().values()); // current all quests
        List<Quest> localList =  new ArrayList<>(this.availableQuests.values()); // previous all quests

        // add quests to parse through to create a sort of index powered by the 'helper maps'
        Set<Quest> questSet = new HashSet<Quest>();
        questSet.addAll(registryList);
        questSet.addAll(localList);

        // update 'helper maps'
        questSet.stream().forEach(quest -> {
            if (!quest.isValid()) {
                return;
            }

            // get this quest id (to check available quests against the diary)
            String questID = quest.getID();

            // get our current position in quest (curr, prev)
            ConnectionsData questProgress = this.diary.getQuestProgress(questID);

            // the action/stage that is todo
            QuestAction action;
            QuestStage stage;
            
            if (questProgress != null) { // (if user has progressed in this quest)
                // get the 'todo' action/stage
                action = this.diary.getAction(questID);
                stage = this.diary.getStage(questID);

            } else { // (if no user progress logged for this quest)
                // get the 'entry point' action/stage
                stage = quest.getStages().get(quest.getEntry()); // the first stage to look at when starting a quest
                action = stage.getEntryPoint(); // the first action to look at when starting a quest

                // add quest to diary
                this.diary.addQuest(quest.getID());
            }

            // queue up the action/stage
            this.entryStages.put(quest, stage);
            this.entryActions.put(stage, action);

            // put the NPC from entry action (if is valid)
            QuestNPC npc = action.getNPC();
            if (npc != null || action.getClass() != None.class) {
                this.npcActions.put(npc, action);
            }
        });

        // merge previous and current map of all quests, as main list of quests available to this quester
        this.availableQuests = questSet.stream()
            .collect(Collectors.toMap(quest -> quest.getID(), quest -> quest));

        // show particles indicating NPCs
        if (this.fx) {
            this.showFX();
        }
    }

    /**
     * Remove a quest for this quest client.
     * @param quest the quest to remove
     */
    public void removeQuest(Quest quest) {
        this.availableQuests.remove(quest.getID());

        this.update();
    }

    /**
     * Process when an NPC is interacted with.
     * @param npc the npc to interact with the quest through
     */
    public void interact(QuestNPC npc) {
        QuestAction action = this.npcActions.get(npc);
        Quest quest = npc.getQuest();

        // don't continue if there is no action
        // for this interaction
        if (action == null) {
            return;
        }

        // prep interaction/next step vars
        String next_step = action.getConnections().getNext(); // could be action_?, stage_?
        ConnectionsData diaryConnections = this.diary.getQuestProgress(quest.getID()); // read current position in quest

        if (diaryConnections == null) { // if no progress for this quest found
            diaryConnections = quest.getStages().get(quest.getEntry()).getConnections(); // get quest entry point position
        }

        // move forward through connections
        if (next_step != null) { // if there is a next step
            ConnectionsData updatedConnections = new ConnectionsData();
            updatedConnections.setPrev(diaryConnections.getCurr());
            updatedConnections.setCurr(next_step);

            // update the diary
            this.diary.setQuestProgress(quest.getID(), updatedConnections);

            // remove NPCs pending interaction marker/sparkle
            npcActions.remove(npc);

            // update quest state
            this.update();
        }
        
        // execute the interaction
        action.Run(this); // test, less complicated? also remove me
    }

    /**
     * Get the quest diary for this quest client.
     * @return a quest diary
     */
    public QuestDiary getDiary() {
        return this.diary;
    }
}
