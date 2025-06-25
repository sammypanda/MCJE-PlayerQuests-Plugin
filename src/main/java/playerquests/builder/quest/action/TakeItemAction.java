package playerquests.builder.quest.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.condition.CompletionCondition;
import playerquests.builder.quest.action.condition.TimeCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.TakeItemListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.ItemsOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.product.Quest;
import playerquests.utility.ChatUtils;
import playerquests.utility.serialisable.ItemSerialisable;
import playerquests.utility.singleton.QuestRegistry;

public class TakeItemAction extends QuestAction {

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
        return "Take Item";
    }

    @Override
    /**
     * - prompt for consenting
     */
    protected void prepare(QuesterData questerData) {
        // compose the command for consenting
        final Player player = questerData.getQuester().getPlayer();
        final QuestStage questStage = this.getStage();
        final Quest quest = questStage.getQuest();
        final String path = new StagePath(questStage, List.of(this)).toString(); // the path to the action
        final String command = String.format("/action consent %s.%s", quest.getID(), path); // command that resolves the clash?
        
        Builder message = Component.text()
            .appendNewline()
            .append(Component.text(
                String.format("The '%s' quest is requesting to take items", quest.getTitle())
            ))
            .color(NamedTextColor.GRAY)
            .appendNewline();

        // List the items
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();
        itemsOption.getItems().forEach((item, amount) -> {
            message.append(
                Component.text(String.format("- %s (%d)", item.getName(), amount))
                    .color(NamedTextColor.WHITE)
            ).appendNewline();
        });

        // Add the click functionality
        message
            .appendNewline()
            .append(Component.text("> "))
            .append(Component.text("Click here to proceed")
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command))
            )
            .build();

        ChatUtils.message(message.asComponent()).player(player).send(); // send request for consent
    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        if ( ! questerData.getConsent(this)) {
            return false;
        }

        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();
        return ItemSerialisable.hasRequiredItems(player, itemsOption.getItems());
    }

    @Override
    protected Class<?> getListenerType() {
        return TakeItemListener.class;
    }

    @Override
    protected void success(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        Inventory playerInventory = player.getInventory();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();
        Quest quest = this.getStage().getQuest();

        itemsOption.getItems().forEach((itemSerialisable, amount) -> {
            ItemStack itemStack = itemSerialisable.toItemStack();
            itemStack.setAmount(amount);

            playerInventory.removeItem(itemStack);
            QuestRegistry.getInstance().updateInventoryItem(quest, Map.of(itemSerialisable, amount));
        });

        player.sendMessage(
            String.format("\n<%s>", "Taking item")
        );

        itemsOption.getItems().forEach((item, amount) -> {
            player.sendMessage(
                String.format("- %s (%d)", item.getName(), amount)
            );
        });

        player.sendMessage("");
        questerData.setConsent(this, false); // revert to asking; could set consent to timeout instead
    }

    @Override
    protected void failure(QuesterData questerData) {
        // restart listener
        questerData.stopListener(this);
        this.startListener(questerData);
    }

    @Override
    protected ActionListener<?> startListener(QuesterData questerData) {
        return new TakeItemListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Takes items from the ", "quester."))
            .setItem(Material.FLOWER_POT);
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
