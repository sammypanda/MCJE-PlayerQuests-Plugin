package playerquests.client.chat.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import playerquests.Core;
import playerquests.builder.quest.data.StagePath;
import playerquests.client.quest.QuestClient;
import playerquests.product.Quest;
import playerquests.utility.singleton.QuestRegistry;

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
        final boolean isPlayer = sender instanceof Player;

        // exit if not a player
        if (!isPlayer) {
            return false;
        }

        final Player player = Bukkit.getPlayer(sender.getName());
        final QuestClient quester = Core.getQuestRegistry().getQuester(player);

        // invalid if not enough args
        if (args.length <= 1) {
            return false;
        }

        // get parts of the argument pointing to the action
        String[] idParts = args[1].split("\\.");
        String questID = idParts[0];
        String pathCode = String.format("%s.%s", idParts[1], idParts[2]);

        // get quest and get stage path
        Quest quest = QuestRegistry.getInstance().getQuest(questID);
        StagePath path = new StagePath(pathCode);

        // start the relevant action
        quester.start(List.of(path), quest, false);

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
