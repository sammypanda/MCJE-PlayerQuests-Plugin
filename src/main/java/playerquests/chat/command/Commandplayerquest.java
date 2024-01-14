package playerquests.chat.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;

import playerquests.quest.Quest;

/**
 * Opens the plugin menu on a players screen.
 */
public class Commandplayerquest extends ChatCommand {
    
    /**
     * Loads the /playerquests command.
     */
    public Commandplayerquest() {
        super("playerquests");
    }

    /**
     * Opens an instance of an empty GUI for the player who sent the command.
     */
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        HumanEntity humanEntity = Bukkit.getServer().getPlayer(sender.getName()); // get the player
        Quest.display(humanEntity); // show the menu for the player
        return true;
    }
}