package playerquests.client.quest;

import java.util.ArrayList; // array list type
import java.util.HashMap; // hash table map type
import java.util.Map; // generic map type

import org.bukkit.Bukkit; // bukkit api
import org.bukkit.Particle; // particle effects (FX)
import org.bukkit.entity.HumanEntity; // represents players and other humanoid entities
import org.bukkit.entity.Player; // represents just players
import org.bukkit.scheduler.BukkitScheduler; // schedules tasks/code/jobs on plugin

import playerquests.Core; // access to singletons
import playerquests.builder.quest.action.QuestAction; // represents quest actions
import playerquests.builder.quest.data.LocationData; // data object containing all location info
import playerquests.builder.quest.npc.QuestNPC; // represents quest npcs
import playerquests.builder.quest.stage.QuestStage; // represents quest stages 
import playerquests.product.Quest; // represents a player quest
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
    private Map<QuestAction, QuestNPC> entryNPCs = new HashMap<QuestAction, QuestNPC>();

    /**
     * Creates a new quest client to act on behalf of a player.
     * <p>
     * A quest client enables interactions. It keeps track of 
     * player quest progress and other quest-related information.
     * @param player user of the quest client
     */
    public QuestClient(Player player) {
        this.player = player;

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

            // put the entry stages
            QuestStage stage = quest.getStages().get(quest.getEntry());
            this.entryStages.put(quest, stage);

            // put the actions from entry stages
            QuestAction action = stage.getEntryPoint();
            this.entryActions.put(stage, action);

            // put the NPCs from entry actions
            QuestNPC npc = action.getNPC();
            this.entryNPCs.put(action, npc);
        });
    }

    /**
     * Adds the quest effects in the world for this quester.
     */
    public void showFX() {
        this.fx = true;

        System.out.println("showing fx");

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        Player player = Bukkit.getServer().getPlayer(this.player.getUniqueId());

        // add interact sparkle to each starting/entry-point NPC
        this.entryNPCs.values().stream().forEach(npc -> {
            if (npc == null) {
                return;
            }

            LocationData location = npc.getLocation();

            System.out.println("particle timer start for " + npc);

            scheduler.runTaskTimer(Core.getPlugin(), () -> {
                player.spawnParticle(
                    Particle.WAX_ON,
                    (double) location.getX(),
                    (double) location.getY(),
                    (double) location.getZ(),
                    5
                );
            }, 0, 20);
        });
    }

    /**
     * Removes the quest effects from the world for this quester.
     */
    public void hideFX() {
        this.fx = false;
    }

    /**
     * Refresh all values.
     */
    public void update() {
        this.addQuests(
            new ArrayList<Quest>(QuestRegistry.getInstance().getAllQuests().values())
        );

        if (this.fx) {
            this.showFX();
        }
    }
}
