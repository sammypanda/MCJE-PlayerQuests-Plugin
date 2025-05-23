package playerquests.builder.quest.action;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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

        ComponentBuilder message = new ComponentBuilder(String.format("\nThe '%s' quest is requesting to take items\n", quest.getTitle()));
            
        // list the items
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();
        itemsOption.getItems().forEach((material, amount) -> {
            message.append(String.format("- %s (%d)", material, amount)).color(ChatColor.GRAY);
        });


        // add the click functionality
        message
            // .append("Do you consent?\n\n").color(ChatColor.GRAY)
            .append("\n> ").reset()
            .append("Click to proceed").color(ChatColor.GREEN).underlined(true)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

        player.spigot().sendMessage(message.build()); // send request for consent
    }

    @Override
    protected Boolean isCompleted(QuesterData questerData) {
        if ( ! questerData.getConsent(this)) {
            return false;
        }

        Inventory playerInventory = questerData.getQuester().getPlayer().getInventory();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();

        return itemsOption.getItems().entrySet().stream().allMatch(entry -> playerInventory.contains(entry.getKey(), entry.getValue()));
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

        itemsOption.getItems().forEach((material, count) -> {
            playerInventory.removeItem(new ItemStack(material, count));
            QuestRegistry.getInstance().updateInventoryItem(quest, Map.of(material, count));
        });

        player.sendMessage(
            String.format("\n<%s>", "Taking item")
        );

        itemsOption.getItems().forEach((material, amount) -> {
            player.sendMessage(
                String.format("- %s (%d)", material, amount)
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
