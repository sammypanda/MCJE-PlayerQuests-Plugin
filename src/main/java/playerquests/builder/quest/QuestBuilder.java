package playerquests.builder.quest;

import java.util.ArrayList; // array list type
import java.util.HashMap; // hash table map type
import java.util.LinkedList;
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.UUID;
import java.util.stream.Collectors; // accumulating elements from a stream into a type
import java.util.stream.IntStream; // used to iterate over a range

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from serialising to json
import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property

import playerquests.Core; // gets the KeyHandler singleton
import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder
import playerquests.client.ClientDirector; // abstractions for plugin functionality
import playerquests.product.Quest; // quest product class
import playerquests.utility.ChatUtils; // sends message in-game
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * For creating and managing a Quest.
 */
public class QuestBuilder {

    /**
     * Whether the quest is valid
     */
    private Boolean isValid = true;

    /**
     * Whether the plugin has a creator/is universal
     */
    private Boolean universal = false;

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
     * The original creator of this quest.
     */
    private UUID originalCreator;

    /**
     * Operations to run whenever the class is instantiated.
     */
    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);
    }

    /**
     * Creates and returns a new default Quest.
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
     * Returns a new quest from an existing quest
     * product object.
     * @param director used to control the plugin
     * @param product the quest template to create a new builder from
     */
    public QuestBuilder(ClientDirector director, Quest product) {
        try {
            this.director = director;

            // set the new quest title the same as the product quest title
            this.title = product.getTitle();

            // add the entry point stage from the product
            this.entryPoint = product.getStages().get(product.getEntry());
            director.setCurrentInstance(this.entryPoint); // make it modifiable

            // add the stages from the product
            this.questPlan = product.getStages();

            // recurse submission of stages to KeyHandler registry
            this.questPlan.values().stream().forEach(stage -> {
                Core.getKeyHandler().registerInstance(stage);
            });

            // add the NPCs from the product
            product.getNPCs().forEach((id, npc) -> {
                npc.setID(id);
                this.questNPCs.put(id, npc);
            });

            if (product.getCreator() == null) {
                // set the quest as a universal one
                this.universal = true;
                director.setCurrentInstance(this.build());
            } else {
                if (product.getCreator() != director.getPlayer().getUniqueId()) {
                    this.originalCreator = product.getCreator();
                }
                
                // set as the current quest in the director
                director.setCurrentInstance(this);
            }

            // create quest product from this builder
            this.build();
        } catch (Exception e) {
            this.isValid = false;
        }
    }

    /**
     * Get the player who originally created this quest.
     * @return the original creators player UUID.
     */
    @JsonIgnore
    public UUID getOriginalCreator() {
        return this.originalCreator;
    }

    /**
     * Add a creator to an otherwise universal quest.
     * @param director the client director to refer to the creator via
     */
    public QuestBuilder setDirector(ClientDirector director) {
        this.director = director;
        
        if (director != null) {
            director.setCurrentInstance(this);
            this.universal = false;
            this.build();
        }

        return this;
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
     * Should be a stage.
     * @return the string representation for the entry point
     */
    @JsonProperty("entry")
    public String getEntryPointString() {
        return this.entryPoint.getID();
    }

    /**
     * Sets the entry point for this quest.
     * <p>
     * Should be a stage.
     * @param stage what the entry point stage is
     */
    public void setEntryPoint(QuestStage stage) {
        this.entryPoint = stage;
    }

    /**
     * Get all the stage IDs on this quest
     * @return list of the stage IDs
     */
    @JsonIgnore
    public LinkedList<String> getStages() {
        return new LinkedList<String>(this.questPlan.keySet());
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
        // compose the quest product from the builder state
        Quest product = new Quest(
            this.title,
            this.entryPoint,
            this.questNPCs,
            this.questPlan,
            this.universal ? null : this.director.getPlayer().getUniqueId()
        );

        // set this quest as in-focus to the creator
        if (!universal) {
            director.setCurrentInstance(product);
        }

        return product;
    }

    public QuestStage addStage(QuestStage questStage) {
        this.getQuestPlan().put(questStage.getID(), questStage);
        this.build(); // push to quest product

        return questStage;
    }

    /**
     * Remove a stage from the quest
     * @param questStage the stage to remove
     * @return whether the stage can be removed
     */
    public Boolean removeStage(QuestStage questStage, Boolean dryRun) {
        Boolean canRemove = true; // whether the stage is safe to remove

        // tests to determine if the quest is dependent on this stage
        canRemove = this.questPlan.get(questStage.getID()).getConnections().isEmpty();
        
        if (dryRun) { // if just to test if removable
            return canRemove; // don't continue
        }

        // remove the stage
        this.questPlan.remove(questStage.getID());
        
        return canRemove;
    }

    /**
     * Remove a stage from the quest
     * @param questStage the stage to remove
     */
    public Boolean removeStage(QuestStage questStage) {
        return this.removeStage(questStage, false);
    }

    /**
     * Checks if everything is correctly set and formed.
     * @return if the NPC object is valid
     */
    @JsonIgnore
    public boolean isValid() {
        if (!this.isValid) {
            return false;
        }

        return this.build().isValid();
    }
}