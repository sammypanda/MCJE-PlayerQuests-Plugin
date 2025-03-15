package playerquests.utility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream; // to dynamically create a sized array

import javax.annotation.Nullable;

import org.bukkit.Bukkit; // used to get server logger
import org.bukkit.ChatColor; // used to colour and format the chat messages!
import org.bukkit.entity.HumanEntity; // identifies a player to send messages to  

import playerquests.Core; // used to access the plugin and get all online players

/**
 * Provides utility methods for handling chat-related functionality in a Bukkit plugin.
 * 
 * This class includes tools for sending formatted messages to players, clearing chat, and checking for specific keywords.
 * It is designed to reduce verbosity in chat-related classes and methods.
 */
public class ChatUtils {

    /**
     * Enumeration defining different message types.
     * 
     * Each type includes a prefix and a color for message formatting.
     */
    public enum MessageType {
        /**
         * Notification type message.
         */
        NOTIF("[PlayerQuests]", ChatColor.GRAY),

        /**
         * Warning type message.
         */
        WARN("[PlayerQuests] ⚠ Help", ChatColor.YELLOW),

        /**
         * Error type message.
         */
        ERROR("[PlayerQuests] 🚫 Error", ChatColor.RED);

        private final String prefix;
        private final ChatColor color;

        MessageType(String prefix, ChatColor color) {
            this.prefix = prefix;
            this.color = color;
        }
    }

    /**
     * Enumeration defining different message targets.
     * 
     * Each target specifies where the message should be sent: console, world, or player.
     */
    public enum MessageTarget {
        /**
         * Target for sending messages to the console.
         */
        CONSOLE {
            @Override
            public void send(String formattedMessage, @Nullable HumanEntity player) {
                Bukkit.getConsoleSender().sendMessage(formattedMessage);
            }
        },

        /**
         * Target for sending messages to all players in the world.
         */
        WORLD {
            @Override
            public void send(String formattedMessage, @Nullable HumanEntity player) {
                Bukkit.broadcastMessage(formattedMessage);
            }
        },

        /**
         * Target for sending messages to a specific player.
         */
        PLAYER {
            @Override
            public void send(String formattedMessage, @Nullable HumanEntity player) {
                if (player == null) {
                    ChatUtils.message("MessageTarget.PLAYER did not pass in player, for message: " + formattedMessage)
                        .type(MessageType.ERROR)
                        .send();
                    return;
                }

                player.sendMessage(formattedMessage);
            }
        };        
    
        /**
         * Sends a formatted message to the target.
         * 
         * @param formattedMessage the message to send
         * @param player the player to send the message to, or null if not applicable
         */
        public abstract void send(String formattedMessage, @Nullable HumanEntity player);
    }

    /**
     * Enumeration defining different message styles.
     * 
     * Each style specifies how the message should be formatted.
     */
    public enum MessageStyle {
        /**
         * Pretty style with bold prefix and formatted message.
         */
        PRETTY {
            @Override
            public String formatMessage(String content, MessageType type) {
                return String.format("\n%s%s:%s %s%s\n", 
                    ChatColor.BOLD,
                    type.prefix,
                    type.color,
                    content,
                    ChatColor.RESET
                );
            }
        },

        /**
         * Simple style with a basic prefix and formatted message.
         */
        SIMPLE {
            @Override
            public String formatMessage(String content, MessageType type) {
                return String.format("%s:%s %s%s",
                    type.prefix,
                    type.color,
                    content,
                    ChatColor.RESET
                );
            }
        },

        /**
         * Plain style with a basic prefix and message.
         */
        PLAIN {
            @Override
            public String formatMessage(String content, MessageType type) {
                return String.format("%s: %s",
                    type.prefix,
                    content
                );
            }
        };
        
        /**
         * Formats a message with the specified content and message type.
         * 
         * @param content the message content
         * @param type the type of the message
         * @return the formatted message
         */
        public abstract String formatMessage(String content, MessageType type);
    }

    /**
     * Builder class for creating and sending messages.
     * 
     * Allows for customization of message content, type, target, style, and recipient.
     */
    public static class MessageBuilder {
        private String content;
        private MessageType type = MessageType.NOTIF; // Default
        private MessageTarget target = MessageTarget.WORLD; // Default
        private MessageStyle style = MessageStyle.PRETTY; // Default
        private HumanEntity player = null; // Default

        /**
         * Constructs a MessageBuilder with the specified content.
         * 
         * Defaults to MessageType.NOTIF, MessageTarget.WORLD, and MessageStyle.PRETTY.
         * 
         * @param content the message to send
         */
        public MessageBuilder(String content) {
            this.content = content;
        }

        /**
         * Sets the content of the message.
         * 
         * @param baseMessage the new message content
         * @return this MessageBuilder
         */
        public MessageBuilder content(String baseMessage) {
            this.content = baseMessage;
            return this;
        }
        
        /**
         * Gets the content of the message.
         * 
         * @return the string message content.
         */
        public String getContent() {
            return this.content;
        }

        /**
         * Sets the type of the message.
         * 
         * @param messageType the MessageType enum
         * @return this MessageBuilder
         */
        public MessageBuilder type(MessageType messageType) {
            this.type = messageType;
            return this;
        }

        /**
         * Sets the target of the message.
         * 
         * @param messageTarget the MessageTarget enum
         * @return this MessageBuilder
         */
        public MessageBuilder target(MessageTarget messageTarget) {
            this.target = messageTarget;
            return this;
        }

        /**
         * Sets the player to send the message to.
         * Assumes MessageTarget is PLAYER.
         * 
         * @param player the HumanEntity which represents a player
         * @return this MessageBuilder
         */
        public MessageBuilder player(HumanEntity player) {
            this.target = MessageTarget.PLAYER;
            this.player = player;
            return this;
        }

        /**
         * Sets the style of the message.
         * 
         * @param messageStyle the MessageStyle enum
         * @return this MessageBuilder
         */
        public MessageBuilder style(MessageStyle messageStyle) {
            this.style = messageStyle;
            return this;
        }

        /**
         * Sends the constructed message to the target.
         */
        public void send() {
            String formattedMessage = style.formatMessage(content, type);
            target.send(formattedMessage, this.player);
        }
    }

    /**
     * Prevents instantiation of ChatUtils.
     * 
     * Throws an AssertionError if attempted.
     */
    private ChatUtils() {
        throw new AssertionError("ChatUtils should not be instantiated.");
    }

    /**
     * Creates an array filled with 100 newline value elements.
     * 
     * @return an array of 100 newline elements
     */
    private static String[] newlineArray() {
        return newlineArray(0, 100);
    }

    /**
     * Creates an array with a specified number of newline value elements.
     * 
     * @param start the initial index (inclusive)
     * @param end the upper bound index (exclusive)
     * @return an array of newline elements
     */
    private static String[] newlineArray(Integer start, Integer end) {
        return IntStream.range(start, end).mapToObj(_ -> "\n").toArray(String[]::new);
    }

    /**
     * For all players: Uses an array of 100 newline values to clear the chat.
     */
    public static void clearChat() {
        clearChat(100);
    }

    /**
     * For all players: Uses an array of newline values to clear chat lines.
     * 
     * @param lines number of lines to clear
     */
    public static void clearChat(Integer lines) {
        Core.getPlugin().getServer().getOnlinePlayers().stream().forEach(player -> {
            clearChat(player, lines);
        });
    }

    /**
     * For one player: Uses an array of 100 newline values to clear the chat.
     * 
     * @param player the player to clear the chat for
     */
    public static void clearChat(HumanEntity player) {
        player.sendMessage(newlineArray());
    }

    /**
     * For one player: Uses an array of newline values to clear chat lines.
     * 
     * @param player the player to clear the lines for
     * @param lines number of lines to clear
     */
    public static void clearChat(HumanEntity player, Integer lines) {
        player.sendMessage(newlineArray(0, lines));
    }

    /**
     * Sends a message with the bare minimum input.
     * If you would like to customise what is being sent
     * use ChatUtils.message() to access the MessageBuilder
     * 
     * @param content what message to send (sends as the 
     * MessageBuilder defaults)
     */
    public static void send(String content) {
        ChatUtils.message(content)
            .send();
    }

    /**
     * Neatly accesses the message builder.
     * 
     * @param content the bare minimum message
     * @return a message builder to customise the message
     */
    public static MessageBuilder message(String content) {
        return new MessageBuilder(content);
    }

    /**
     * A list of the exit keywords.
     * Stored in capitals, so to compare use toUpperCase().
     * 
     * @return a list of exit keywords
     */
    public static List<String> getExitKeywords() {
        return Arrays.asList("EXIT", "E", "X");
    }

    /**
     * A list of the confirm keywords.
     * Stored in capitals, so to compare use toUpperCase().
     * 
     * @return a list of confirm keywords
     */
    public static List<String> getConfirmKeywords() {
        return Arrays.asList("CONFIRM", "C");
    }

    /**
     * Check if a string is an exit keyword.
     * 
     * @param keyword the string that might be an exit keyword
     * @return whether the keyword is or isn't indicating an exit
     */
    public static Boolean isExitKeyword(String keyword) {
        return getExitKeywords().contains(keyword.toUpperCase());
    }

    /**
     * Check if a string is a confirm keyword.
     * 
     * @param keyword the string that might be a confirm keyword
     * @return whether the keyword is or isn't indicating a confirm
     */
    public static Boolean isConfirmKeyword(String keyword) {
        return getConfirmKeywords().contains(keyword.toUpperCase());
    }

    /**
     * Cut a string down and replace the end with an elipsis.
     * Magical :D
     * @param string the string to cut down
     * @param targetLength what amount of the string to show
     * @return
     */
    public static String shortenString(String string, Integer targetLength) {
        return string.length() > targetLength - 1 ? string.substring(0, targetLength) + "..." : string;
    }
}