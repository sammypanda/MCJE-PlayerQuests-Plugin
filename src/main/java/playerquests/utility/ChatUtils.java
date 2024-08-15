package playerquests.utility;

import java.util.stream.IntStream; // to dynamically create a sized array

import javax.annotation.Nullable;

import org.bukkit.Bukkit; // used to get server logger
import org.bukkit.ChatColor; // used to colour and format the chat messages!
import org.bukkit.entity.HumanEntity; // identifies a player to send messages to  

import playerquests.Core; // used to access the plugin and get all online players

/**
 * Helpful tools which can reduce the verbosity of Chat-related classes and methods.
 */
public class ChatUtils {

    /**
     * What should be sent
     */
    public enum MessageType {
        NOTIF("[PlayerQuests]", ChatColor.GRAY),
        WARN("[PlayerQuests] ⚠ Help", ChatColor.YELLOW),
        ERROR("[PlayerQuests] 🚫 Error", ChatColor.RED);

        private final String prefix;
        private final ChatColor color;

        MessageType(String prefix, ChatColor color) {
            this.prefix = prefix;
            this.color = color;
        }
    }

    /**
     * Who it should be send to
     */
    public enum MessageTarget {
        CONSOLE {
            @Override
            public void send(String formattedMessage, @Nullable HumanEntity player) {
                Bukkit.getConsoleSender().sendMessage(formattedMessage);
            }
        },
        WORLD {
            @Override
            public void send(String formattedMessage, @Nullable HumanEntity player) {
                Bukkit.broadcastMessage(formattedMessage);
            }
        },        
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
        
        public abstract void send(String formattedMessage, @Nullable HumanEntity player);
    }

    /**
     * How it should look
     */
    public enum MessageStyle {
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
        PLAIN {
            @Override
            public String formatMessage(String content, MessageType type) {
                return String.format("%s: %s",
                    type.prefix,
                    content
                );
            }
        };
        
        public abstract String formatMessage(String content, MessageType type);
    }

    /**
     * MessageBuilder inner class
     */
    public static class MessageBuilder {
        private String content;
        private MessageType type = MessageType.NOTIF; // Default
        private MessageTarget target = MessageTarget.WORLD; // Default
        private MessageStyle style = MessageStyle.PRETTY; // Default
        private HumanEntity player = null; // Default

        /**
         * Constructer for the message.
         * Defaults to: NOTIF, WORLD, PRETTY.
         * @param content the message to send
         */
        public MessageBuilder(String content) {
            this.content = content;
        }

        /**
         * For editing the base message.
         * @param the potatos of what the message is
         */
        public MessageBuilder content(String baseMessage) {
            this.content = baseMessage;
            return this;
        }

        /**
         * For adding a message type
         * @param messageType the MessageType enum
         * @return the MessageBuilder to chain next function.
         */
        public MessageBuilder type(MessageType messageType) {
            this.type = messageType;
            return this;
        }

        public MessageBuilder target(MessageTarget messageTarget) {
            this.target = messageTarget;
            return this;
        }

        /**
         * Sets the player to send a message to.
         * Assumes MessageTarget is PLAYER.
         * @param player the HumanEntity which represents a player
         * @return this MessageBuilder
         */
        public MessageBuilder player(HumanEntity player) {
            this.target = MessageTarget.PLAYER;
            this.player = player;
            return this;
        }

        public MessageBuilder style(MessageStyle messageStyle) {
            this.style = messageStyle;
            return this;
        }

        public void send() {
            String formattedMessage = style.formatMessage(content, type);
            target.send(formattedMessage, this.player);
        }
    }

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
     * Sends a message with the bare minimum input.
     * If you would like to customise what is being sent
     * use ChatUtils.message() to access the MessageBuilder
     * @param content what message to send (sends as the 
     * MessageBuilder defaults)
     */
    public static void send(String content) {
        ChatUtils.message(content)
            .send();
    }

    /**
     * Neatly accesses the message builder.
     * @param content the bare minimum message
     * @return a message builder to customise the message
     */
    public static MessageBuilder message(String content) {
        return new MessageBuilder(content);
    }
}