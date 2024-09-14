package playerquests.builder.quest.action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;

/**
 * Action for giving items to the quester.
 */
public class RewardItem extends QuestAction {

    /**
     * Default constructor (for Jackson)
     */
    public RewardItem() {}

    /**
     * Gives items to the player.
     * @param stage stage this action belongs to
     */
    public RewardItem(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        return Arrays.asList(
            ActionOption.ITEMS
        );
    }

    @Override
    public void Run(QuestClient quester) {
        Player player = quester.getPlayer();
        Location location = player.getLocation();

        // send messages
        player.sendMessage(" ");
        player.sendMessage("# Rewarding Items");
        this.items.forEach((material, count) -> {
            player.sendMessage("# " + material.name() + " (" + count + ")");
        });
        player.sendMessage(" ");

        // drop items at player
        this.items.forEach((material, count) -> {
            ItemStack item = new ItemStack(material, count);
            location.getWorld().dropItem(location, item);
        });

        // continue to next action
        quester.gotoNext(this);
    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }
    
}
