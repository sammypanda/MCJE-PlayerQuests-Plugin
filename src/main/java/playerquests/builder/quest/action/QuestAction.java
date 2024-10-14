package playerquests.builder.quest.action;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.stage.QuestStage;

/**
 * The class that lays out how functionality
 * is programmed for quest actions.
 * @see playerquests.builder.quest.action.option.ActionOption
 * @see playerquests.builder.quest.action.listener.ActionListener
 */
// TODO: Determine if it's possible to annotate the back reference on the constructor arg to avoid adding the ugly no-arg constructor.
public abstract class QuestAction {

    /**
     * The quest stage that this action belongs to.
     */
    @JsonBackReference
    private final QuestStage stage;

    /**
     * The unique identifier of this action.
     */
    @JsonProperty("id")
    private String id;

    /**
     * Constructs a new QuestAction with the specified stage.
     * This constructor initializes the action ID and action options.
     * @param stage the stage this action belongs to.
     */
    public QuestAction(QuestStage stage) {
        this.stage = stage;
    }

    /**
     * Gets the stage that this action belongs to.
     * @return The quest stage instance.
     */
    @JsonIgnore
    public QuestStage getStage() {
        return this.stage;
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
    @JsonIgnore
    public String getID() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.getID();
    }
    
    /**
     * Gets the name of the action.
     * @return the readable name.
     */
    public abstract String getName();

    /**
     * Starts the action.
     * @param actionData the action data for the current runtime.
     */
    public void run(ActionData actionData) {}

    /**
     * Setting up the action before any 
     * checking.
     * @param actionData the action data for the current runtime.
     */
    protected abstract void prepare(ActionData actionData);

    /**
     * Determines if the action should
     * now finish.
     * @param actionData the action data for the current runtime.
     */
    public void check(ActionData actionData) {}

    /**
     * Logic to indicate that the quest
     * was successfully completed.
     * Should set values to help other methods.
     * @param actionData the action data for the current runtime.
     * @return if was successful
     */
    protected abstract Boolean validate(ActionData actionData);

    /**
     * Completes the action.
     * - Determines whether should call 
     * {@link #onSuccess(ActionData)} or {@link #onFailure(ActionData)}
     * @param actionData the action data for the current runtime.
     */
    protected void stop(ActionData actionData) {}

    /**
     * Things to do when the action was
     * successfully completed.
     * @param actionData the action data for the current runtime.
     */
    protected abstract void onSuccess(ActionData actionData);

    /**
     * Things to do when the action was
     * aborted early.
     * @param actionData the action data for the current runtime.
     */
    protected abstract void onFailure(ActionData actionData);

    /**
     * Starts listener that will trigger checks.
     * @param actionData the action data for the current runtime.
     * @return the listener for the action
     */
    protected abstract ActionListener<?> startListener(ActionData actionData);
}
