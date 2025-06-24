package playerquests.builder.quest.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference; // stops infinite recursion
import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.StagePath;
import playerquests.product.Quest; // back reference to quest this stage belongs to

/**
 * Represents a stage in a quest.
 */
public class QuestStage {

    /**
     * The quest this stage belongs to.
     */
    @JsonBackReference
    private Quest quest;

    /**
     * The id for the stage.
     */
    @JsonProperty("id")
    private String id;

    /**
     * The map of actions.
     */
    @JsonManagedReference
    private Map<String, QuestAction> actions = new HashMap<String, QuestAction>();

    /**
     * List of starting points.
     */
    @JsonProperty("startpoints")
    private List<StagePath> startPoints = new ArrayList<StagePath>();

    /**
     * The human readable label of the stage.
     */
    @JsonProperty("label")
    private String label;

    /**
     * Constructs a new {@code QuestStage} with the specified stage ID.
     * @param id the unique identifier for this stage
     */
    public QuestStage(@JsonProperty("id") String id) {
        this.id = id;
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a numeric stage ID.
     * @param quest the quest this stage belongs to
     * @param idNumber the numeric identifier for this stage
     */
    public QuestStage(Quest quest, Integer idNumber) {
        this.id = "stage_"+idNumber;

        // set which quest this stage belongs to
        this.quest = quest;
    }

    /**
     * Constructs a new {@code QuestStage} for the given quest with a fully qualified stage ID.
     * This constructor parses the stage ID from the provided string and initializes the stage.
     * @param quest the quest this stage belongs to
     * @param id the fully qualified stage ID (e.g., "stage_1")
     */
    public QuestStage(Quest quest, String id) {
        this(quest, Integer.parseInt(id.substring(6)));
    }

    /**
     * Sets the quest associated with this stage.
     * @param quest the quest to associate with this stage
     */
    public void setQuest(Quest quest) {
        this.quest = quest;
    }

    /**
     * Gets the quest associated with this stage.
     * @return the quest associated with this stage
     */
    public Quest getQuest() {
        return this.quest;
    }

    /**
     * Returns the unique identifier for this stage.
     * @return the stage ID
     */
    @JsonIgnore
    public String getID() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }

    /**
     * Sets the unique identifier for this stage.
     * @param id the stage ID to set
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Gets a map of actions in this stage.
     * @return the actions
     */
    public Map<String, QuestAction> getActions() {
        return this.actions;
    }

    /**
     * Gets a sorted list of actions
     * @return linked list of actions
     */
    @JsonIgnore
    public List<QuestAction> getOrderedActions() {
        // create an ordered list of stages, ordered by action_[this number]
        LinkedList<QuestAction> orderedList = this.actions.values().stream()
            .sorted(Comparator.comparingInt(action -> {
                String[] parts = action.getID().split("_");

                return Integer.parseInt(parts[parts.length - 1]);
            }))
            .collect(Collectors.toCollection(LinkedList::new));

        return orderedList;
    }

    /**
     * Adds a new action to this stage.
     * This method generates a new action ID, assigns it to the action, and adds it to the actions map.
     * @param action the action to add
     * @return the ID of the newly added action
     */
    @JsonIgnore
    public String addAction(QuestAction action) {
        String actionID = "action_"+this.actions.size(); // get next ID
        action.setID(actionID); // set the ID local to the action
        this.actions.put(action.getID(), action); // add to the actions map
        return actionID;
    }

    /**
     * Gets the actions the quest starts with.
     * @return the starting actions
     */
    public List<StagePath> getStartPoints() {
        if (startPoints == null) {
            return List.of();
        }

        return this.startPoints;
    }

    /**
     * Sets the actions the quest starts with.
     * @param startPoints a list of paths to actions
     */
    public void setStartPoints(List<StagePath> startPoints) {
        if (startPoints == null) {
            this.startPoints = List.of();
            return;
        }

        this.startPoints = startPoints;
    }

    /**
     * Replace an action with another.
     * @param oldAction the action to get rid of.
     * @param newAction the action to replace the old one.
     * @return the completed new action after replacement.
     */
    public QuestAction replaceAction(QuestAction oldAction, QuestAction newAction) {
        String id = oldAction.getID();

        // set inner action meta
        newAction.setStage(this);
        newAction.setID(id);

        // replace the old action
        this.actions.replace(id, newAction);

        // return the completed action
        return newAction;
    }

    /**
     * Remove an action from this stage.
     * @param action the action to remove
     * @return empty if was successful
     */
    public Optional<String> removeAction(QuestAction action) {
        // get all actions
        List<QuestAction> allActions = this.quest.getStages().values().stream()
            .map(QuestStage::getActions)
            .map(Map::values)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        // check if any action of all actions, contains the passed in one as
        // a dependency (a 'next action').
        List<String> dependencies = allActions.stream()
            .filter(actions -> actions.getData().getNextActions().stream()
                .flatMap(path -> path.getActions(quest).stream())
                .anyMatch(actionMatch -> actionMatch.equals(action)))
            .map(actions -> actions.getID())
            .collect(Collectors.toList());


        // if it's depended, then return error message early
        if (!dependencies.isEmpty()) {
            return Optional.of("This action is pointed to by another. Please remove it as a 'next' action. (on " + String.join(", ", dependencies) + ")");
        }

        this.actions.remove(action.getID());
        return Optional.empty(); // success
    }

    /**
     * Gets the human editable label for the stage.
     * @return current human editable label.
     */
    @JsonIgnore
    public String getLabel() {
        if (this.label == null) {
            return this.getID();
        }

        return this.label;
    }

    /**
     * Sets the human editable label for the stage.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Checks if this stage has a label
     * @return true if the stage has a label
     */
    public boolean hasLabel() {
        return this.label != null;
    }
}
