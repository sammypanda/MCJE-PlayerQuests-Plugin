package playerquests.builder.quest.action;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;

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
     * Starts the action.
     * @param quester who the action is running for.
     */
    public void run(QuestClient quester) {}

    /**
     * Setting up the action before any 
     * checking.
     */
    protected abstract void prepare();

    /**
     * Determines if the action should
     * now finish.
     * @param quester the client for the quester.
     * @param listener instance of the listener that called the check.
     */
    public void check(QuestClient quester, ActionListener<?> listener) {}

    /**
     * Logic to indicate that the quest
     * was successfully completed.
     * Should set values to help other methods.
     * @param quester the client for the quester.
     * @param listener instance of the listener that called the check.
     * @return if was successful
     */
    protected abstract Boolean validate(QuestClient quester, ActionListener<?> listener);

    /**
     * Completes the action.
     * - Determines whether should call 
     * {@link #onSuccess()} or {@link #onFailure()}
     */
    private void stop() {}

    /**
     * Things to do when the action was
     * successfully completed.
     * @return a block of code to run
     */
    protected abstract Runnable onSuccess();

    /**
     * Things to do when the action was
     * aborted early.
     * @return a block of code to run
     */
    protected abstract Runnable onFailure();

    /**
     * Starts listener that will trigger checks.
     * @return the listener for the action
     */
    protected abstract ActionListener<?> startListener();
}
