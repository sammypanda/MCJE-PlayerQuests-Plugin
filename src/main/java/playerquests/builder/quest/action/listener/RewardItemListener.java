package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;

import playerquests.Core;
import playerquests.builder.quest.action.RewardItemAction;
import playerquests.builder.quest.data.QuesterData;

public class RewardItemListener extends ActionListener<RewardItemAction> {

    public RewardItemListener(RewardItemAction action, QuesterData questerData) {
        super(action, questerData);

        // delay to ease if quest creator chooses to loop
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            action.check(questerData);
        }, 100);
    }
    
}
