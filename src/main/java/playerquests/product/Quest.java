package playerquests.product;

import java.io.IOException; // thrown if Quest cannot be saved
import java.util.HashMap;
import java.util.List;
import java.util.Map; // generic map type
import java.util.UUID; // identifies the player who created this quest

import org.bukkit.Bukkit; // Bukkit API
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // configures ignoring unknown fields
import com.fasterxml.jackson.annotation.JsonManagedReference; // refers to the parent of a back reference
import com.fasterxml.jackson.annotation.JsonProperty; // how a property is serialised
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json is invalid
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper; // used to deserialise/serialise this class
import com.fasterxml.jackson.databind.SerializationFeature; // used to configure serialisation

import playerquests.Core; // the main class of this plugin
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder
import playerquests.utility.ChatUtils; // helpers for in-game chat
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.FileUtils; // helpers for working with files
import playerquests.utility.PluginUtils;
import playerquests.utility.annotation.Key; // key-value pair annotations for KeyHandler
import playerquests.utility.singleton.Database; // the preservation everything store
import playerquests.utility.singleton.QuestRegistry; // multi-threaded quest store

/**
 * Represents a quest containing all information necessary for gameplay.
 * This class provides methods for creating, saving, validating, and toggling quests.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // ignore id
public class Quest {
    /**
     * The label of this quest.
     */
    private String title = null;

    /**
     * The map of NPCs used in this quest, by their ID.
     */
    @JsonManagedReference
    private Map<String, QuestNPC> npcs = null;

    /**
     * The map of stages used in this quest, by the stage ID.
     */
    @JsonManagedReference
    private Map<String, QuestStage> stages = null;

    /**
     * The UUID of the player who created this quest.
     */
    private UUID creator = null;

    /**
     * If the quest is toggled.
     */
    private Boolean toggled;

    /**
     * The ID of this quest.
     */
    private String id;

    /**
     * Start points for this quest
     */
    private List<StagePath> startPoints = List.of();
    
    /**
     * Constructs a new Quest with the specified parameters.
     * 
     * @param title The title of the quest.
     * @param npcs A map of NPCs used in the quest.
     * @param stages A map of stages used in the quest.
     * @param creator The UUID of the player who created the quest.
     * @param id the id of the quest.   
     * @param startpoints the actions that start when the quest starts.
     */
    public Quest(
        @JsonProperty("title") String title,
        @JsonProperty("npcs") Map<String, QuestNPC> npcs, 
        @JsonProperty("stages") Map<String, QuestStage> stages, 
        @JsonProperty("creator") UUID creator,
        @JsonProperty("id") String id,
        @JsonProperty("startpoints") List<StagePath> startpoints
    ) {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);

        // remove null NPCs and stages
        npcs.entrySet().removeIf(stage -> stage.getValue() == null);
        stages.entrySet().removeIf(npc -> npc.getValue() == null);

        this.title = title;
        this.id = id;
        this.creator = creator;
        this.npcs = npcs;
        this.stages = stages;

        // only set startpoints if not null
        if (this.startPoints != null) {
            this.startPoints = startpoints;
        }

        // Set Quest dependency for each QuestStage instead of custom deserialize
        if (this.stages != null) {
            stages.forEach((stage_id, stage) -> {
                stage.setQuest(this);

                // set stage ID if it's missing
                if (stage_id != stage.getID()) {
                    stage.setID(stage_id);
                }
            });
        }

        // Set Quest dependency for each QuestNPC instead of custom deserialize
        if (this.npcs != null) { 
            npcs.forEach((npc_id, npc) -> {
                npc.setQuest(this);

                // set npc ID if it's missing
                if (npc_id != npc.getID()) {
                    npc.setID(npc_id);
                }
            });
        }
    }

    /**
     * Creates a quest from a JSON string.
     * 
     * @param questJSON The JSON string representing the quest file.
     * @return A {@link Quest} object created from the JSON string.
     */
    public static Quest fromJSONString(String questJSON) {
        Quest quest = null;
        ObjectMapper jsonObjectMapper = new ObjectMapper(); // used to deserialise json to object
        
        // configure the mapper
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty
        jsonObjectMapper.setSerializationInclusion(Include.NON_NULL);

        // create the quest product
        try {
            quest = jsonObjectMapper.readValue(questJSON, Quest.class);
        } catch (JsonMappingException e) {
            System.err.println("Could not map a quest JSON string to a valid quest product. " + e);
        } catch (JsonProcessingException e) {
            System.err.println("Malformed JSON attempted as a quest string. " + e);
        }

        return quest;
    }

    /**
     * Gets the title of this quest.
     * 
     * @return The title of this quest.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the map of NPCs used in this quest.
     * 
     * @return The map of NPCs, keyed by their ID.
     */
    public Map<String, QuestNPC> getNPCs() {
        return npcs;
    }

    /**
     * Gets the map of stages used in this quest.
     * 
     * @return The map of stages, keyed by their ID.
     */
    public Map<String, QuestStage> getStages() {
        return stages;
    }

    /**
     * Gets the UUID of the player who created this quest.
     * 
     * @return The UUID of the creator, null if none.
     */
    public UUID getCreator() {
        return creator;
    }

    /**
     * Gets the Player object for this quest creator if can be found.
     * 
     * @return the Player object of the creator, null if none.
     */
    @JsonIgnore
    public Player getCreatorPlayer() {
        UUID creatorUUID = this.getCreator();

        if (creatorUUID == null) {
            return null;
        }

        return Bukkit.getPlayer(creatorUUID);
    }

    /**
     * Gets a unique identifier for this quest.
     * <p>
     * The ID is composed of the quest title and creator UUID. This allows for easy differentiation of different versions of the same quest.
     * 
     * @return The unique ID of this quest.
     */
    @JsonProperty("id") 
    public String getID() {
        return this.id;
    }

    /**
     * Converts this quest to a JSON string.
     * 
     * @return A JSON string representing this quest.
     * @throws JsonProcessingException If the JSON cannot be serialized.
     */
    public String toJSONString() throws JsonProcessingException {
        // get the product of this builder
        Quest product = this;

        // serialises an object into json
        ObjectMapper jsonObjectMapper = new ObjectMapper();

        // configure the mapper
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty

        // present this quest product as a json string (prettied)
        return jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
    }

    /**
     * Saves this quest to a file.
     * 
     * @return A message indicating the result of the save operation.
     */
    @Key("quest")
    public String save() {
        String questName = Core.getQuestsPath() + this.getID() + ".json"; // name pattern
        Player player = null;

        // set player if this quest has one
        if (this.creator != null) {
            player = Bukkit.getPlayer(this.creator);
        };

        // create quest in fs, or update it
        try {
            if (FileUtils.check(questName)) { // if the quest is in the fs
                Core.getQuestRegistry().submit(this);
            }

            FileUtils.create( // create the quest json file
                questName, // name pattern
                this.toJSONString().getBytes() // put the content in the file
            );
            return "'" + this.title + "' was saved.";
        } catch (IOException e) {
            MessageBuilder errorMessage = ChatUtils.message(e.getMessage())
                                                   .type(MessageType.ERROR)
                                                   .target(MessageTarget.CONSOLE)
                                                   .style(MessageStyle.PLAIN);
            
            // send error to console regardless
            errorMessage.send();

            // also send to player if they are around
            if (player != null) {
                errorMessage.player(player).send();
            }

            return "'" + this.title + "' could not save.";
        }
    }

    /**
     * Checks if this quest is toggled (enabled).
     * 
     * @return Whether the quest is enabled.
     */
    @JsonIgnore
    public Boolean isToggled() {
        // if value, uninitiated or unset
        // find truth in database
        if (this.toggled == null) {
            this.toggled = Database.getInstance().getQuestToggled(this);
        }

        return this.toggled;
    }

    /**
     * Toggles the quest's enabled/disabled state.
     */
    public void toggle() {
        this.toggle(!this.toggled);
    }

    /**
     * Toggles the quest's enabled/disabled state with a specified value.
     * 
     * @param toEnable Whether to enable (true) or disable (false) the quest.
     */
    public void toggle(boolean toEnable) {
        // check if able to be toggled
        if (!isAllowed()) {
            toEnable = false;
        }

        // do toggling
        if (toEnable) {
            toEnable = QuestRegistry.getInstance().toggle(this); // can overwrite toggle with false, if failed
        } else {
            QuestRegistry.getInstance().untoggle(this);
        }

        // store toggle state
        this.toggled = toEnable;

        // preserve toggle state
        Database.getInstance().setQuestToggled( // update database state (when we can)
            this,
            toEnable
        );
    }

    /**
     * Validates this quest's data.
     * 
     * @return Whether the quest is valid.
     */
    @JsonIgnore
    public boolean isValid() {
        UUID questCreator = this.creator;
        Player player = null;
        MessageBuilder response = new MessageBuilder("Something is wrong with the quest") // default message; default sends to console
            .type(MessageType.ERROR)
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN);
        boolean isValid = true; // assume valid unless errors are found

        // Check if the player is valid and set the player object if a creator is present
        if (questCreator != null) {
            player = Bukkit.getPlayer(questCreator); // get player from UUID
            response.player(player).style(MessageStyle.PRETTY); // set message target to player if player is found
        }

        // Validate quest title
        if (this.title == null) {
            response.content("A quest has no title");
            isValid = false;
        }

        // send the response
        if (!isValid) {
            response.send(); // send our :( message
        }

        return isValid;
    }

    /**
     * Checks if this quest should be allowed to be enabled.
     * 
     * @return Whether the quest is allowed to be enabled.
     */
    @JsonIgnore
    public boolean isAllowed() {
        UUID questCreator = this.creator;
        Player player = null;
        MessageBuilder response = new MessageBuilder("Cannot enable the quest") // default message; default sends to console
            .type(MessageType.ERROR)
            .target(MessageTarget.CONSOLE)
            .style(MessageStyle.PLAIN);
        boolean isAllowed = true; // assume valid unless errors are found

        // Check if the player is valid and set the player object if a creator is present
        if (questCreator != null) {
            player = Bukkit.getPlayer(questCreator); // get player from UUID
            response.player(player).style(MessageStyle.PRETTY); // set message target to player if player is found
        }

        // Validate quest title
        isAllowed = !PluginUtils.getPredictiveInventory(this, QuestRegistry.getInstance().getInventory(this)).entrySet().stream().anyMatch(entry -> {
            Integer amount = entry.getValue();

            if (amount <= 0) {
                response.content(String.format("The '%s' quest is missing some stock", this.title));
                return true; // exit
            }

            return false; // continue
        });

        // send the response
        if (!isAllowed) {
            response.send(); // send our :( message
        }

        return isAllowed;
    }

    /**
     * Provides a string representation of this quest.
     * 
     * @return A string representing this quest.
     */
    @JsonIgnore
    @Override
    public String toString() {
        return String.format("%s=%s", super.toString(), this.getID());
    }

    /**
     * Refunds the resources used in this quest to the creator.
     * <p>
     * If the creator is null (indicating a shared quest), no refund is performed.
     */
    public void refund() {
        if (this.creator == null) {
            return; // no need to refund, a shared quest has infinite resources
        }

        Player player = Bukkit.getPlayer(creator);

        // return NPC resources
        this.getNPCs().values().stream().forEach(npc -> {
            npc.refund(player);
        });

        // let the player know
        ChatUtils.message("Returned items from quest.")
            .player(player)
            .style(MessageStyle.PRETTY)
            .send();
    }

    /**
     * Get how many of each item is required to be in the quest inventory.
     * @return the minimum numbers of items required.
     */
    @JsonIgnore
    public Map<Material, Integer> getRequiredInventory() {
        Map<Material, Integer> requiredInventory = new HashMap<>();

        return requiredInventory;
    }

    /**
     * Get the list of starting points for this quest.
     * @return a list of stage paths (which can include actions).
     */
    @JsonProperty("startpoints")
    public List<StagePath> getStartPoints() {
        if (this.startPoints == null) {
            return List.of();
        }
        
        return this.startPoints;
    }
}