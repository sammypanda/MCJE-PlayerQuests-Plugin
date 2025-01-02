package playerquests.builder.quest.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.option.ActionOption;

/**
 * The entire game context that could possibly 
 * be needed for actions.
 * Especially useful for checking conditionals.
 */
public class ActionData {

    /**
     * The action this data belongs to.
     */
    @JsonBackReference
    private QuestAction action;

    /**
     * The unique identifier of this action.
     */
    @JsonProperty("id")
    private String id;

    /**
     * The actions slated to come after this one.
     */
    @JsonProperty("next")
    private List<StagePath> nextActions = new ArrayList<>();

    /**
     * The options in this action.
     */
    @JsonProperty("options")
    @JsonManagedReference
    private List<ActionOption> options = new ArrayList<>();

    /**
     * The conditionals that are required for this action to
     * ever start.
     */
    @JsonProperty("conditions")
    @JsonManagedReference
    private List<ActionCondition> conditions = new ArrayList<>();

    /**
     * Default constructor for Jackson
     */
    public ActionData() {}

    /**
     * Constructor for providing action context.
     * Args (if you're sure they aren't needed) can be nullified.
     * @param action the action that owns this data
     * @param id the unique identifier for the action
     * @param nextActions the actions slated to come next
     * @param conditions the conditionals to allow the action to complete
     */
    public ActionData( 
        QuestAction action,
        String id,
        List<StagePath> nextActions,
        List<ActionCondition> conditions
    ) {
        this.action = action;
        this.id = id;
        this.nextActions = nextActions;
        this.conditions = conditions;
    }

    /**
     * Gets the next actions.
     * @return a list of paths to next actions
     */
    public List<StagePath> getNextActions() {
        if (this.nextActions == null) {
            return List.of();
        }

        return this.nextActions;
    }

    /**
     * Adds an action to the list of those slated to come next.
     * @param path path to the quest action
     */
    public void addNextAction(StagePath path) {
        this.nextActions.add(path);
    }

    /**
     * Replaces actions slated to come next.
     * @param nextActions the actions slated to come next
     */
    public void setNextActions(List<StagePath> nextActions) {
        if (nextActions == null) {
            return;
        }

        this.nextActions = nextActions;
    }

    /**
     * Sets the unique identifier for this action.
     * @param id the unique identifier
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Gets the unique identifier for this action.
     * @return the unique identifier
     */
    public String getID() {
        return this.id;
    }

    @JsonProperty("options")
    private void setOptions(List<ActionOption> options) {
        if (options == null) {
            this.options = List.of();
            return;
        }

        this.options = options;
    }

    /**
     * Gets the list of options.
     * @return a list of action options
     */
    public List<ActionOption> getOptions() {
        return this.getAction().getOptions().stream()
            .map(clazz -> {
                // search in existing list for option with matching subclass type
                Optional<ActionOption> existingOption = this.options.stream()
                    .filter(option -> clazz.isAssignableFrom(option.getClass()))
                    .findFirst();

                // don't continue if already in options list
                if (existingOption.isPresent()) {
                    return existingOption.get();
                }

                // create options
                try {
                    // create the instance of the option
                    return clazz.getDeclaredConstructor(ActionData.class).newInstance(this);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(option -> option != null) // filter out any null values
            .collect(Collectors.toList());
    }

    /**
     * Gets the option by the class type.
     * @param <T> the option type
     * @param optionType the class of the option type
     * @return the option object
     */
    public <T extends ActionOption> Optional<T> getOption(Class<T> optionType) {
        return action.getData().getOptions().stream()
            .filter(optionType::isInstance)
            .map(optionType::cast)
            .findFirst();
    }

    /**
     * Get the action this data belongs to.
     * @return a quest action.
     */
    public QuestAction getAction() {
        return this.action;
    }

    /**
     * Set (or replace) the settings of an option.
     * @param option the action option to put
     */
    public void setOption(ActionOption option) {
        this.options.removeIf(o -> option.getClass().isAssignableFrom(o.getClass()));
        this.options.add(option);
    }

    public List<ActionCondition> getConditions() {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }

        return this.conditions;
    }

    /**
     * Remove a condition from the list.
     * @param condition the condition to remove
     * @return the error message, or empty if successful
     */
	public Optional<String> removeCondition(ActionCondition condition) {
        if (!this.conditions.remove(condition)) { // if failed to remove
            return Optional.of("Could not remove this action");
        }

        return Optional.empty(); // successful
	}

    /**
     * Add a condition to the list.
     * @param condition the condition to add
     */
	public void addCondition(ActionCondition condition) {
        this.conditions.add(condition);
	}
}
