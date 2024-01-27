package playerquests.builder.quest;

import java.io.IOException; // thrown if a file cannot be created
import java.util.ArrayList; // array list type
import java.util.HashMap; // hash table map type
import java.util.List; // generic list type
import java.util.Map; // generic map type

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from serialising to json
import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property
import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json cannot serialise
import com.fasterxml.jackson.databind.ObjectMapper; // turns objects into json
import com.fasterxml.jackson.databind.SerializationFeature; // configures json serialisation 

import playerquests.Core; // gets the KeyHandler singleton
import playerquests.builder.quest.component.QuestNPC;  // object for quest npcs
import playerquests.builder.quest.component.QuestStage; // object for quest stages
import playerquests.client.ClientDirector; // abstractions for plugin functionality
import playerquests.product.Quest; // quest product class
import playerquests.utility.ChatUtils; // sends message in-game
import playerquests.utility.FileUtils; // creates files
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * For creating and managing a Quest.
 */
public class QuestBuilder {

    /**
     * Used to access plugin functionality.
     */
    private ClientDirector director;

    /**
     * The title of the quest.
     */
    @JsonProperty("title")
    private String title = ""; // default quest title

    /**
     * Entry point for the quest.
     */
    private QuestStage entryPoint;

    /**
     * Map of the NPC characters.
     */
    @JsonIgnore
    private Map<String, QuestNPC> questNPCs = new HashMap<String, QuestNPC>();

    /**
     * Map of the quest stages and actions.
     */
    @JsonIgnore
    private Map<String, QuestStage> questPlan = new HashMap<String, QuestStage>();

    /**
     * Where quests are saved.
     */
    private String savePath = "quest/templates/";

    /**
     * The quest product.
     */
    private Quest quest = new Quest(this);

    /**
     * Operations to run whenever the class is instantiated.
     */
    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);
    }

    /**
     * Returns a new default Quest.
     * @param director used to control the plugin
     */
    public QuestBuilder(ClientDirector director) {
        this.director = director;

        // default entry point as first stage (stage_0)
        this.entryPoint = new QuestStage(director, 0);

        // add default entry point stage to questPlan map
        this.questPlan.put(this.entryPoint.getID(), this.entryPoint);

        // TODO: remove this testing NPC
        QuestNPC testNPC = new QuestNPC();
        this.questNPCs.put(testNPC.getID(), testNPC);

        // set as the current instance in the director
        director.setCurrentInstance(this);
    }

    /**
     * Title for the quest.
     * <p>
     * Also used as the ID: [Title]_[Owner Player ID]
     * @param title the name for the quest
     */
    @Key("quest.title")
    public void setTitle(String title) {
        this.title = title; // set the new title
    }

    /**
     * Gets the quest title.
     * @return quest name
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Creates a quest template based on the current state of the builder.
     * @return this quest as a json object
     * @throws JsonProcessingException when the json cannot seralise
     */
    private String getTemplateString() throws JsonProcessingException {
        // serialises an object into json
        ObjectMapper jsonObjectMapper = new ObjectMapper();

        // configure the mapper
        jsonObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // allow json object to be empty

        // present this quest builder as a template json string (prettied)
        return jsonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    /**
     * Gets the entry point ID.
     * <p>
     * Either an action or a stage.
     * @return the string representation for the entry point
     */
    @JsonProperty("entry")
    public String getEntryPointString() {
        return this.entryPoint.getID();
    }

    /**
     * Saves a quest into the QuestBuilder.savePath.
     * @return the response message
     * @throws IllegalArgumentException when saving is not safe/possible
     */
    @Key("quest")
    public String save() throws IllegalArgumentException {
        if (this.title.contains("_")) {
            throw new IllegalArgumentException("Quest name '" + this.title + "' not allowed underscores.");
        }

        try {
            FileUtils.create( // create the template json file
                this.savePath + this.title + "_" + this.director.getPlayer().getUniqueId().toString() + ".json", // name pattern
                getTemplateString().getBytes() // put the content in the file
            );
        } catch (IOException e) {
            ChatUtils.sendError(this.director.getPlayer(), e.getMessage(), e);
            return "Quest Builder: '" + this.title + "' could not save.";
        }

        return "Quest Builder: '" + this.title + "' was saved";
    }

    /**
     * Get all the stage IDs on this quest
     * @return list of the stage IDs
     */
    @JsonIgnore
    public List<String> getStages() {
        return new ArrayList<String>(this.questPlan.keySet());
    }

    // public List<QuestAction> getActions() {
        // TODO: create QuestAction outline
    // }

    /**
     * Get the entire quest plan map.
     * @return map of the quest objects and values
     */
    @JsonProperty("stages")
    public Map<String, QuestStage> getQuestPlan() {
        return this.questPlan;
    }

    /**
     * Get the quest NPCs that have been created.
     * @return map of quest NPCs
     */
    @JsonProperty("npcs")
    public Map<String, QuestNPC> getQuestNPCs() {
        // TODO: filter out, out of bounds NPCs (npc_-1); as they have been improperly formed

        return this.questNPCs;
    }
}