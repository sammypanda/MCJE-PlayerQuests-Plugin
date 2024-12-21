package playerquests.builder.quest.action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.stage.QuestStage;

/**
 * The class that lays out how functionality
 * is programmed for quest actions.
 * Requires:
 * - QuestStage constructor
 * - Default constructor (for jackson)
 * @see playerquests.builder.quest.action.option.ActionOption
 * @see playerquests.builder.quest.action.listener.ActionListener
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type") // Specify the property name
@JsonSubTypes({
    // Add concrete actions here
    @JsonSubTypes.Type(value = NoneAction.class, name = "NoneAction"),
    @JsonSubTypes.Type(value = SpeakAction.class, name = "SpeakAction")
})
public abstract class QuestAction {

    /**
     * The quest stage that this action belongs to.
     */
    @JsonBackReference
    private QuestStage stage;

    /**
     * The context data of this action.
     */
    @JsonManagedReference
    @JsonProperty("data")
    private ActionData actionData = new ActionData(this, null, null, null);

    /**
     * Constructor for jackson.
     */
    public QuestAction() {}

    /**
     * Constructs a new QuestAction with the specified stage.
     * This constructor initializes the action ID and action options.
     * @param stage the stage this action belongs to
     */
    public QuestAction(QuestStage stage) {
        this.stage = stage;
    }

    /**
     * Get the option types that qualify for this action.
     * @return a list of action option classes
     */
    @JsonIgnore
    public abstract List<Class<? extends ActionOption>> getOptions();

    /**
     * Gets the stage that this action belongs to.
     * @return The quest stage instance.
     */
    @JsonIgnore
    public QuestStage getStage() {
        return this.stage;
    }

    /**
     * Sets the stage that this action belongs to.
     * @param stage the quest stage
     */
    @JsonBackReference
    public void setStage(QuestStage stage) {
        if (stage == null) {
            return;
        }
        
        this.stage = stage;
    }

    /**
     * Sets the unique identifier for this action.
     * @param id the unique identifier
     */
    public void setID(String id) {
        this.actionData.setID(id);
    }

    /**
     * Gets the unique identifier for this action.
     * @return the unique identifier
     */
    @JsonIgnore
    public String getID() {
        return this.actionData.getID();
    }

    @Override
    public String toString() {
        return this.getID();
    }
    
    /**
     * Gets the name of the action.
     * @return the readable name.
     */
    @JsonIgnore
    public abstract String getName();

    /**
     * Starts the action.
     * @param questerData the data about the quester playing the action.
     */
    public void run(QuesterData questerData) {
        this.prepare(questerData); // prepare the action to be checked
        this.getData().setListener(this.startListener(questerData)); // start the action listener that triggers checks
    }

    /**
     * Setting up the action before any 
     * checking.
     * @param questerData the data about the quester playing the action.
     */
    protected abstract void prepare(QuesterData questerData);

    /**
     * Determines if the action should
     * now finish.
     * - Determines whether should call 
     * {@link #success(questerData)} or {@link #failure(questerData)}
     * @param questerData the data about the quester playing the action.
     */
    public void check(QuesterData questerData) {
        // if not successful don't finish
        if (!this.isCompleted(questerData)) {
            return;
        }

        // TODO: implement action failures

        // run success method
        this.success(questerData);

        // finish the action
        this.stop(questerData);
    }

    /**
     * Logic to indicate that the quest
     * was successfully completed.
     * Should set values to help other methods.
     * @param questerData the data about the quester playing the action.
     * @return if was successful
     */
    protected abstract Boolean isCompleted(QuesterData questerData);

    /**
     * Completes the action.
     * @param questerData the data about the quester playing the action.
     */
    public void stop(QuesterData questerData) {
        this.stop(questerData, false);
    }

    /**
     * Completes the action.
     * @param questerData the data about the quester playing the action.
     * @param halt if to halt continuation
     */
    public void stop(QuesterData questerData, Boolean halt) {
        // close the listener
        this.actionData.getListener().close();

        // remove this action instance from the quest client (the player basically)
        questerData.getQuester().untrackAction(this);

        // go to next actions
        if (!halt) {
            this.proceed(questerData);
        }
    }

    /**
     * Things to do when the action was
     * successfully completed.
     * @param questerData the data about the quester playing the action.
     */
    protected abstract void success(QuesterData questerData);

    /**
     * Things to do when the action was
     * aborted early.
     * @param questerData the data about the quester playing the action.
     */
    protected abstract void failure(QuesterData questerData);

    /**
     * Starts listener that will trigger checks.
     * @param questerData the data about the quester playing the action.
     * @return the listener for the action
     */
    protected abstract ActionListener<?> startListener(QuesterData questerData);

    /**
     * Gets the data attributed to this action.
     * @return the context of this action
     */
	public ActionData getData() {
        return this.actionData;
	}

    /**
     * Gets all the existing QuestAction types annotated.
     * @return all known quest action class types
     */
    @SuppressWarnings("unchecked") // it is checked :)
    public static List<Class<? extends QuestAction>> getAllTypes() {
        JsonSubTypes jsonSubTypes = QuestAction.class.getDeclaredAnnotation(JsonSubTypes.class);

        return Arrays.stream(jsonSubTypes.value())
            .map(type -> type.value())
            .filter(clazz -> QuestAction.class.isAssignableFrom(clazz)) // Type check
            .map(clazz -> (Class<? extends QuestAction>) clazz) // Safe cast
            .collect(Collectors.toList());
    }

    /**
     * Creates the slots in a GUI that would be used
     * to select this action.
     * @param gui the GUI to put the slot on
     * @param slot the position to create the slot in on the GUI
     * @return the GUI slot created
     */
    public abstract GUISlot createSlot(GUIBuilder gui, Integer slot);

    /**
     * Logic to indicate that the quest 
     * action is valid, or requires further editing.
     * @return empty if was successful
     */
    public abstract Optional<String> isValid();

    /**
     * Continues onto the next action(s) according to the context.
     * Warning: Make sure you only put this on actions that require interaction
     *          for success, otherwise you'll get in an infinite loop.
     * @param questerData the context.
     */
    public void proceed(QuesterData questerData) {
        // get next actions
        List<StagePath> nextActions = this.getData().getNextActions();

        // trigger next actions
        if (!nextActions.isEmpty()) {
            questerData.getQuester().start(nextActions, actionData.getAction().getStage().getQuest());
        }
    }

    /**
     * Remove this action from the stage.
     * @return empty optional if was successful
     */
    public Optional<String> delete() {
        return this.getStage().removeAction(this);
    }
}
