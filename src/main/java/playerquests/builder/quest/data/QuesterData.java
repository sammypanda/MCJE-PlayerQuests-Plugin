package playerquests.builder.quest.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import playerquests.builder.quest.action.NoneAction;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;
import playerquests.product.FX;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;

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
    private HashMap<QuestAction<?,?>, ActionListener<?>> listeners = new HashMap<>();

    /**
     * Useful for stopping FXs from cycling.
     */
    private HashMap<QuestAction<?,?>, List<FX>> effects = new HashMap<>();

    /**
     * Lock to wait for an ongoing action clash to be resolved.
     */
    private Boolean clashLock = false;

    /**
     * Map if quester has consented to an action.
     */
    private Map<QuestAction<?,?>, Boolean> actionConsent = new HashMap<>();

    /**
     * Map of registered QuestNPCs that are BlockNPC.
     */
    private Map<Entry<QuestAction<?,?>, QuestNPC>, BlockData> blockNPCs = new HashMap<>();

    /**
     * Map of registered QuestNPCs that are EntityNPC.
     */
    private Map<Entry<QuestAction<?,?>, QuestNPC>, NPC> entityNPCs = new HashMap<>();

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
    public ActionListener<?> addListener(QuestAction<?,?> action, ActionListener<?> actionListener) {
        this.listeners.put(action, actionListener);
        return actionListener;
    }

    /**
     * Gets a listener from this quester data by its type.
     * @param listenerType
     * @return
     */
    public ActionListener<?> getListener(QuestAction<?,?> action) {
        return this.listeners.get(action);
    }

    /**
     * Adds action FX to be tracked by this Quester Data.
     * @param action the action to associate the FX with
     * @param effectList the list of FX to add
     * @return the passed in list of FX
     */
    public List<FX> addFX(QuestAction<?,?> action, List<FX> effectList) {
        this.effects.put(action, effectList);
        return effectList;
    }

    /**
     * Get the ongoing FX associated with a Quest Action.
     * @param questAction the quest action the FX is for
     * @return a list of effects that are currently in the world
     */
    public List<FX> getFX(QuestAction<?,?> action) {
        return this.effects.getOrDefault(action, List.of());
    }

    /**
     * Do work to resolve any clashes where two actions try to play
     * at the same time.
     * @param action the quest action to check against
     * @return whether the clash has been resolved
     */
    public boolean resolveClashes(QuestAction<?,?> action) {
        // if waiting to resolve a clash, don't continue
        if (clashLock) {
            return false;
        }

        QuestClient quester = this.getQuester();

        // if there are more than one actions
        // left after this filtration, that means there is a clash
        ArrayList<QuestAction<?,?>> clashingActions = new ArrayList<>(quester.getTrackedActions().stream()
            // filter out exact matches
            .filter(trackedAction -> !trackedAction.equals(action))
            // filter out any 'None' action
            .filter(trackedAction -> !(trackedAction instanceof NoneAction))
            // check against locations
            .filter(trackedAction -> trackedAction.getLocation() != null && trackedAction.getLocation().equals(action.getLocation()))
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
        Builder message = Component.text()
            .appendNewline()
            .append(Component.text("This area offers more than one action\n"))
            .appendNewline()
            .append(Component.text("Click one of the following:").color(NamedTextColor.GRAY))
            .appendNewline().appendNewline(); // establish the message to send

        clashingActions.forEach((clashingAction) -> { // add actions
            final QuestStage questStage = clashingAction.getStage();
            final Quest quest = questStage.getQuest();
            final String path = new StagePath(questStage, List.of(clashingAction)).toString(); // the path to the action
            final String command = String.format("/action start %s.%s", quest.getID(), path); // command that resolves the clash?

            message
                .append(Component.text(
                    String.format("> %s.%s%n",
                        quest.getTitle(), // the quest title
                        path)) // the path to the action
                )
                .color(NamedTextColor.WHITE)
                .clickEvent(ClickEvent.runCommand(command));
        });
        
        // send the finished message
        ChatUtils.message(message.asComponent())
            .player(player) // to the player
            .send();

        this.clashLock = false;

        // don't continue if unresolved
        return false;
    }

    /**
     * Stop an action listener and unset it.
     * @param action the action the listener is paired with
     */
    public void stopListener(QuestAction<?,?> action) {
        ActionListener<?> listener = this.getListener(action);

        if (listener == null) {
            return;
        }

        listener.close();
        this.listeners.remove(action);
    }

    /**
     * Set consent for an action, like taking items from quester inventory.
     * @param action quest action to set consent for
     * @param consent state of consent to set
     */
    public void setConsent(QuestAction<?,?> action, boolean consent) {
        this.actionConsent.put(action, consent);
    }

    /**
     * Get the consent state for an action.
     * @param action quest action to check consent of
     * @return consent state; defaulting to false
     */
    public boolean getConsent(QuestAction<?,?> action) {
        return this.actionConsent.getOrDefault(action, false);
    }

    public BlockData getBlockNPC(QuestAction<?,?> action, QuestNPC npc) {
        return this.blockNPCs.get(Map.entry(action, npc));
    }

    public void addBlockNPC(QuestAction<?,?> action, QuestNPC npc, BlockData value) {
        this.blockNPCs.put(Map.entry(action, npc), value);
    }

    public void removeBlockNPC(QuestAction<?,?> action, QuestNPC npc) {
        this.blockNPCs.remove(Map.entry(action, npc));
    }

    public NPC getCitizenNPC(QuestAction<?,?> action, QuestNPC npc) {
        return this.entityNPCs.get(Map.entry(action, npc));
    }

    public void addCitizenNPC(QuestAction<?,?> action, QuestNPC npc, NPC value) {
        this.entityNPCs.put(Map.entry(action, npc), value);
    }

    public void removeEntityNPC(QuestAction<?,?> action, QuestNPC npc) {
        this.entityNPCs.remove(Map.entry(action, npc));
    }

    public List<Entry<QuestAction<?,?>, QuestNPC>> getNPCs() {
        ArrayList<Entry<QuestAction<?,?>, QuestNPC>> npcs = new ArrayList<>();

        npcs.addAll(this.blockNPCs.keySet());
        npcs.addAll(this.entityNPCs.keySet());

        return npcs;
    }
}
