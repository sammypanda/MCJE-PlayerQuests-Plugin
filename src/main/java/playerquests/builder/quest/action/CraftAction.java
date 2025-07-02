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
import playerquests.builder.quest.action.listener.CraftListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.ItemsOption;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;

/**
 * An action that waits until something is crafted. :D      
 */
public class CraftAction extends QuestAction {

    /**
     * the NPC added into the world.
     */
    QuestNPC npc;

    /**
     * Constructor for jackson.
     */
    public CraftAction() {}

    /**
     * Skips the currently tasked quest action.
     * @param stage stage this action belongs to
     */
    public CraftAction(QuestStage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return "Craft";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();

        player.sendMessage(
            String.format("\n<%s>", "Craft these items to continue")
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
        return true; // tracked by listener; maybe not the best idea
    }

    @Override
    protected void success(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer();
        ItemsOption itemsOption = this.getData().getOption(ItemsOption.class).get();

        player.sendMessage(
            String.format("\n<%s>", "Items successfully crafted")
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
        return new CraftListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Waits until an item is crafted."))
            .setItem(Material.CRAFTING_TABLE);
    }

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(
            ItemsOption.class // TODO: might need to be singular?
        );
    }

    @Override
    public Optional<String> isValid() {
        return Optional.empty();
    }

    @Override
    public List<Class<? extends ActionCondition>> getConditions() {
        return List.of(
            TimeCondition.class,
            ActionCondition.class,
            CompletionCondition.class
        );
    }

    @Override
    protected Class<?> getListenerType() {
        return CraftListener.class;
    }

    @Override
    public LocationData getLocation() {
        return null;
    }

    @Override
    public List<ActionTweaks> getTweaks() {
        return List.of(
            ActionTweaks.NO_FX
        );
    }
}
