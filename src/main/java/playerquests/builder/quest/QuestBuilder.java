package playerquests.builder.quest;

import java.io.IOException; // thrown if a file cannot be created
import java.util.ArrayList; // array list type
import java.util.HashMap; // hash table map type
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.stream.Collectors; // accumulating elements from a stream into a type
import java.util.stream.IntStream; // used to iterate over a range

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from serialising to json
import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property
import com.fasterxml.jackson.core.JsonProcessingException; // thrown when json cannot serialise
import com.fasterxml.jackson.databind.ObjectMapper; // turns objects into json
import com.fasterxml.jackson.databind.SerializationFeature; // configures json serialisation 

import playerquests.Core; // gets the KeyHandler singleton
import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder
import playerquests.client.ClientDirector; // abstractions for plugin functionality
import playerquests.product.Quest; // quest product class
import playerquests.utility.ChatUtils; // sends message in-game
import playerquests.utility.FileUtils; // creates files
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * For creating and managing a Quest.
 */
// TODO: create QuestAction outline
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
        this.entryPoint = new QuestStage(this.build(), 0);
        director.setCurrentInstance(this.entryPoint); // make it modifiable

        // add default entry point stage to questPlan map
        this.questPlan.put(this.entryPoint.getID(), this.entryPoint);

        // set as the current quest in the director
        director.setCurrentInstance(this);
        this.build(); // build default product
    }

    /**
     * Title for the quest.
     * <p>
     * Also used as the ID: [Title]_[Owner Player ID]
     * @param title the name for the quest
     */
    @Key("quest.title")
    public void setTitle(String title) {
        if (title.contains("_")) {
            ChatUtils.sendError(this.director.getPlayer(), "Quest label '" + this.title + "' not allowed underscores.");
            return;
        }

        this.title = title; // set the new title
        this.build();
    }

    /**
     * Gets the quest title.
     * @return quest name
     */
    @Key("Quest")
    public String getTitle() {
        return this.title;
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
     * Get all the stage IDs on this quest
     * @return list of the stage IDs
     */
    @JsonIgnore
    public List<String> getStages() {
        return new ArrayList<String>(this.questPlan.keySet());
    }

    /**
     * Get the entire quest plan map.
     * @return map of the quest objects and values
     */
    @JsonProperty("stages")
    public Map<String, QuestStage> getQuestPlan() {
        return this.questPlan;
    }

    /**
     * Get the filtered quest NPCs that have been created.
     * @return map of quest NPCs
     */
    @JsonProperty("npcs")
    public Map<String, QuestNPC> getQuestNPCs() {
        // Remove invalid/out of bound NPC IDs
        Map<String, QuestNPC> filteredNPCs = this.questNPCs.entrySet().stream() // get questnpcs map as set stream (loop)
            .filter(entry -> entry.getKey() != "npc_-1") // filter out all IDs that are out of bounds
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)); // set the result

        return filteredNPCs;
    }

    /**
     * Get the quest NPCs that have been created.
     * @param all whether to show all npcs or not
     * @return map of quest NPCs
     */
    @JsonProperty("npcs")
    public Map<String, QuestNPC> getQuestNPCs(Boolean all) {
        if (!all) {
            return this.getQuestNPCs(); // return filtered
        } else {
            return this.questNPCs; // return all NPCs, including invalid/unfiltered
        }
    }

    /**
     * Adds an NPC to this quest.
     * @param npc the npc object to add to the map
     * @return if was successful
     */
    @JsonIgnore
    public Boolean addNPC(QuestNPC npc) {
        // remove to replace if already exists
        if (this.questNPCs.containsKey(npc.getID())) {
            this.questNPCs.remove(npc.getID());
        }

        // set this quest as the npc parent
        npc.setQuest(this.build());

        // run checks
        if (!npc.isValid()) {
            npc.setID("npc_-1"); // mark as incomplete
            npc.setQuest(null);
            return false;
        }

        // add new valid NPC
        npc.setID(this.nextNPCID()); // set this npc with a valid ID
        this.questNPCs.put(npc.getID(), npc); // put valid NPC in the quest npc list
        return true;
    }

    /**
     * Removes an NPC from this quest.
     * @param npc the npc object to remove from the map
     */
    public void removeNPC(QuestNPC npc) {
        this.questNPCs.remove(npc.getID());
    }

    /**
     * Provides what the next NPC ID would be.
     * @return the next valid 'npc_[number]' NPC ID
     */
    @JsonIgnore
    public String nextNPCID() {
        // count up to compensate for previous dropped IDs
        Integer npcID = IntStream.iterate(0, i -> i + 1)
            .filter(i -> !this.questNPCs.keySet().contains("npc_"+i)) // check if the id at this count exists
            .findFirst() // stop iterating when found a gap (an id not contained in the npc list)
            .orElse(-1); // default to npc_-1 if the list is empty

        return "npc_" + npcID;
    }

    /**
     * Get the director instance which owns this builder.
     * @return the client director instance
     */
    @JsonIgnore
    public ClientDirector getDirector() {
        return this.director;
    }

    /**
     * Build the quest product from the state of this builder.
     */
    @JsonIgnore
    public Quest build() {
        Quest product = new Quest(
            this.title,
            this.entryPoint,
            this.questNPCs,
            this.questPlan,
            this.director.getPlayer().getUniqueId()
        );

        director.setCurrentInstance(product);
        return product;
    }
}