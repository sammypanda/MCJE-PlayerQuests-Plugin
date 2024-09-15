package playerquests.builder.quest;

import java.util.Comparator;
import java.util.HashMap; // hash table map type
import java.util.LinkedList;
import java.util.Map; // generic map type
import java.util.UUID;
import java.util.stream.Collectors; // accumulating elements from a stream into a type
import java.util.stream.IntStream; // used to iterate over a range

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from serialising to json
import com.fasterxml.jackson.annotation.JsonProperty; // for declaring a field as a json property

import playerquests.Core; // gets the KeyHandler singleton
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.npc.QuestNPC; // quest npc builder
import playerquests.builder.quest.stage.QuestStage; // quest stage builder
import playerquests.client.ClientDirector; // abstractions for plugin functionality
import playerquests.product.Quest; // quest product class
import playerquests.utility.ChatUtils; // sends message in-game
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.annotation.Key; // to associate a key name with a method

/**
 * For creating and managing a Quest.
 * 
 * The {@link QuestBuilder} class provides methods to build and configure quests, including
 * defining stages, adding NPCs, and setting entry points. It also supports loading from existing
 * quest templates and validating the quest setup.
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
    private StagePath entryPoint;

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
     * The amount of stock in the quest.
     */
    private Map<Material, Integer> inventory = new HashMap<>();

    /**
     * Operations to run whenever the class is instantiated.
     * This block registers the instance with the KeyHandler.
     */
    {
        // adding to key-value pattern handler
        Core.getKeyHandler().registerInstance(this);
    }

    /**
     * Creates and returns a new default Quest.
     * 
     * @param director The {@link ClientDirector} used to control the plugin.
     */
    public QuestBuilder(ClientDirector director) {
        this.director = director;

        // default entry point as first stage (stage_0)
        QuestStage stage = new QuestStage(this.build(), 0);
        this.entryPoint = new StagePath(
            stage,
            null
        );
        
        // make it modifiable
        director.setCurrentInstance(stage);

        // add default entry point stage to questPlan map
        this.questPlan.put(stage.getID(), stage);

        // set as the current quest in the director
        director.setCurrentInstance(this);
        this.build(); // build default product
    }

    /**
     * Returns a new quest builder from an existing quest product object.
     * 
     * @param director The {@link ClientDirector} used to control the plugin.
     * @param product The {@link Quest} template to create a new builder from.
     */
    public QuestBuilder(ClientDirector director, Quest product) {
        try {
            this.director = director;

            // set the new quest title the same as the product quest title
            this.title = product.getTitle();

            // add the entry point stage from the product
            QuestStage entryStage = product.getStages().get(product.getEntry().getStage());
            this.entryPoint = new StagePath(entryStage, null);
            director.setCurrentInstance(entryStage); // make it modifiable

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

            // set the creator (if applicable, otherwise it's a universal quest)
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
     * 
     * @return The UUID of the original creator's player.
     */
    @JsonIgnore
    public UUID getOriginalCreator() {
        return this.originalCreator;
    }

    /**
     * Add a creator to an otherwise universal quest.
     * 
     * @param director The {@link ClientDirector} to refer to the creator via.
     * 
     * @return The current {@link QuestBuilder} instance.
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
     * Set the title for the quest.
     * <p>
     * The title is also used as the ID: [Title]_[Owner Player ID].
     * 
     * @param title The name for the quest.
     * 
     * @return The current {@link QuestBuilder} instance.
     */
    @Key("quest.title")
    public QuestBuilder setTitle(String title) {
        if (title.contains("_")) {
            ChatUtils.message("Quest label '" + this.title + "' not allowed underscores.")
                .player(this.director.getPlayer())
                .type(MessageType.WARN)
                .send();
            return this;
        }

        this.title = title; // set the new title
        this.build();
        return this;
    }

    /**
     * Get the quest title.
     * 
     * @return The quest name.
     */
    @Key("Quest")
    public String getTitle() {
        return this.title;
    }

    /**
     * Get the entry point ID.
     * <p>
     * Should be a stage.
     * 
     * @return The string representation of the entry point.
     */
    @JsonProperty("entry")
    public String getEntryPointString() {
        return this.entryPoint.toString();
    }

    /**
     * Set the entry point for this quest.
     * <p>
     * Should be a stage.
     * 
     * @param path The {@link StagePath} representing the entry point stage.
     */
    public void setEntryPoint(StagePath path) {
        this.entryPoint = path;
    }

    /**
     * Get all the stage IDs for this quest.
     * 
     * @return A list of the stage IDs, ordered by stage number.
     */
    @JsonIgnore
    public LinkedList<String> getStages() {
        // create an ordered list of stages, ordered by stage_[this number]
        LinkedList<String> orderedList = this.questPlan.keySet().stream()
            .map(stage -> stage.split("_"))
            .sorted(Comparator.comparingInt(parts -> Integer.parseInt(parts[1])))
            .map(parts -> String.join("_", parts))
            .collect(Collectors.toCollection(LinkedList::new));

        return orderedList;
    }

    /**
     * Get the entire quest plan map.
     * 
     * @return A map of the quest stages with their IDs as keys.
     */
    @JsonProperty("stages")
    public Map<String, QuestStage> getQuestPlan() {
        return this.questPlan;
    }

    /**
     * Get the filtered quest NPCs that have been created.
     * 
     * @return A map of filtered quest NPCs.
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
     * 
     * @param all Whether to show all NPCs or not.
     * 
     * @return A map of quest NPCs, either filtered or unfiltered.
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
     * 
     * @param npc The {@link QuestNPC} object to add to the map.
     * 
     * @return Whether the addition was successful.
     */
    @JsonIgnore
    public Boolean addNPC(QuestNPC npc) {
        Player player = Bukkit.getPlayer(this.getDirector().getPlayer().getUniqueId());

        // remove to replace if already exists
        if (this.questNPCs.containsKey(npc.getID())) {
            this.questNPCs.remove(npc.getID());
            npc.refund(player);
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

        // remove one of the block the npc is being set as
        npc.penalise(player);

        return true;
    }

    /**
     * Removes an NPC from this quest.
     * 
     * @param npc The {@link QuestNPC} object to remove from the map.
     */
    public void removeNPC(QuestNPC npc) {
        // remove from quest list
        this.questNPCs.remove(npc.getID());

        // refund resources
        npc.refund(Bukkit.getPlayer(this.getDirector().getPlayer().getUniqueId()));

        // remove from world
        npc.remove();
    }

    /**
     * Provides the next valid NPC ID.
     * 
     * @return The next valid 'npc_[number]' NPC ID.
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
     * 
     * @return The {@link ClientDirector} instance.
     */
    @JsonIgnore
    public ClientDirector getDirector() {
        return this.director;
    }

    /**
     * Build the quest product from the state of this builder.
     * 
     * @return The constructed {@link Quest} product.
     */
    @JsonIgnore
    public Quest build() {
        // compose the quest product from the builder state
        Quest product = new Quest(
            this.title,
            this.entryPoint,
            this.questNPCs,
            this.questPlan,
            this.universal ? null : this.director.getPlayer().getUniqueId(),
            true, // always toggle cloned quests on when freshly cloned
            this.getID()
        );

        // set this quest as in-focus to the creator
        if (!universal) {
            director.setCurrentInstance(product);
        }

        return product;
    }

    /**
     * Adds a stage to the quest.
     * 
     * @param questStage The {@link QuestStage} to add.
     * 
     * @return The added {@link QuestStage}.
     */
    public QuestStage addStage(QuestStage questStage) {
        this.getQuestPlan().put(questStage.getID(), questStage);
        this.build(); // push to quest product

        return questStage;
    }

    /**
     * Removes a stage from the quest.
     * 
     * @param questStage The {@link QuestStage} to remove.
     * @param dryRun Whether to perform a dry run without actually removing the stage.
     * 
     * @return Whether the stage can be removed.
     */
    public Boolean removeStage(QuestStage questStage, Boolean dryRun) {
        Boolean canRemove = true; // whether the stage is safe to remove

        // tests for if any is dependent on this stage
        Boolean connectionsDependency = !this.questPlan.get(questStage.getID()).getConnections().isEmpty(); // if the stage is connected to anything else
        Boolean entryDependency = this.entryPoint.getStage() == questStage.getID(); // if the stage is part of the quest entry point

        // check if unable to remove (if are dependencies)
        if (connectionsDependency || entryDependency) {
            canRemove = false;
        }
        
        if (dryRun) { // if just to test if removable
            return canRemove; // don't continue
        }

        // remove the stage
        this.questPlan.remove(questStage.getID());
        
        return canRemove;
    }

    /**
     * Removes a stage from the quest.
     * 
     * @param questStage The {@link QuestStage} to remove.
     * 
     * @return Whether the stage was successfully removed.
     */
    public Boolean removeStage(QuestStage questStage) {
        return this.removeStage(questStage, false);
    }

    /**
     * Checks if everything is correctly set and formed.
     * 
     * @return Whether the quest is valid.
     */
    @JsonIgnore
    public boolean isValid() {
        if (!this.isValid) {
            return false;
        }

        return this.build().isValid();
    }

    /**
     * Get the would-be ID of this quest.
     * @return the id for the quest.
     */
    public String getID() {
        // the player creating/editing/saving the quest
        String creator = this.getDirector().getPlayer().getUniqueId().toString(); 

        // the format of the ID
        return String.format("%s%s", 
            title, 
            creator != null ? "_"+creator : ""
        );
    }
}