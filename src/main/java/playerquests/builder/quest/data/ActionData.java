package playerquests.builder.quest.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.action.listener.ActionListener;
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
     * Useful for pulling in values.
     */
    @JsonIgnore
    private ActionListener<?> listener;

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
     * Default constructor for Jackson
     */
    public ActionData() {}

    /**
     * Constructor for providing action context.
     * Args (if you're sure they aren't needed) can be nullified.
     * @param action the action that owns this data
     * @param id the unique identifier for the action
     * @param listener the action listener for this action
     * @param nextActions the actions slated to come next
     */
    public ActionData( 
        QuestAction action,
        String id,
        ActionListener<?> listener,
        List<StagePath> nextActions
    ) {
        this.action = action;
        this.id = id;
        this.listener = listener;
        this.nextActions = nextActions;
    }

    /**
     * Gets the action listener associated with this action.
     * @return the listener
     */
    public ActionListener<?> getListener() {
        return listener;
    }

    /**
     * Sets the action listener for this action.
     * @param actionListener the listener that will trigger action checking
     * @return the passed in listener
     */
    public ActionListener<?> setListener(ActionListener<?> actionListener) {
        this.listener = actionListener;
        return actionListener;
    }

    /**
     * Gets the action listener for this action.
     * @return the listener that will trigger action checking
     */
    public List<StagePath> getNextActions() {
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
}
