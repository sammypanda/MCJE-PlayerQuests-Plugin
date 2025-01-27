package playerquests.client.chat.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import playerquests.Core;

/**
 * Command for working with quest actions.
 */
public class Commandaction extends ChatCommand {

    /**
     * Sets the command as '/action'.
     */
    public Commandaction() {
        super("action");
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // TODO: implementing actually starting the action
        Bukkit.broadcastMessage("unfinished: starting " + args[1]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final boolean isPlayer = sender instanceof Player;
        final Player player = Bukkit.getPlayer(sender.getName());

        switch (args.length) {
            case 1:
                return List.of("start", "stop");

            case 2:
                if (!isPlayer) {
                    return List.of();
                }

                // return a list of currently relevant paths for the quest player
                return Core.getQuestRegistry().getQuester(player).getDiary().getQuestProgress().entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream()
                        .map(path -> String.format("%s.%s", entry.getKey().getID(), path.toString())))
                    .toList();
        }

        // no tab complete
        return List.of();
    }
    
}
