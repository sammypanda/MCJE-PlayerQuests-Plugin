package playerquests.builder.gui.function;

import java.util.ArrayList; // used to store the params for this meta action

import org.bukkit.Bukkit; // used to access a Scheduler
import org.bukkit.event.EventHandler; // handling spigot events
import org.bukkit.event.HandlerList; // to unregister event listener (ChatPromptListener)
import org.bukkit.event.Listener; // to register event listener (ChatPromptListener)
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor; // used to format the chat messages to guide user input/UX
import org.bukkit.entity.HumanEntity; // refers to the player

import playerquests.Core; // used to access the Plugin and KeyHandler instances
import playerquests.client.ClientDirector; // powers functionality for functions
import playerquests.utility.ChatUtils; // used to clear chat lines for a better UI
import playerquests.utility.PluginUtils; // used to validate function params

/**
 * Meta action to swiftly move to another GUI screen based on another template JSON file. 
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
         * Creates a new listener for chat prompt inputs.
         * @param parent the origin ChatPrompt GUI function
         */
        public ChatPromptListener(ChatPrompt parent) {
            this.parentClass = parent;
        }

        /**
         * Sends user input back to ChatPrompt GUI function.
         * @param event
         */
        @EventHandler
        private void onChat(AsyncPlayerChatEvent event) {
            event.setCancelled(true); // cancel the chat message from sending to others
            this.parentClass.setResponse(event.getMessage()); // set the user response
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
    private HumanEntity player;

    /**
     * Requests player user input using a chat box prompt.
     * @param params 1. the prompt 2. the key name
     * @param director to set values
     */
    public ChatPrompt(ArrayList<Object> params, ClientDirector director) {
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
        this.chatListener = new ChatPromptListener(this);
        this.player = this.director.getPlayer();

        try {
            PluginUtils.validateParams(this.params, String.class, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.sendError(this.player, e.getMessage());
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
    public enum MessageType {
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

        if (this.value.toUpperCase().equals("EXIT")) {
            putPredefinedMessage(MessageType.EXITED);
            this.confirmedValue = false;
            this.exit();
            return;
        }

        if (this.confirmedValue) {
            if (!this.key.equals("none")) {
                try {
                    Class<?> classType = Core.getKeyHandler().getClassFromKey(this.key); // discover what class type the key is for
                    Object instance = this.director.getCurrentInstance(classType); // get the current in-use instance for the class type
                    Core.getKeyHandler().setValue(instance, this.key, this.value); // set the value
                } catch (IllegalArgumentException e) {
                    ChatUtils.sendError(this.player, e.getMessage());
                    this.errored = true;
                }
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
