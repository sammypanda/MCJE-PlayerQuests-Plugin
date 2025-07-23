package playerquests.client.chat.command;

import org.bukkit.Bukkit; // getting the defined plugin commands
import org.bukkit.command.Command; // represents commands
import org.bukkit.command.CommandSender; // the entity who sends the command
import org.bukkit.command.TabExecutor; // how commands are executed (w/ tab completion)

/**
 * Organises the framework for all the commands.
 */
public abstract class ChatCommand implements TabExecutor {

    /**
     * To allow the command classes to construct themselves as the command executor.
     * @param commandName the key for the command as defined in the plugin.yml
     */
    protected ChatCommand(String commandName) {
        Bukkit.getServer().getPluginCommand(commandName).setExecutor(this);
    }

    /**
     * Delegate the execution to the specific command class.
     * @param sender the player which typed the command
     * @param command the command itself
     * @param label the alias of the command used
     * @param args the arguments passed in by the player
     * @return true or false, see {@link #onCommand(CommandSender, Command, String, String[])}
     */
    public abstract boolean execute(CommandSender sender, Command command, String label, String[] args);

    /**
     * Ran when a command is received by the server.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return execute(sender, command, label, args);
    }
}