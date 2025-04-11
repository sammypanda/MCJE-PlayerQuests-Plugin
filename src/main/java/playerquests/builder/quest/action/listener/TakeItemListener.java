package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;

import playerquests.Core;
import playerquests.builder.quest.action.TakeItemAction;
import playerquests.builder.quest.data.QuesterData;

public class TakeItemListener extends ActionListener<TakeItemAction> {

    public TakeItemListener(TakeItemAction action, QuesterData questerData) {
        super(action, questerData);

        // delay to ease if quest creator chooses to loop
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            action.check(questerData);
        }, 20);
    }
    
}
