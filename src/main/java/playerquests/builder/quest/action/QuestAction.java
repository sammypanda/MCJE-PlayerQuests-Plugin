package playerquests.builder.quest.action;

import java.util.ArrayList; // array type of list
import java.util.List; // generic list type
import java.util.Optional;

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
 * Represents a quest stage action with predefined behavior.
 * <p>
 * Quest actions define specific behaviors that are executed during a quest. They provide a way to
 * encapsulate complex operations and simplify quest design. This class is abstract and should be 
 * extended by specific action types.
 * </p>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = None.class, name = "None"),
    @JsonSubTypes.Type(value = Speak.class, name = "Speak"),
    @JsonSubTypes.Type(value = GatherItem.class, name = "GatherItem")
})
public abstract class QuestAction {

    /**
     * The list of action options associated with this action.
     */
    @JsonProperty("options")
    protected List<ActionOption> actionOptions;

    /**
     * The ID of the NPC associated with this action, if applicable.
     */
    @JsonProperty("npc")
    protected String npc;

    /**
     * The dialogue associated with this action, if applicable.
     */
    @JsonProperty("dialogue")
    protected List<String> dialogue;

    /**
     * The quest stage that this action belongs to.
     */
    @JsonBackReference
    private QuestStage stage;

    /**
     * The ID of this action.
     */
    private String action;

    /**
     * The connections data for this action, indicating how it is linked to other actions or stages.
     */
    @JsonProperty("connections")
    private ConnectionsData connections = new ConnectionsData();

    /**
     * Default constructor for Jackson deserialization.
     */
    public QuestAction() {}

    /**
     * Constructs a new QuestAction with the specified stage.
     * <p>
     * This constructor initializes the action ID and action options.
     * </p>
     * 
     * @param stage The quest stage this action belongs to.
     */
    public QuestAction(QuestStage stage) {
        this.stage = stage;
        this.action = "action_-1";
        this.actionOptions = this.initOptions();
    }

    /**
     * Provides a list of all possible action types that can be added to a quest stage.
     * 
     * @return A list of action type names.
     */
    public static List<String> allActionTypes() {
        List<String> actionTypes = new ArrayList<>();

        actionTypes.add("None");
        actionTypes.add("Speak");
        actionTypes.add("GatherItem");

        return actionTypes;
    }

    @Override
    public String toString() {
        return this.action;
    }

    /**
     * Gets the type of this action as a string.
     * 
     * @return The class name of the action type.
     */
    @JsonIgnore
    public String getType() {
        return this.getClass().getSimpleName();
    }

    /** 
     * Gets the ID of this action.
     * 
     * @return The action ID.
     */
    @JsonProperty("id")
    public String getID() {
        return this.action;
    }

    /** 
     * Sets the ID of this action.
     * 
     * @param ID The new action ID.
     * @return The updated action ID.
     */
    public String setID(String ID) {
        return this.action = ID;
    }
    
    /**
     * Gets the stage that this action belongs to.
     * 
     * @return The quest stage instance.
     */
    @JsonIgnore
    public QuestStage getStage() {
        return this.stage;
    }

    /**
     * Submits this action to the quest stage.
     * <p>
     * This method adds the action to the stage and assigns a valid ID.
     * </p>
     * 
     * @return The submitted quest action.
     */
    public QuestAction submit() {
        this.stage.addAction(this);

        return this;
    }

    /**
     * Initializes the list of action options for this action.
     * <p>
     * This method should be implemented by subclasses to define specific options.
     * </p>
     * 
     * @return A list of action options.
     */
    public abstract List<ActionOption> initOptions();

    /**
     * Gets the list of action options associated with this action.
     * 
     * @return A list of action options.
     */
    public List<ActionOption> getActionOptions() {
        return this.actionOptions;
    }

    /**
     * Gets the NPC associated with this action, if applicable.
     * 
     * @return The NPC instance associated with this action.
     */
    @JsonIgnore
    public QuestNPC getNPC() {
        return this.stage.getQuest().getNPCs().get(this.npc);
    }

    /**
     * Sets the NPC associated with this action.
     * 
     * @param npc The NPC to associate with this action.
     */
    public void setNPC(QuestNPC npc) {
        if (npc == null) {
            return;
        }

        this.npc = npc.getID();
    }

    /**
     * Gets the dialogue associated with this action.
     * 
     * @return A list of dialogue lines.
     */
    public List<String> getDialogue() {
        return this.dialogue;
    }

    /**
     * Sets the dialogue associated with this action.
     * 
     * @param dialogue A list of dialogue lines to set.
     * @return The updated quest action.
     */
    public QuestAction setDialogue(List<String> dialogue) {
        this.dialogue = dialogue;

        return this;
    }

    /**
     * Executes the action with the given quest client.
     * <p>
     * This method should be implemented by subclasses to define specific behavior.
     * </p>
     * 
     * @param quester The quest client executing this action.
     */
    public abstract void Run(QuestClient quester);

    /**
     * Gets the connections data for this action.
     * 
     * @return The connections data.
     */
    @JsonIgnore
    public ConnectionsData getConnections() {
        return this.connections;
    }

    /**
     * Validates the action and returns any validation errors.
     * <p>
     * This method should be implemented by subclasses to define specific validation logic.
     * </p>
     * 
     * @return An optional containing an error message if invalid, or empty if valid.
     */
    public abstract Optional<String> validate();
}
