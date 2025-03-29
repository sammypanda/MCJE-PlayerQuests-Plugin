package playerquests.builder.quest.action;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.CompletionCondition;
import playerquests.builder.quest.action.condition.TimeCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.RequestItemListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;

/**
 * Action for an NPC requesting an item from the quester.
 */
public class RequestItemAction extends QuestAction {

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(); // TODO: create an item condition
    }

    @Override
    public List<Class<? extends ActionCondition>> getConditions() {
        return List.of(
            TimeCondition.class,
            CompletionCondition.class
        );
    }

    @Override
    public List<ActionTweaks> getTweaks() {
        return List.of();
    }

    @Override
    public String getName() {
        return "Request Item";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        // TODO: hmm
    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        return true; // TODO: hmm
    }

    @Override
    protected Class<?> getListenerType() {
        return RequestItemListener.class;
    }

    @Override
    protected void success(QuesterData questerData) {}

    @Override
    protected void failure(QuesterData questerData) {}

    @Override
    protected ActionListener<?> startListener(QuesterData questerData) {
        return new RequestItemListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Waits for an item ", "to be collected."))
            .setItem(Material.ITEM_FRAME);
    }

    @Override
    public Optional<String> isValid() {
        return Optional.empty();
    }

    @Override
    public LocationData getLocation() {
        // TODO: if no location uhhh null?

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLocation'");
    }
}
