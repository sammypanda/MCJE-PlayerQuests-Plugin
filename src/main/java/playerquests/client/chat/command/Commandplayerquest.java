package playerquests.client.chat.command;

import org.bukkit.Bukkit; // getting the server singleton
import org.bukkit.command.Command; // represents commands
import org.bukkit.command.CommandSender; // entity who sends the command
import org.bukkit.entity.HumanEntity; // player the playerquests main gui should open for

/**
 * Shows the main GUI window upon the /playerquest command.
 */
public class Commandplayerquest extends ChatCommand {

    /**
     * Sets the command as '/playerquests'.
     */
    public Commandplayerquest() {
        // define which command
        super("playerquests");
    }

    /**
     * Shows the main GUI window to the player who sent the command.
     */
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        HumanEntity humanEntity = Bukkit.getServer().getPlayer(sender.getName());
        // TODO: open main GUI window
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }
    
}
