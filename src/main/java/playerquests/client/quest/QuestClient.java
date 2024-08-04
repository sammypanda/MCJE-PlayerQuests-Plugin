package playerquests.client.quest;

import java.util.ArrayList; // array list type
import java.util.HashMap; // hash implementation of map data type
import java.util.List; // generic list type
import java.util.Map; // map data type, offers a key-value pair

import org.bukkit.Bukkit; // the minecraft server API
import org.bukkit.Particle;
import org.bukkit.entity.Player; // represents just players
import org.bukkit.scheduler.BukkitScheduler; // for doing actions later and etc
import org.bukkit.scheduler.BukkitTask; // for particle effects (FX)

import playerquests.Core;
import playerquests.builder.quest.action.QuestAction; // quest action
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.npc.QuestNPC; // represents quest npcs
import playerquests.product.Quest; // represents a player quest

/**
 * Quest tracking and interactions for each player.
 */
public class QuestClient {

    /**
     * The player who is using this quest client.
     */
    private Player player;

    /**
     * Quest diary API for this player.
     */
    private QuestDiary diary;

    /**
     * The action associated with an NPC.
     * Helps show an action when interacting with an NPC.
     */
    private Map<QuestNPC, QuestAction> actionNPC = new HashMap<QuestNPC, QuestAction>();

    /**
     * List of the running FX.
     */
    private List<BukkitTask> activeFX = new ArrayList<BukkitTask>();

    /**
     * Used for particle FX loops.
     */
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    /**
     * Creates a new quest client to act on behalf of a player.
     * <p>
     * A quest client enables interactions. It keeps track of 
     * player quest progress and other quest-related information.
     * @param player user of the quest client
     */
    public QuestClient(Player player) {
        this.player = player;

        this.diary = new QuestDiary(this);
    }

    /**
     * Gets the player who is using the quest client.
     * @return human entity object representing the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Add a list of quests for this quest client.
     * @param questList a list of quest products.
     */
    public void addQuests(List<Quest> questList) {
        questList.parallelStream().forEach((quest) -> {
            this.diary.addQuest(quest);
        });

        this.update(); // update what the player sees
    }

    /**
     * Adds the quest effects in the world for this quester.
     */
    public void showFX() {
        this.hideFX(); // ensure old are removed before showing

        this.actionNPC.keySet().stream().forEach((npc) -> {
            // create particle effect
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

            // store a reference to this effect for cancelling
            activeFX.add(task);
        });
    }

    /**
     * Removes the quest effects from the world for this quester.
     */
    public void hideFX() {
        // get all active effects and cancel
        this.activeFX.stream().forEach((task) -> {
            // cancel FX loops
            scheduler.cancelTask(task.getTaskId());
        });
    }

    /**
     * Update what the player sees.
     */
    public void update() {
        // clear action-npc-associations for the refresh! (good for if a quest is deleted)
        this.actionNPC.clear();

        // NPC magic! (assigning an action to an npc based on progress in quest)
        // get each quest progress
        this.diary.getQuestProgress().forEach((quest, connections) -> {

            // get the action we are up to in this quest to..
            QuestAction action = this.diary.getAction(quest);

            // ..get the npc
            QuestNPC npc = action.getNPC();

            // don't continue if no npc matched to this action
            if (npc == null) {
                return;
            }
            
            // ..and submit the 'action <-> NPC' association
            this.actionNPC.put(
                npc,
                action
            );
        });

        this.showFX();
    }

    /**
     * Remove a quest for this quest client.
     * @param quest the quest to remove
     */
    public void removeQuest(Quest quest) {
        this.diary.removeQuest(quest);

        this.update(); // reflect changes
    }

    /**
     * Process when an NPC is interacted with.
     * @param npc the npc to interact with the quest through
     */
    public void interact(QuestNPC npc) {
        // Find the action associated with this npc in a helper map
        QuestAction action = this.actionNPC.get(npc);
        Quest quest = npc.getQuest();

        // Don't continue if there is no action for this interaction
        if (action == null) {
            return;
        }

        // Prepare interaction/next step vars
        String next_step = action.getConnections().getNext(); // could be action_?, stage_?
        ConnectionsData diaryConnections = this.diary.getQuestProgress(quest); // read current position in quest

        if (diaryConnections == null) { // if no progress for this quest found
            diaryConnections = quest.getStages().get(quest.getEntry()).getConnections(); // get quest entry point position
        }

        // move forward through connections
        if (next_step != null) { // if there is a next step
            ConnectionsData updatedConnections = new ConnectionsData();
            updatedConnections.setPrev(diaryConnections.getCurr());
            updatedConnections.setCurr(next_step);

            // update the diary
            this.diary.setQuestProgress(quest, updatedConnections);

            // remove NPCs pending interaction marker/sparkle
            this.actionNPC.remove(npc);

            // update quest state
            this.update();
        }

        // Do the action
        action.Run(this);

        // Update what the player sees
        this.update();
    }

    /**
     * Get the quest diary for this quest client.
     * @return a quest diary
     */
    public QuestDiary getDiary() {
        return this.diary;
    }
}
