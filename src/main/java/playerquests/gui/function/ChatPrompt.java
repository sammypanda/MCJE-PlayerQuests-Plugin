package playerquests.gui.function;

import org.bukkit.Bukkit; // used to access a Scheduler
import org.bukkit.event.HandlerList; // to unregister event listener (ChatPromptListener)
import org.bukkit.event.Listener; // to register event listener (ChatPromptListener)

import net.md_5.bungee.api.ChatColor; // used to format the chat messages to guide user input/UX
import playerquests.Core; // used to access the Plugin and KeyHandler instances
import playerquests.chat.listener.ChatPromptListener; // custom event listener user input
import playerquests.utils.ChatUtils; // used to clear chat lines for a better UI

/**
 * Meta action to swiftly move to another GUI screen based on another template JSON file. 
 */
public class ChatPrompt extends GUIFunction {

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
        validateParams(this.params, String.class, String.class);

        this.prompt = (String) params.get(0);
        this.key = (String) params.get(1);
        this.chatListener = new ChatPromptListener(this);

        // set the player
        if (this.parentGui.getViewer() == null) { this.parentGui.setViewer(player); }

        // temporarily close the existing GUI but don't dispose
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            this.parentGui.minimise();
        });

        this.wasSetUp = true;

        // create a listener for checking if the user types in a value (and any other related chat events)
        Bukkit.getPluginManager().registerEvents(this.chatListener, Core.getPlugin());

        this.execute(); // loop back after setting up
    }

    /**
     * An enum of all predefined types of prompt messages.
     * <p>
     * Messages defined in ChatPrompt.putPredefinedMessage().
     */
    public enum MessageType {
        REQUEST,
        CONFIRM,
        EXITED,
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

        if (this.value.toUpperCase().equals("EXIT")) {
            putPredefinedMessage(MessageType.EXITED);
            this.exit();
            return;
        }

        if (this.confirmedValue) {
            try {
                Core.getKeyHandler().setValue(this.parentGui, this.key, this.value);
            } catch (IllegalArgumentException e) {
                ChatUtils.sendError(this.player, e.getMessage());
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
        if (value.toUpperCase().equals("CONFIRM")) {
            this.confirmedValue = true;
            return;
        }
        
        this.value = value;
    }

    /**
     * Powers the visual feedback for the chat prompt; shows the user instructions and information.
     * @param type the ENUM for the stage of the chat prompt.
     */
    private void putPredefinedMessage(MessageType type) {
        switch(type) {
            case REQUEST:
            this.player.sendMessage(
                ChatColor.UNDERLINE + this.prompt + ChatColor.RESET
            );
            ChatUtils.clearChat(this.player, 1);
            this.player.sendMessage(
                ChatColor.RED + "or type " + ChatColor.GRAY + "exit" + ChatColor.RESET
            );
            break;

            case CONFIRM:
            this.player.sendMessage(
                ChatColor.UNDERLINE + prompt + ChatColor.RESET + " " +
                ChatColor.GRAY + "" + ChatColor.ITALIC + "Entered: " + this.value
            );
            ChatUtils.clearChat(this.player, 1);
            this.player.sendMessage(
                ChatColor.GRAY + "enter again\n" +
                ChatColor.GREEN + "or type " + ChatColor.GRAY + "confirm\n" +
                ChatColor.RED + "or type " + ChatColor.GRAY + "exit" + ChatColor.RESET
            );
            break;

            case EXITED:
            this.player.sendMessage(
                ChatColor.GRAY + "" + ChatColor.ITALIC + "exited"
            );
            break;

            case CONFIRMED:
            this.player.sendMessage(
                ChatColor.DARK_GREEN + "" + ChatColor.ITALIC + "confirmed"
            );
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

        // reset values
        this.value = null;
        this.wasSetUp = false;
        this.confirmedValue = false;

        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // async request an event to occur
            this.parentGui.open(); // open the old GUI again after minimise().

            this.parentSlot.executeNext(this.player); // run the next function
        });
    }
}
