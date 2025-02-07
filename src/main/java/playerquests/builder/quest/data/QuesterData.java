package playerquests.builder.quest.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;
import playerquests.product.FX;
import playerquests.product.Quest;

/**
 * The data about the quester playing the action.
 */
public class QuesterData {

    /**
     * Useful for communicating action progress.
     * May not be present if some other client is in use.
     */
    private final QuestClient quester;

    /**
     * Useful for checking location/biome/other.
     * May not be present for something like 'has player completed x quest'.
     */
    private final Location location;

    /**
     * Useful for stopping listening to an action that has been completed.
     */
    private HashMap<QuestAction, ActionListener<?>> listeners = new HashMap<>();

    /**
     * Useful for stopping FXs from cycling.
     */
    private HashMap<QuestAction, List<FX>> effects = new HashMap<>();

    /**
     * Useful for tracking NPCs in the world.
     */
    private HashMap<QuestAction, QuestNPC> npcs = new HashMap<>();

    /**
     * Lock to wait for an ongoing action clash to be resolved.
     */
    private Boolean clashLock = false;

    /**
     * The context of data useful for working with a QuestClient.
     * @param quester the QuestClient
     * @param location location of the quester
     */
    public QuesterData(QuestClient quester, Location location) {
        this.quester = quester;
        this.location = location;
    }

    /**
     * Returns the QuestClient associated with this action.
     * @return the quester
     */
    public QuestClient getQuester() {
        return quester;
    }

    /**
     * Returns the location this action is taking place in.
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Adds a listener to this quester data.
     * @param actionListener the listener that will trigger action checking
     * @return the passed in listener
     */
    public ActionListener<?> addListener(QuestAction action, ActionListener<?> actionListener) {
        this.listeners.put(action, actionListener);
        return actionListener;
    }

    /**
     * Gets a listener from this quester data by its type.
     * @param listenerType
     * @return
     */
    public ActionListener<?> getListener(QuestAction action) {
        return this.listeners.get(action);
    }

    /**
     * Adds action FX to be tracked by this Quester Data.
     * @param action the action to associate the FX with
     * @param effectList the list of FX to add
     * @return the passed in list of FX
     */
    public List<FX> addFX(QuestAction action, List<FX> effectList) {
        this.effects.put(action, effectList);
        return effectList;
    }

    /**
     * Get the ongoing FX associated with a Quest Action.
     * @param questAction the quest action the FX is for
     * @return a list of effects that are currently in the world
     */
    public List<FX> getFX(QuestAction action) {
        return this.effects.get(action);
    }

    /**
     * Do work to resolve any clashes where two actions try to play
     * at the same time.
     * @param action the quest action to check against
     * @return whether the clash has been resolved
     */
    public boolean resolveClashes(QuestAction action) {
        // if waiting to resolve a clash, don't continue
        if (clashLock) {
            return false;
        }

        QuestClient quester = this.getQuester();

        // if there are more than one actions 
        // left after this filtration, that means there is a clash
        ArrayList<QuestAction> clashingActions = new ArrayList<>(quester.getTrackedActions().stream()
            // filter out exact matches
            .filter(trackedAction -> !trackedAction.equals(action))
            // check against locations
            .filter(trackedAction -> trackedAction.getLocation().equals(action.getLocation()))
            // get final size
            .toList());
            
        // exit if no clashing to resolve
        if (clashingActions.size() == 0) {
            return true;
        }

        // lock so quester is required to resolve this clash before any others
        this.clashLock = true;

        // resolve clashing
        clashingActions.add(action); // add the reference action in as an option
        Player player = quester.getPlayer(); // get the player
        ComponentBuilder message = new ComponentBuilder("\nThis area offers more than one action\n")
            .append("Click one of the following:\n\n").color(ChatColor.GRAY); // establish the message to send

        clashingActions.forEach((clashingAction) -> { // add actions
            final QuestStage questStage = clashingAction.getStage();
            final Quest quest = questStage.getQuest();
            final String path = new StagePath(questStage, List.of(clashingAction)).toString(); // the path to the action
            final String command = String.format("/action start %s.%s", quest.getID(), path); // command that resolves the clash?

            message
                .append(String.format("> %s.%s\n", 
                    quest.getTitle(), // the quest title
                    path)) // the path to the action
                .reset() // clear inherited formatting
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        });
        player.spigot().sendMessage(message.build()); // send the message

        this.clashLock = false;

        // don't continue if unresolved
        return false;
    }

    /**
     * Track the NPC in the QuesterData.
     * @param npc the NPC to track
     */
    public void addNPC(QuestAction questAction, QuestNPC npc) {
        this.npcs.put(questAction, npc);
    }

    /**
     * Untrack the NPC in the QuesterData.
     * @param npc the NPC to untrack
     */
    public void removeNPC(QuestAction questAction, QuestNPC npc) {
        this.npcs.remove(questAction, npc);
    }

    /**
     * Get the NPCs tracked in this QuesterData.
     * @return the list of tracked npcs;
     */
    public Map<QuestAction, QuestNPC> getNPCs() {
        return this.npcs;
    }
}
