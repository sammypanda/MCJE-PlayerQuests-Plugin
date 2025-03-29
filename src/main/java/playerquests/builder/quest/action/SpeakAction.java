package playerquests.builder.quest.action;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.CompletionCondition;
import playerquests.builder.quest.action.condition.TimeCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.SpeakListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.DialogueOption;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.npc.QuestNPC;

/**
 * Action for an NPC speaking.
 */
public class SpeakAction extends QuestAction {

    /**
     * the NPC added into the world.
     */
    QuestNPC npc;

    /**
     * Default constructor for Jackson.
     */
    public SpeakAction() {}

    @Override
    public String getName() {
        return "Speak";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        this.npc = this.placeNPC(questerData);
    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        return true;
    }

    @Override
    protected void success(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();

        String firstEntry = this.getData().getOption(DialogueOption.class).get().getText().getFirst();
        // TODO: support multiple entries ^

        // send message
        player.sendMessage(
            String.format("<%s> %s", this.npc.getName(), firstEntry)
        );

        // remove the NPC
        this.unplaceNPC(questerData);
    }

    @Override
    protected void failure(QuesterData questerData) {}

    @Override
    protected ActionListener<?> startListener(QuesterData questerData) {        
        return new SpeakListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Makes an NPC speak."))
            .setItem(Material.OAK_SIGN);
    }

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(
            NPCOption.class,
            DialogueOption.class
        );
    }

    @Override
    public Optional<String> isValid() {
        if (this.getData().getOption(NPCOption.class).get().getNPC() == null) {
            return Optional.of("NPC is missing, try choosing an NPC in the action options.");
        }

        if (this.getData().getOption(DialogueOption.class).get().getText().isEmpty()) {
            return Optional.of("Dialogue is missing, try setting some dialogue for the NPC to speak.");
        }

        return Optional.empty();
    }

    @Override
    public List<Class<? extends ActionCondition>> getConditions() {
        return List.of(
            TimeCondition.class,
            CompletionCondition.class
        );
    }

    @Override
    protected Class<?> getListenerType() {
        return SpeakListener.class;
    }

    @Override
    public LocationData getLocation() {
        return new LocationData(this.npc.getLocation());
    }

    @Override
    public List<ActionTweaks> getTweaks() {
        return List.of();
    }
}
