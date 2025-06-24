package playerquests.builder.gui.function;

import java.util.List; // used to store the params for this gui function

import org.bukkit.Bukkit; // used to access a Scheduler
import org.bukkit.event.EventHandler; // handling spigot events
import org.bukkit.event.HandlerList; // to unregister event listener (ChatPromptListener)
import org.bukkit.event.Listener; // to register event listener (ChatPromptListener)
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.entity.Player; // refers to the player

import playerquests.Core; // used to access the Plugin and KeyHandler instances
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.utility.ChatUtils; // used to clear chat lines for a better UI
import playerquests.utility.PluginUtils; // used to validate function params

/**
 * Creates a prompt for user input. 
 */
public class ChatPrompt extends GUIFunction {

    /**
     * Custom event listener user input.
     */
    private class ChatPromptListener implements Listener {

        /**
         * The ChatPrompt instance.
         */
        private ChatPrompt parentClass;

        /**
         * The player this listener is for.
         */
        private Player player;

        /**
         * Creates a new listener for chat prompt inputs.
         * @param parent the origin ChatPrompt GUI function
         */
        public ChatPromptListener(ChatPrompt parent, Player player) {
            this.parentClass = parent;
            this.player = player;
        }

        @EventHandler
        private void onCommand(PlayerCommandPreprocessEvent event) {
            // do not capture other players events
            if (this.player != event.getPlayer()) {
                return;
            }

            // exit ChatPrompt
            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                this.parentClass.exit();
            });
        }

        /**
         * Sends user input back to ChatPrompt GUI function.
         * @param event
         */
        @EventHandler
        private void onChat(AsyncChatEvent event) {
            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }

            // get user response
            String response = PlainTextComponentSerializer.plainText().serialize(event.message());

            // finish
            event.setCancelled(true); // cancel the chat message from sending to others
            this.parentClass.setResponse(response); // set the user response
            this.parentClass.execute(); // loop back to the function
        }

    }

    /**
     * The prompt for the user to determine their input from.
     */
    private String prompt;

    /**
     * The key name used to identify and invoke the method via KeyHandler.
     */
    private String key;

    /**
     * The user input.
     */
    private String value = null;

    /**
     * Tracking if the setup process in execute() has already been done.
     */
    private Boolean wasSetUp = false;

    /**
     * Tracking if the user input has been confirmed by the user.
     */
    private Boolean confirmedValue = false;

    /**
     * Instantiating Listener to pass values back into this function on events.
     */
    private Listener chatListener;

    /**
     * Player which function should execute on.
     */
    private Player player;

    /**
     * Requests player user input using a chat box prompt.
     * @param params 1. the prompt 2. the key name
     * @param director to set values
     */
    public ChatPrompt(List<Object> params, ClientDirector director) {
        super(params, director);
    }

    /**
     * Creating and validating values for the chat prompt.
     * <ul>
     * <li>Validates passed in params.
     * <li>Sets the prompt and key as class values.
     * <li>Creates and registers an instance of a chat event listener.
     * <li>Minimises the GUI (closing without disposing).
     * <li>Marks the prompt as successfully setup.
     * <li>Re-runs {@link #execute()}.
     * </ul>
     */
    private void setUp() {
        // set initial values
        this.prompt = (String) params.get(0);
        this.key = (String) params.get(1);
        this.player = this.director.getPlayer();
        this.chatListener = new ChatPromptListener(this, Bukkit.getPlayer(this.player.getUniqueId()));

        try {
            PluginUtils.validateParams(this.params, String.class, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.message(e.getMessage())
                .player(this.player)
                .type(ChatUtils.MessageType.ERROR)
                .send();
        }

        // temporarily close the existing GUI but don't dispose
        this.director.getGUI().getResult().minimise();

        // mark this function class as setup
        this.wasSetUp = true;

        // create a listener for checking if the user types in a value (and any other related chat events)
        Bukkit.getPluginManager().registerEvents(this.chatListener, Core.getPlugin());

        // loop back after setting up
        this.execute();
    }

    /**
     * An enum of all predefined types of prompt messages.
     * <p>
     * Messages defined in ChatPrompt.putPredefinedMessage().
     */
    private enum MessageType {
        /**
         * Sends the prompt to the user. 
         */
        REQUEST,

        /**
         * Checking with the user input if value is correct.
         */
        CONFIRM,

        /**
         * Goodbye messages.
         */
        EXITED,

        /**
         * If the value was successfully set for use.
         */
        CONFIRMED
    }

    /**
     * Prompts the user and manages the user input including 'exit' and 'confirm' UX checks.
     */
    @Override
    public void execute() {
        if (!this.wasSetUp) {
            setUp();
            return;
        }

        ChatUtils.clearChat(this.player);

        if (this.value == null) {
            putPredefinedMessage(MessageType.REQUEST);
            return;
        }

        // if the value is an exit keyword
        
        if (ChatUtils.isExitKeyword(this.value)) {
            putPredefinedMessage(MessageType.EXITED);
            this.confirmedValue = false;
            this.exit();
            return;
        }

        if (this.confirmedValue) {
            if (!this.key.equals("none")) {
                throw new RuntimeException("KeyHandler removed.");
            }

            putPredefinedMessage(MessageType.CONFIRMED);
            this.exit();
            return;
        }

        if (this.value != null && this.confirmedValue == false) {
            putPredefinedMessage(MessageType.CONFIRM);
            return;
        }
    }

    /**
     * Used to set the value for confirmation/setting.
     * @param value the user input
     */
    public void setResponse(String value) {
        if (this.value != null && ChatUtils.isConfirmKeyword(value)) {
            this.confirmedValue = true;
            return;
        }
        
        this.value = value;
    }

    /**
     * Used to get the value inputted.
     * @return value of the user input
     */
    public String getResponse() {
        if (confirmedValue) {
            return value;
        }

        return null;
    }

    /**
     * Powers the visual feedback for the chat prompt; shows the user instructions and information.
     * @param type the ENUM for the stage of the chat prompt.
     */
    private void putPredefinedMessage(MessageType type) {
        switch(type) {
            case REQUEST:
            ChatUtils.message(Component.text(this.prompt).decorate(TextDecoration.UNDERLINED)
                .appendNewline()
                .append(Component.text("or type ").color(NamedTextColor.RED))
                .append(Component.text("exit").color(NamedTextColor.GRAY))
            ).player(player).send();
            break;

            case CONFIRM:
            ChatUtils.message(Component.text(this.prompt).decorate(TextDecoration.UNDERLINED).appendSpace().append(Component.text("Entered: " + this.value).decorate(TextDecoration.ITALIC))
                .appendNewline()
                .append(Component.text("enter again").color(NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("or type ").color(NamedTextColor.GREEN))
                .append(Component.text("confirm").color(NamedTextColor.GRAY))
                .appendNewline()
                .append(Component.text("or type ").color(NamedTextColor.RED))
                .append(Component.text("exit").color(NamedTextColor.GRAY))
            ).player(player).send();
            break;

            case EXITED:
            ChatUtils.message(Component.text("exited")
                .decorate(TextDecoration.ITALIC)
                .color(NamedTextColor.RED)
            ).player(player).send();
            break;

            case CONFIRMED:
            ChatUtils.message(Component.text("confirmed")
                .decorate(TextDecoration.ITALIC)
                .color(NamedTextColor.DARK_GREEN)
            ).player(player).send();
            break;
        }
    }

    /**
     * Called when everything is done.
     * <ul>
     * <li>Unregisters the event listener.
     * <li>Resets the values for next user prompt.
     * <li>Requests for the GUI to re-open.
     * <ul>
     */
    private void exit() {
        // stop capturing the user input
        HandlerList.unregisterAll(this.chatListener);

        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // execute next on main thread
            this.director.getGUI().getResult().open(); // open the old GUI again after minimise()
            this.finished(); // run code for when finished

            // reset values
            this.value = null;
            this.wasSetUp = false;
            this.confirmedValue = false;
        });
    }
}
