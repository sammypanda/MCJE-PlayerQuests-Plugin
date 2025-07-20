package playerquests.builder.gui.function;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit; // getting the plugin manager
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; // registering methods as event handlers
import org.bukkit.event.HandlerList; // unregistering event handlers
import org.bukkit.event.Listener; // listening to in-game events
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import playerquests.Core; // accessing singletons
import playerquests.builder.gui.function.data.SelectMethod; // defining which methods to select something
import playerquests.client.ClientDirector; // controls the plugin
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.serialisable.EntitySerialisable;
import playerquests.utility.PluginUtils; // used to validate function params 

/**
 * Function for the user to select an entity.
 */
public class SelectEntity extends GUIFunction {

    private class SelectEntityListener implements Listener {
        /**
         * The {@code SelectEntity} instance.
         */
        private SelectEntity parentClass;

        /**
         * The player this listener is for.
         */
        private Player player;

        /**
         * Select methods to not allow.
         */
        private List<SelectMethod> deniedMethods;

        /**
         * Constructs a new {@code SelectEntityListener}.
         *
         * @param parent the parent {@code SelectEntity} instance
         * @param player the player associated with this listener
         */
        public SelectEntityListener(SelectEntity parent, Player player) {
            this.parentClass = parent;
            this.player = player;
            this.deniedMethods = parent.getDeniedMethods();
        }
        
        /**
         * Handles player interactions with entities.
         * @param event the {@code PlayerInteractEntityEvent} triggered when a player interacts with an entity
         */
        @EventHandler
        private void onInteract(PlayerInteractEntityEvent event) {
            if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
                return; // no duplicating interaction
            }

            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }
            
            if (deniedMethods.contains(SelectMethod.PAT)) {
                return; // do not continue
            }

            // get the entity
            Entity clickedEntity = event.getRightClicked();
            if (clickedEntity == null) {
                return;
            }

            // cancel the interaction
            event.setCancelled(true);

            // get as serialiable entity
            EntitySerialisable entitySerialisable = EntitySerialisable.fromEntity(clickedEntity);

            // TODO: disallow selecting existing NPCs

            // set response
            if (entitySerialisable != null) {
                this.parentClass.setResponse(entitySerialisable);
            }
        }

        /**
         * Handles player command inputs to exit the selection.
         * @param event the {@code PlayerCommandPreprocessEvent} triggered when a player issues a command
         */
        @EventHandler
        private void onCommand(PlayerCommandPreprocessEvent event) {
            // do not capture other players events
            if (this.player != event.getPlayer()) {
                return;
            }

            // exit SelectMaterial
            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                this.parentClass.setCancelled(true);
                this.parentClass.execute(); // run with cancellation
            });
        }

        /**
         * Handles player chat inputs for exiting.
         * @param event the {@code AsyncPlayerChatEvent} triggered when a player sends a chat message
         */
        @EventHandler
        private void onChat(AsyncChatEvent event) {
            // if the event is coming from a different player
            if (this.player != event.getPlayer()) {
                return; // do not capture other players events
            }

            event.setCancelled(true);

            Bukkit.getScheduler().runTask(Core.getPlugin(), () -> { // run on next tick
                String message = PlainTextComponentSerializer.plainText().serialize(event.message());

                // if wanting to exit (or trying to do another command)
                if (ChatUtils.isExitKeyword(message)) {
                    this.parentClass.setCancelled(true);
                    this.parentClass.execute(); // run with cancellation
                }
            });
        }
    }

    /**
     * Detecting which entity the user selects.
     */
    private Listener entityListener;

    /**
     * The resulting entity selected.
     */
    private EntitySerialisable result;

    /**
     * The player selecting the entity.
     */
    private Player player;

    /**
     * The starting prompt to give the user.
     */
    private String prompt;

    /**
     * If the function has been set up.
     */
    private boolean wasSetup;

    /**
     * The entities to deny.
     */
    private List<EntityType> deniedEntities;

    /**
     * The methods of selecting entities to deny.
     */
    private List<SelectMethod> deniedMethods;

    /**
     * If the player has cancelled the selection.
     */
    private boolean cancelled;

    /** 
     * Provides input as a user selected enttiy.
     * <ul>
     * <li>By right clicking the physical entity
     * </ul>
     * @param params 1. the prompt to show the user 2. list of denied entities 3. list of denied methods
     * @param director to set values
     */
    public SelectEntity(List<Object> params, ClientDirector director) {
        super(params, director);
    }

    /**
     * Creating and validating values for the entity selector
     */
    private void setup() {
        try {
            PluginUtils.validateParams(this.params, String.class, List.class, List.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.message(e.getMessage())
                .player(this.player)
                .type(MessageType.ERROR)
                .send();
        }

        // set params
        this.prompt = (String) params.get(0);
        this.deniedEntities = castDeniedEntities(params.get(1));
        this.deniedMethods = castDeniedMethods(params.get(2));

        // set default denied methods
        this.deniedMethods.addAll(List.of(SelectMethod.CHAT, SelectMethod.HIT, SelectMethod.SELECT));

        // get and set the player who is selecting the entity
        this.player = this.director.getPlayer();

        // temporarily close the existing GUI but don't dispose
        this.director.getGUI().getResult().minimise();

        // register events and listener
        this.entityListener = new SelectEntityListener(this, Bukkit.getPlayer(this.player.getUniqueId()));
        Bukkit.getPluginManager().registerEvents(this.entityListener, Core.getPlugin());

        // mark this function class as setup
        this.wasSetup = true;

        // loop back after setting up
        this.execute();
    }

    /**
     * Cast deniedEntities object to a list of denied entities.
     * @param object object of entites to deny
     * @return list of entities to deny
     */
    private List<EntityType> castDeniedEntities(Object object) {
        List<?> castedList = (List<?>) object; // wildcard generics for cast checking

        return castedList.stream()
            .filter(entityType -> entityType instanceof EntityType) // filter out items that aren't entity
            .map(entityType -> (EntityType) entityType) // cast
            .toList(); // collect into final denylist
    }

    /**
     * Gets the entities that cannot be set as an NPC.
     * @return a list of entities
     */
    public List<EntityType> getDeniedEntities() {
        return this.deniedEntities;
    }

    /**
     * Check and cast deniedMethods to a list of denied method ENUMs
     * @param object object of methods to deny
     * @return list of methods to deny
     */
    private List<SelectMethod> castDeniedMethods(Object object) {
        List<?> castedList = (List<?>) object; // wildcard generics for cast checking

        return (ArrayList<SelectMethod>) castedList.stream()
            .filter(method -> method instanceof SelectMethod) // filter out non-method items
            .map(method -> (SelectMethod) method) // cast safely
            .toList(); // collect into final denylist
    }

    /**
     * Gets the select methods that have been denied.
     * @return a list of select method enums
     */
    public List<SelectMethod> getDeniedMethods() {
        return this.deniedMethods;
    }

    @Override
    public void execute() {
        if (!this.wasSetup) {
            this.setup();
            return;
        }

        // clear the chat
        ChatUtils.clearChat(this.player);

        if (this.cancelled) {
            ChatUtils.message(
                Component.text("exited").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC)
            ).player(player).send();
            this.exit();
            return;
        }

        if (this.result == null) {
            ChatUtils.message(Component.empty()
                .append(Component.text(this.prompt).decorate(TextDecoration.UNDERLINED))
                .appendNewline().appendNewline()
                .append(Component.text("or type ").color(NamedTextColor.RED))
                .append(Component.text("exit").color(NamedTextColor.GRAY))
            ).player(player).send();
            return;
        }

        ChatUtils.message(
            Component.text("Selected: " + result.toString()).decorate(TextDecoration.ITALIC).color(NamedTextColor.GRAY)
        ).player(player).send();

        this.exit(); // finish
    }

    /**
     * Sets the selected entity.
     * This method validates the entity and ensures it is not in the denied entities list.
     * @param entitySerialisable the {@code EntitySerialisable} to set as the selected entity
     */
    public void setResponse(EntitySerialisable entitySerialisable) {
        if (this.deniedEntities.contains(entitySerialisable.getEntityData().getEntityType(entitySerialisable.getProperties()))) {
            ChatUtils.message("This entity is denied from being set as an NPC.")
                .player(this.player)
                .type(MessageType.WARN)
                .send();
            this.result = null;
            return;
        }

        this.result = entitySerialisable; // set the entity the user selected
        this.execute();
    }

    /**
     * Returns the entity selected by the user.
     * @return the selected entity
     */
    public EntitySerialisable getResult() {
        return this.result;
    }

    /**
     * Cleans up and finishes the entity selection function.
     */
    private void exit() {
        HandlerList.unregisterAll(this.entityListener); // remove private handlers
        this.finished(); // execute onFinish code
    }

    /**
     * Marks the entity selection function as cancelled.
     *
     * @param cancelled {@code true} to cancel the selection, {@code false} otherwise
     */
    private void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }
}
