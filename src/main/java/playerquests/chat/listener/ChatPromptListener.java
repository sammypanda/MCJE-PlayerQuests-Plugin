package playerquests.chat.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import playerquests.gui.function.ChatPrompt;

public class ChatPromptListener implements Listener {

    private ChatPrompt parentClass;

    public ChatPromptListener(ChatPrompt parent) {
        this.parentClass = parent;
    }

    @EventHandler
    private void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        this.parentClass.setResponse(event.getMessage());
        this.parentClass.execute();
    }
}