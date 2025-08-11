package playerquests.builder.quest.action;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.CompletionCondition;
import playerquests.builder.quest.action.condition.TimeCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.NarrateListener;
import playerquests.builder.quest.action.listener.SpeakListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.DialogueOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;

/**
 * Action for an NPC speaking.
 */
public class NarrateAction extends QuestAction<NarrateAction, NarrateListener> {

    /**
     * Default constructor for Jackson.
     */
    public NarrateAction() {
        // Nothing here
    }

    @Override
    public String getName() {
        return "Narrate";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        // No preparations needed
    }

    @Override
    protected boolean isCompleted(QuesterData questerData) {
        return true;
    }

    @Override
    protected void success(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();

        this.getData().getOption(DialogueOption.class).ifPresent(dialogue ->
            // send message
            player.sendPlainMessage(
                String.format("%n> %s%n", dialogue.getText().getFirst())
            )
        );
    }

    @Override
    protected void failure(QuesterData questerData) {
        // no failure case
    }

    @Override
    protected NarrateListener startListener(QuesterData questerData) {        
        return new NarrateListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Puts text into the chat."))
            .setItem(Material.SPRUCE_SIGN);
    }

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(
            DialogueOption.class
        );
    }

    @Override
    public List<Class<? extends ActionCondition>> getConditionBlocklist() {
        return List.of();
    }

    @Override
    protected Class<?> getListenerType() {
        return SpeakListener.class;
    }

    @Override
    public LocationData getLocation() {
        return null;
    }

    @Override
    public List<ActionTweaks> getTweaks() {
        return List.of();
    }
}
