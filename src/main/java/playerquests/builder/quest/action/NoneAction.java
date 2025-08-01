package playerquests.builder.quest.action;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.TimeCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.NoneListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;

/**
 * An action that does nothing. :D      
 */
public class NoneAction extends QuestAction<NoneAction, NoneListener> {

    /**
     * the NPC added into the world.
     */
    QuestNPC npc;

    /**
     * Constructor for jackson.
     */
    public NoneAction() {
        // Nothing here
    }

    /**
     * Skips the currently tasked quest action.
     * @param stage stage this action belongs to
     */
    public NoneAction(QuestStage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return "None";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        this.npc = this.spawnNPC(questerData);
    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        return true;
    }

    @Override
    protected void success(QuesterData questerData) {
        // remove the NPC
        this.despawnNPC(questerData);
    }

    @Override
    protected void failure(QuesterData questerData) {}

    @Override
    protected NoneListener startListener(QuesterData questerData) {
        return new NoneListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Does nothing."))
            .setItem(Material.BLACK_CONCRETE);
    }

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(
            NPCOption.class
        );
    }

    @Override
    public Optional<String> isValid() {
        return Optional.empty();
    }

    @Override
    public List<Class<? extends ActionCondition>> getConditions() {
        return List.of(
            TimeCondition.class
        );
    }

    @Override
    protected Class<?> getListenerType() {
        return NoneListener.class;
    }

    @Override
    public LocationData getLocation() {
        if (this.npc == null) {
            return null;
        }
        
        return new LocationData(this.npc.getLocation());
    }

    @Override
    public List<ActionTweaks> getTweaks() {
        return List.of(
            ActionTweaks.NO_FX
        );
    }
}
