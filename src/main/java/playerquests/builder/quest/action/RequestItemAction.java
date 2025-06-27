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
import playerquests.builder.quest.action.listener.RequestItemListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.ItemsOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.utility.serialisable.ItemSerialisable;

/**
 * Action for an NPC requesting an item from the quester.
 */
public class RequestItemAction extends QuestAction {

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(
            ItemsOption.class
        );
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
        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();

        player.sendMessage(
            String.format("\n<%s>", "Items requested")
        );

        itemsOption.getItems().forEach((item, amount) -> {
            player.sendMessage(
                String.format("- %s (%d)", item.getName(), amount)
            );
        });

        player.sendMessage("");

    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();
        return ItemSerialisable.hasRequiredItems(player, itemsOption.getItems());
    }

    @Override
    protected Class<?> getListenerType() {
        return RequestItemListener.class;
    }

    @Override
    protected void success(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();

        player.sendMessage(
            String.format("\n<%s>", "Items successfully collected")
        );

        itemsOption.getItems().forEach((item, amount) -> {
            player.sendMessage(
                String.format("- %s (%d)", item.getName(), amount)
            );
        });

        player.sendMessage("");
    }

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
        if (this.getData().getOption(ItemsOption.class).get().getItems().isEmpty()) {
            return Optional.of("No items are set, try choosing some items in the action options.");
        }

        return Optional.empty();
    }

    @Override
    public LocationData getLocation() {
        return null;
    }
}
