package playerquests.product;

import java.io.IOException; // thrown if Quest cannot be saved
import java.util.Map; // generic map type
import java.util.UUID; // identifies the player who created this quest

import org.bukkit.Bukkit; // Bukkit API

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // configures ignoring unknown fields
import com.fasterxml.jackson.annotation.JsonManagedReference; // refers to the parent of a back reference
import com.fasterxml.jackson.annotation.JsonProperty; // how a property is serialised
import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json is invalid
import com.fasterxml.jackson.databind.ObjectMapper; // used to deserialise/serialise this class
import com.fasterxml.jackson.databind.SerializationFeature; // used to configure serialisation

import playerquests.Core; // the main class of this plugin
import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder
import playerquests.utility.ChatUtils; // helpers for in-game chat
import playerquests.utility.FileUtils; // helpers for working with files
import playerquests.utility.annotation.Key; // key-value pair annotations for KeyHandler

/**
 * The Quest product containing all the information 
 * about a quest, ready to be played.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // ignore id
public class Quest {

    /**
     * The label of this quest.
     */
    private String title;

    /**
     * The starting/entry point stage ID for this quest.
     */
    private String entry;

    /**
     * The map of NPCs used in this quest, by their ID.
     */
    @JsonManagedReference
    private Map<String, QuestNPC> npcs;

    /**
     * The map of stages used in this quest, by the stage ID.
     */
    @JsonManagedReference
    private Map<String, QuestStage> stages;

    /**
     * The UUID of the player who created this quest.
     */
    private UUID creator;
    
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
        this.entry = "stage_-1";
        
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

                System.out.println(this); // this is null
                System.out.println(":0 quest npcs: " + this.getNPCs());
            }
        }
    }

    /**
     * Gets the label of this quest.
     * @return the label of this quest
     */
    public String getTitle() {
        return title;
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
        return String.format("%s_%s" , title, creator);
    }

    /**
     * Creates a quest template based on the current product.
     * @return this quest as a json object
     * @throws JsonProcessingException when the json cannot seralise
     */
    private String toTemplateString() throws JsonProcessingException {
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
     * @throws IllegalArgumentException when saving is not safe/possible
     */
    @Key("quest")
    public String save() throws IllegalArgumentException {
        try {
            FileUtils.create( // create the template json file
                "quest/templates/" + this.title + "_" + this.creator.toString() + ".json", // name pattern
                this.toTemplateString().getBytes() // put the content in the file
            );
        } catch (IOException e) {
            ChatUtils.sendError(Bukkit.getPlayer(this.creator), e.getMessage(), e);
            return "Quest Builder: '" + this.title + "' could not save.";
        }

        // asume enabled and submit (adds the quest to the world)
        Core.getQuestRegistry().submit(this);
        return "Quest Builder: '" + this.title + "' was saved";
    }
}