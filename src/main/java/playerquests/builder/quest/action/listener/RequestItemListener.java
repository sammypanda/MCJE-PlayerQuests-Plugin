package playerquests.builder.quest.action.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;

import playerquests.Core;
import playerquests.builder.quest.action.RequestItemAction;
import playerquests.builder.quest.data.QuesterData;

/**
 * Listens for if the request item has been collected.
 */
public class RequestItemListener extends ActionListener<RequestItemAction> {

    public RequestItemListener(RequestItemAction action, QuesterData questerData) {
        super(action, questerData);
        action.check(questerData);
    }

    @EventHandler
    private void onItemPickup(EntityPickupItemEvent event) {
        if ( ! passedPlayerCheck((Player) event.getEntity())) { return; }

        // delay to include picked up item
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            action.check(questerData);
        }, 5);
    }
}
