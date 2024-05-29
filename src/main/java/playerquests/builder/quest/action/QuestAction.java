package playerquests.builder.quest.action;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type

import com.fasterxml.jackson.annotation.JsonBackReference; // stops infinite recursion
import com.fasterxml.jackson.annotation.JsonIgnore; // ignoring fields when serialising
import com.fasterxml.jackson.annotation.JsonProperty; // defining fields when serialising
import com.fasterxml.jackson.annotation.JsonSubTypes; // defines sub types of an abstract class
import com.fasterxml.jackson.annotation.JsonTypeInfo; // where to find type definition

import playerquests.builder.quest.data.ActionOption; // enums for possible options to add to an action
import playerquests.builder.quest.data.ConnectionsData; // indicates where this action is in the quest
import playerquests.builder.quest.npc.QuestNPC; // represents NPCs
import playerquests.builder.quest.stage.QuestStage; // represents quest stages
import playerquests.client.quest.QuestClient; // the quester themselves

/**
 * Passes and handles the quest stage action 'types'.
 * <p>
 * Quest actions are pre-defined behaviours that make
 * it possible to do more with quests. They
 * generally simplify more complex operations.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = None.class, name = "None"),
    @JsonSubTypes.Type(value = Speak.class, name = "Speak")
})
public abstract class QuestAction {

    /**
     * The list of ActionOptions
     */
    @JsonProperty("options")
    protected List<ActionOption> actionOptions;

    /**
     * The NPC this action is from (if applicable)
     */
    @JsonProperty("npc")
    protected String npc;

    /**
     * The dialogue (if applicable)
     */
    @JsonProperty("dialogue")
    protected List<String> dialogue;

    /**
     * The parent stage this action belongs to.
     */
    @JsonBackReference
    private QuestStage stage;

    /**
     * The ID of this action.
     */
    private String action;

    /**
     * The string representation of the type.
     */
    private String type;

    /**
     * The connections for the quest action.
     */
    @JsonProperty("connections")
    private ConnectionsData connections = new ConnectionsData();

    /**
     * Default constructor (for Jackson)
    */
    public QuestAction() {}

    /**
     * Not intended to be created directly, is abstract class for action types.
     * <p>
     * Use .submit() on this method to add it to it's quest stage.
     * See docs/README for list of action types.
     * @param stage stage this action belongs to
    */
    public QuestAction(QuestStage stage) {
        this.stage = stage;
        this.action = "action_-1";
        this.actionOptions = this.initOptions();
        this.type = this.getClass().getSimpleName();
    }

    /**
     * Shows a list of all the action types that could be added to a quest stage.
     * @return list of every action type
     */
    public static List<String> allActionTypes() {
        List<String> actionTypes = new ArrayList<>();

        actionTypes.add("None");
        actionTypes.add("Speak");

        return actionTypes;
    }

    @Override
    public String toString() {
        return this.action;
    }

    /**
     * Gets the string representation of the type.
     * @return current action type as a string
     */
    @JsonIgnore
    public String getType() {
        return this.type;
    }

    /** 
     * Gets this action's ID in the stage.
     * @return current action ID (name)
    */
    @JsonProperty("id")
    public String getID() {
        return this.action;
    }

    /** 
     * Sets this action's ID.
     * @param ID new action ID (name)
    */
    public String setID(String ID) {
        return this.action = ID;
    }
    
    /**
     * Gets the stage this action belongs to.
     * @return current quest stage instance
     */
    @JsonIgnore
    public QuestStage getStage() {
        return this.stage;
    }

    /**
     * Submits this function to the stage.
     * <p>
     * This also gives it a valid ID.
     * @return the quest action submitted to the quest stage
     */
    public QuestAction submit() {
        this.stage.addAction(this);

        return this;
    }

    /**
     * Add option enums to list so the quest knows
     * what options to process.
     * @return a list of action option enums
     */
    public abstract List<ActionOption> initOptions();

    /**
     * Get a list of options attributed to this
     * action.
     * @return a list of action option enums
     */
    public List<ActionOption> getActionOptions() {
        return this.actionOptions;
    }

    /**
     * Get the NPC this action is emitted from.
     */
    @JsonIgnore
    public QuestNPC getNPC() {
        return this.stage.getQuest().getNPCs().get(this.npc);
    }

    /**
     * Set the NPC this action is emitted from.
     */
    public void setNPC(QuestNPC npc) {
        if (npc == null) {
            return;
        }

        this.npc = npc.getID();
    }

    /**
     * Get the dialogue to emit.
     */
    public List<String> getDialogue() {
        return this.dialogue;
    }

    /**
     * Set the NPC this action is emitted from.
     * @return the modified quest action
     */
    public QuestAction setDialogue(List<String> dialogue) {
        this.dialogue = dialogue;

        return this;
    }

    /**
     * What is done when this quest action is called.
     */
    public abstract void Run(QuestClient quester);

    /**
     * Get what quest stages/actions are connected to the current one.
     * @return quest connections object
     */
    @JsonIgnore
    public ConnectionsData getConnections() {
        return this.connections;
    }
}
