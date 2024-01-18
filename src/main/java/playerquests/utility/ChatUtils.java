package playerquests.utility;

import java.util.stream.IntStream; // to dynamically create a sized array

import org.bukkit.ChatColor; // used to colour and format the chat messages!
import org.bukkit.entity.HumanEntity; // identifies a player to send messages to  

import playerquests.Core; // used to access the plugin and get all online players

/**
 * Helpful tools which can reduce the verbosity of Chat-related classes and methods.
 */
public class ChatUtils {

    /**
     * ChatUtils should not be instantiated.
     */
    private ChatUtils() {
        throw new AssertionError("ChatUtils should not be instantiated.");
    }

    /**
     * Creates an array filled with 100 newline value elements.
     * @return an array of 100 elements.
     */
    private static String[] newlineArray() {
        return newlineArray(0, 100);
    }

    /**
     * Creates an array with a determinable amount of newline value elements.
     * @param start the initial index (inclusive)
     * @param end the upper bound index (exclusive)
     * @return an array of a range of elements.
     */
    private static String[] newlineArray(Integer start, Integer end) {
        return IntStream.range(start, end).mapToObj(i -> "\n").toArray(String[]::new);
    }

    /**
     * For all players: Uses an array of 100 newline values to clear the chat.
     */
    public static void clearChat() {
        clearChat(100);
    }

    /**
     * For all players: Uses an array of newline values to clear chat lines.
     * @param lines number of lines to clear
     */
    public static void clearChat(Integer lines) {
        Core.getPlugin().getServer().getOnlinePlayers().stream().forEach(player -> {
            clearChat(player, lines);
        });
    }

    /**
     * For one player: Uses an array of 100 newline values to clear the chat.
     * @param player the player to clear the chat for
     */
    public static void clearChat(HumanEntity player) {
        player.sendMessage(newlineArray());
    }

    /**
     * For one player: Uses an array of newline values to clear chat lines.
     * @param player the player to clear the lines for
     * @param lines number of lines to clear
     */
    public static void clearChat(HumanEntity player, Integer lines) {
        player.sendMessage(newlineArray(0, lines));
    } 

    /**
     * Formatting for an error message to a player.
     * @param player the player to send error message to
     * @param content what the error message should say
     */
    public static void sendError(HumanEntity player, String content) {
        System.err.println("sent error: " + content + " to player " + player.getName());
        player.sendMessage("\n" + ChatColor.BOLD + "Error: " + ChatColor.RED + content + ChatColor.RESET + "\n");
    }
}