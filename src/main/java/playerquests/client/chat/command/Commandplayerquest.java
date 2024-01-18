package playerquests.client.chat.command;

import org.bukkit.Bukkit; // getting the server singleton
import org.bukkit.command.Command; // represents commands
import org.bukkit.command.CommandSender; // entity who sends the command
import org.bukkit.entity.HumanEntity; // player the playerquests main gui should open for

import playerquests.client.gui.GUIClient; // for controlling the plugin over a GUI

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
        HumanEntity player = Bukkit.getServer().getPlayer(sender.getName()); // get the player who sent the command
        GUIClient guiClient = new GUIClient(player); // create a new gui client
        guiClient.open(); // open the gui client (main GUI)
        return true; // mark as a valid command
    }
    
}
