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
import playerquests.builder.quest.action.None;
import playerquests.builder.quest.action.QuestAction; // quest action
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.npc.QuestNPC; // represents quest npcs
import playerquests.product.Quest; // represents a player quest
import playerquests.utility.singleton.Database; // the preservation/backup store
import playerquests.utility.singleton.QuestRegistry;

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
     * Quests which won't progress.
     * Useful for if the main action is being waited to be finished.
     */
    private List<Quest> lockedQuests = new ArrayList<>();

    /**
     * List of the running FX.
     */
    private List<BukkitTask> activeFX = new ArrayList<>();

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
        questList.stream().forEach((quest) -> {
            this.diary.addQuest(quest);
        });

        this.update(); // update what the player sees
    }

    /**
     * Adds the quest effects in the world for this quester.
     */
    public synchronized void showFX() {
        this.hideFX(); // ensure old are removed before showing

        this.actionNPC.keySet().stream().forEach((npc) -> {
            // create particle effect
            LocationData location = npc.getLocation();
            BukkitTask task = scheduler.runTaskTimer(Core.getPlugin(), () -> { // synchronous
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
    public synchronized void hideFX() {
        // get all active effects and cancel
        this.activeFX.forEach((task) -> {
            // cancel FX loops
            scheduler.cancelTask(task.getTaskId());
        });
    }

    /**
     * Update what the player sees.
     * @param run whether it's the first time updating.
     */
    public synchronized void update(Boolean run) {
        // clear action-npc-associations for the refresh! (good for if a quest is deleted)
        this.actionNPC.clear();

        // NPC magic! (assigning an action to an npc based on progress in quest)
        // get each quest progress
        Map<QuestNPC, QuestAction> actionNPCsLocal = new HashMap<>(); 
        this.diary.getQuestProgress().forEach((quest, connections) -> {

            // get the action we are up to in this quest to..
            QuestAction action = this.diary.getAction(quest);

            // ..get the npc
            QuestNPC npc = action.getNPC();

            // don't continue if no npc matched to this action
            if (npc == null) {
                // if first time running
                if (run) {
                    // auto-start actions that aren't interfacable with an NPC
                    Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                        action.Run(this);
                    });
                }

                return;
            }
            
            // ..and submit the 'action <-> NPC' association
            actionNPCsLocal.put(
                npc,
                action
            );
        });

        // submit the NPCs we found progress for
        this.actionNPC.putAll(actionNPCsLocal);

        this.showFX();
    }

    /**
     * Update what the player sees.
     */
    public synchronized void update() {
        this.update(false);
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
    public synchronized void interact(QuestNPC npc) {
        // Find the action associated with this npc in a helper map
        QuestAction action = this.actionNPC.get(npc);

        // Don't continue if no action associated with this npc
        if (action == null) {
            return;
        }

        // Do the action
        action.Run(this);
    }

    /**
     * Get the quest diary for this quest client.
     * @return a quest diary
     */
    public QuestDiary getDiary() {
        return this.diary;
    }

    /**
     * Start an action.
     * @param action the action to continue to or past.
     * @param next whether to go to 'current' or 'next' action      .
     */
    public void start(QuestAction action, Boolean next) {
        Quest quest = QuestRegistry.getInstance().getQuest(action.getStage().getQuest().getID());
        QuestNPC npc = action.getNPC();
        ConnectionsData currentConnections = action.getConnections();

        // Don't continue if there is no quest or action for this interaction
        if (action == null || quest == null) {
            return;
        }

        // don't continue if untoggled
        if (!quest.isToggled()) {
            return;
        }

        // Prepare interaction/next step vars
        StagePath next_step;
        if (next) {
            next_step = currentConnections.getNext();
        } else {    
            next_step = currentConnections.getCurr();
        }
        
        // read current position in quest
        ConnectionsData diaryConnections = this.diary.getQuestProgress(quest);
        if (diaryConnections == null) { // if no progress for this quest found
            diaryConnections = quest.getStages().get(quest.getEntry().getStage()).getConnections(); // get quest entry point position
        }

        // don't continue if no next step
        if (next && next_step == null) { 
            this.diary.removeQuest(quest);
            this.update();
            return;
        }

        ConnectionsData updatedConnections = new ConnectionsData();
        updatedConnections.setPrev(diaryConnections.getCurr());
        updatedConnections.setCurr(next_step);

        // run in sequence
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            // update the diary
            this.diary.setQuestProgress(quest, updatedConnections);

            // update the db for preservation sake
            if (!this.isLocked(quest)) {
                Database.getInstance().setDiaryQuest(this.diary, quest, updatedConnections);
            }

            if (npc != null) {
                // remove NPCs pending interaction marker/sparkle
                this.actionNPC.remove(npc);
            }

            // auto-execute next auto if no npc to wait for
            QuestAction nextAction = next_step.getAction(quest);
            if (nextAction.getNPC() == null && !nextAction.getClass().equals(None.class)) {
                nextAction.Run(this);
            }

            // update quest state
            this.update();
        });
    }

    /**
     * Check if a quest is in the lock list.
     * @param quest the quest to check.
     * @return whether it is locked or not.
     */
    public boolean isLocked(Quest quest) {
        return this.lockedQuests.contains(quest);
    }

    /**
     * Set a quest as locked or unlocked.
     * @param quest the quest to set.
     * @param lock state of the lock.
     */
    public void setLocked(Quest quest, Boolean lock) {
        if (lock) {
            this.lockedQuests.add(quest);
            return;
        }

        this.lockedQuests.remove(quest);
    }

    /**
     * Put a quest in waiting for a main action to complete.
     * @param action the action that we are waiting on.
     */
    public void wait(QuestAction action) {
        Quest quest = action.getStage().getQuest();

        this.setLocked(quest, true);
        this.start(action, false); // start the 'current' action sequence.
    }
}
