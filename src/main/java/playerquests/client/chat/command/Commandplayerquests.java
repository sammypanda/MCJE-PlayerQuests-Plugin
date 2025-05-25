package playerquests.client.chat.command;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit; // getting the server singleton
import org.bukkit.GameMode;
import org.bukkit.command.Command; // represents commands
import org.bukkit.command.CommandSender; // entity who sends the command
import org.bukkit.entity.Player;

import playerquests.client.gui.GUIClient; // for controlling the plugin over a GUI
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageType;

/**
 * Shows the main GUI window upon the /playerquest command.
 */
public class Commandplayerquests extends ChatCommand {

    /**
     * Sets the command as '/playerquests'.
     */
    public Commandplayerquests() {
        // define which command
        super("playerquests");
    }

    /**
     * Shows the main GUI window to the player who sent the command.
     */
    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Player player = Bukkit.getServer().getPlayer(sender.getName()); // get the player who sent the command
        List<GameMode> allowedGameModes = Arrays.asList(GameMode.ADVENTURE, GameMode.SURVIVAL); // the gamemodes that don't cause breakages

        // if not an allowed GameMode
            // CREATIVE DOES NOT WORK WITH PLAYERQUESTS:
            // for some reason inventory stuff gets all out of wack
            // when in creative. I could try to create different ways 
            // of doing things for players in creative, but playerquests is
            // survival-based so it seems a bit niche.
        if (!allowedGameModes.contains(player.getGameMode())) {
            ChatUtils.message("PlayerQuests does not work outside of survival.")
                .type(MessageType.WARN)
                .style(MessageStyle.PRETTY)
                .player(player)
                .send();
            player.setGameMode(GameMode.SURVIVAL); // set into survival
        }

        GUIClient guiClient = new GUIClient(player); // create a new gui client
        guiClient.open(); // open the gui client (main GUI)
        return true; // mark as a valid command
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return List.of();
    }
    
}
