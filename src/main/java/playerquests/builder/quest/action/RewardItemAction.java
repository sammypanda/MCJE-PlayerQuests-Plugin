package playerquests.builder.quest.action;

import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.CompletionCondition;
import playerquests.builder.quest.action.condition.TimeCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.RewardItemListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.ItemsOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.product.Quest;
import playerquests.utility.singleton.QuestRegistry;

public class RewardItemAction extends QuestAction {

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
        return "Reward Item";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();

        player.sendMessage(
            String.format("\n<%s>", "Dropping reward")
        );

        itemsOption.getItems().forEach((material, amount) -> {
            player.sendMessage(
                String.format("- %s (%d)", material, amount)
            );
        });

        player.sendMessage("");
    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        return true;
    }

    @Override
    protected Class<?> getListenerType() {
        return RewardItemListener.class;
    }

    @Override
    protected void success(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        Location playerLocation = player.getLocation();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();
        Quest quest = this.getStage().getQuest();

        QuestRegistry.getInstance().updateInventoryItem(quest, itemsOption.getItems(), (itemSerialisable, amount) -> {
            ItemStack itemStack = itemSerialisable.toItemStack(); // get an item stack from playerquests item data
            itemStack.setAmount(amount); // set the amount of the above

            playerLocation.getWorld().dropItem(playerLocation, itemStack);
        }, true);
    }

    @Override
    protected void failure(QuesterData questerData) {}

    @Override
    protected ActionListener<?> startListener(QuesterData questerData) {
        return new RewardItemListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Gives the quester ", "items."))
            .setItem(Material.CHEST);
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
