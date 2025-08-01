package playerquests.client.chat.command;

import java.util.Arrays;
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

    /**
     * Used for decoding the intended action from command string
     */
    public record Target(String questID, String pathCode) {}

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        final boolean isPlayer = sender instanceof Player;

        // exit if not a player
        if (!isPlayer) {
            return false;
        }

        // invalid if not enough args
        if (args.length <= 1) {
            return false;
        }

        final String keyword = args[0].toLowerCase();
        final Player player = Bukkit.getPlayer(sender.getName());
        final QuestClient quester = Core.getQuestRegistry().getQuester(player);

        switch (keyword) {
            case "start":
                return this.startAction(args, quester);
            case "consent":
                return this.setConsent(args, quester);
            default:
                return false;
        }
    }

    private boolean startAction(String[] args, QuestClient quester) {
        Target target = this.actionTargetDecode(args);

        // get quest and get stage path
        Quest quest = QuestRegistry.getInstance().getQuest(target.questID());
        StagePath path = new StagePath(target.pathCode());

        // start the relevant action
        path.getActions(quest).forEach(action -> {
            quester.start(action, false);
        });

        return true;
    }

    private boolean setConsent(String[] args, QuestClient quester) {
        Target target = this.actionTargetDecode(args);

        // get quest and get stage path
        Quest quest = QuestRegistry.getInstance().getQuest(target.questID());
        StagePath path = new StagePath(target.pathCode());

        // for the relevant actions
        path.getActions(quest).forEach(action -> {
            // set as consented
            quester.getData().setConsent(action, true);
        });

        return true;
    }

    private Target actionTargetDecode(String[] args) {
        /*
         * tolerate spaces to find the *real* args[1] (the quest ID + paths)
         * - reference example we'll use: "name of quest_205295.stage_0.action_0" 
        */
        // split closest to the separate parts as we can get
        String partialQuestID = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)); // for example: gets 'name of' (but not) ..quest_205295.stage_0.action_0
        String partialStagePath = args[args.length - 1]; // for example: gets quest_205295.stage_0.action_0

        // split up on .
        String[] resolvePartial = partialStagePath.split("\\."); // for example: gets ['quest_205295', 'stage_0', 'action_0']

        // resolve the separate parts :D and enjoy the fruits of the complexity
        return new Target(
            String.format("%s %s", partialQuestID, resolvePartial[0]).trim(), // for example: joins 'name of' with 'quest_205295' with a whitespace
            String.format("%s.%s", resolvePartial[1], resolvePartial[2]).trim() // for example: joins 'stage_0' and 'action_0' with a '.'
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final boolean isPlayer = sender instanceof Player;
        final Player player = Bukkit.getPlayer(sender.getName());

        switch (args.length) {
            case 1:
                return List.of("start", "consent");
            case 2:
                if (!isPlayer) {
                    return List.of();
                }

                // return a list of currently relevant paths for the quest player
                return Core.getQuestRegistry().getQuester(player).getDiary().getQuestProgress().entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream()
                        .map(path -> String.format("%s.%s", entry.getKey().getID(), path.toString())))
                    .toList();
            default:
                // no tab complete
                return List.of();
        }
    }
    
}
