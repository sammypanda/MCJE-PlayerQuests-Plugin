package playerquests.product;

import java.io.IOException; // thrown if Quest cannot be saved
import java.util.HashMap;
import java.util.Map; // generic map type
import java.util.UUID; // identifies the player who created this quest

import org.bukkit.Bukkit; // Bukkit API
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
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder
import playerquests.utility.ChatUtils; // helpers for in-game chat
import playerquests.utility.ChatUtils.MessageBuilder;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.FileUtils; // helpers for working with files
import playerquests.utility.annotation.Key; // key-value pair annotations for KeyHandler
import playerquests.utility.singleton.Database;
import playerquests.utility.singleton.QuestRegistry;

/**
 * The Quest product containing all the information 
 * about a quest, ready to be played.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // ignore id
public class Quest {

    /**
     * The label of this quest.
     */
    private String title = null;

    /**
     * The starting/entry point stage ID for this quest.
     */
    private String entry = null;

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

    @JsonIgnore
    /**
     * If the quest is toggled.
     */
    private Boolean toggled = false;
    
    /**
     * Creates a quest instance for playing and viewing!
     * @param title label of this quest
     * @param entry starting/entry point stage for this quest
     * @param npcs map of NPCs used in this quest
     * @param stages map of stages used in this quest
     * @param creator UUID of player who created this quest
     */
    public Quest(
        @JsonProperty("title") String title, 
        @JsonProperty("entry") QuestStage entry, 
        @JsonProperty("npcs") Map<String, QuestNPC> npcs, 
        @JsonProperty("stages") Map<String, QuestStage> stages, 
        @JsonProperty("creator") UUID creator
    ) {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);

        this.title = title;
        
        if (entry != null) {
            this.entry = entry.getID();
        }

        this.npcs = npcs;
        this.stages = stages;
        this.creator = creator;

        // Set Quest dependency for each QuestStage instead of custom deserialize
        if (stages != null) {
            for (QuestStage stage : stages.values()) {
                stage.setQuest(this);
            }
        }

        // Set Quest dependency for each QuestNPC instead of custom deserialize
        if (npcs != null) {
            for (QuestNPC npc : npcs.values()) {
                npc.setQuest(this);
            }
        }

        // Determine if toggled
        this.toggled = Database.getInstance().getQuestToggled(this);
    }

    /**
     * Creates a quest product from a string template.
     * @param questTemplate the (json) string quest template
     * @return the quest product created from the quest template
     */
    public static Quest fromTemplateString(String questTemplate) {
        Quest quest = null;
        ObjectMapper jsonObjectMapper = new ObjectMapper(); // used to deserialise json to object
        
        // configure the mapper
        jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty
        jsonObjectMapper.setSerializationInclusion(Include.NON_NULL);

        // create the quest product
        try {
            quest = jsonObjectMapper.readValue(questTemplate, Quest.class);
        } catch (JsonMappingException e) {
            System.err.println("Could not map a quest template string to a valid quest product.");
        } catch (JsonProcessingException e) {
            System.err.println("Malformed JSON attempted as a quest template string.");
        }

        return quest;
    }

    /**
     * Gets the label of this quest.
     * @return the label of this quest
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the starting prev, current, next steps.
     * @return
     */
    @JsonIgnore
    public ConnectionsData getConnections() {
        return new ConnectionsData(null, this.entry, null);
    }

    /**
     * Gets the starting/entry point stage ID for this quest.
     * @return the ID of the starting/entry point stage for this quest
     */
    public String getEntry() {
        return entry;
    }

    /**
     * Gets the map of NPCs used in this quest.
     * @return the map of NPCs used in this quest
     */
    public Map<String, QuestNPC> getNPCs() {
        return npcs;
    }

    /**
     * Gets the map of stages used in this quest.
     * @return the map of stages used in this quest
     */
    public Map<String, QuestStage> getStages() {
        return stages;
    }

    /**
     * Gets the creator (UUID) of this quest.
     * @return the creator (UUID) of this quest
     */
    public UUID getCreator() {
        return creator;
    }

    /**
     * Gets the liquid ID for this quest.
     * <p>
     * IDs aren't fixed, if the quest title 
     * changes, it's considered a different quest.
     * This means creating new versions of the 
     * same quest is very easy. like Quest -> Quest2.
     */
    @JsonProperty("id") 
    public String getID() {
        return String.format("%s%s", 
            title, 
            creator != null ? "_"+creator : ""
        );
    }

    /**
     * Creates a quest template based on the current product.
     * @return this quest as a json object
     * @throws JsonProcessingException when the json cannot seralise
     */
    public String toTemplateString() throws JsonProcessingException {
        // get the product of this builder
        Quest product = this;

        // serialises an object into json
        ObjectMapper jsonObjectMapper = new ObjectMapper();

        // configure the mapper
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty

        // present this quest product as a template json string (prettied)
        return jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(product);
    }

    /**
     * Saves a quest into the QuestBuilder.savePath.
     * @return the response message
     */
    @Key("quest")
    public String save() {
        String questName = "quest/templates/" + this.getID() + ".json"; // name pattern

        // create quest in fs, or update it
        try {
            if (FileUtils.check(questName)) { // if the quest is in the fs
                Core.getQuestRegistry().submit(this);
            }

            FileUtils.create( // create the template json file
                questName, // name pattern
                this.toTemplateString().getBytes() // put the content in the file
            );
            return "'" + this.title + "' was saved.";
        } catch (IOException e) {
            ChatUtils.message(e.getMessage())
                .player(Bukkit.getPlayer(this.creator))
                .type(MessageType.ERROR)
                .send();
            System.err.println(e);
            return "'" + this.title + "' could not save.";
        }
    }

    @JsonIgnore
    public Map<String, QuestAction> getActions() {
        Map<String, QuestAction> actions = new HashMap<String, QuestAction>();

        this.getStages().forEach((stage_id, stage) -> {
            stage.getActions().forEach((action_id, action) -> {
                actions.put(action_id, action);
            });
        });
        
        return actions;   
    }

    /**
     * Checks if the quest is toggled/enabled.
     * @return whether the quest is enabled/being shown
     */
    @JsonIgnore
    public Boolean isToggled() {
        return Database.getInstance().getQuestToggled(this);
    }

    /**
     * Toggle function as a switch.
     */
    public void toggle() {
        this.toggle(!this.toggled);
    }

    /**
     * Toggle function but with discrete choice.
     * @param toEnable whether to show/enable the quest
     */
    public void toggle(boolean toEnable) {
        this.toggled = toEnable;

        if (toEnable) {
            QuestRegistry.getInstance().submit(this); // resubmit previously untoggled
        } else {
            QuestRegistry.getInstance().remove(this, true); // remove from world (but preserve)
        }

        Database.getInstance().setQuestToggled( // update database state (when we can)
            this,
            toEnable
        );
    }

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

        // Validate quest entry
        if (this.entry == null) {
            response.content(String.format("The %s quest has no starting point", this.title));
            isValid = false;
        }

        // Validate quest stages
        if (this.stages == null || this.stages.isEmpty()) {
            response.content(String.format("The %s quest has no stages", this.title));
            isValid = false;
        }

        // send the response
        if (!isValid) {
            response.send(); // send our :( message
        }

        return isValid;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return String.format("%s=%s", super.toString(), this.getID());
    }
}