package playerquests.builder.quest.action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import playerquests.builder.fx.FXBuilder;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.condition.ActionCondition;
import playerquests.builder.quest.action.data.ActionTweaks;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.LocationData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;
import playerquests.client.quest.QuestDiary;
import playerquests.product.Quest;
import playerquests.product.fx.ParticleFX;
import playerquests.utility.event.ActionCompletionEvent;
import playerquests.utility.singleton.Database;

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
    @JsonSubTypes.Type(value = SpeakAction.class, name = "SpeakAction"),
    @JsonSubTypes.Type(value = RequestItemAction.class, name = "RequestItemAction"),
    @JsonSubTypes.Type(value = RewardItemAction.class, name = "RewardItemAction"),
    @JsonSubTypes.Type(value = TakeItemAction.class, name = "TakeItemAction")
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
     * Get the conditions that qualify for this action.
     * @return a list of action condition classes
     */
    @JsonIgnore
    public abstract List<Class<? extends ActionCondition>> getConditions();

    /**
     * Get the tweaks to apply to this action.
     * @return a list of tweaks to apply.
     */
    @JsonIgnore
    public abstract List<ActionTweaks> getTweaks();
    
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
        this.startParticleFX(questerData); // start the FX
        this.startListener(questerData); // start the action listener that triggers checks
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
        this.check(questerData, false);
    }

    /**
     * Determines if the action should
     * now finish.
     * - Determines whether should call 
     * {@link #success(questerData)} or {@link #failure(questerData)}
     * @param questerData the data about the quester playing the action.
     * @param bypassClash skip clash checks.
     */
    public void check(QuesterData questerData, boolean bypassClash) {
        // check if any conditions aren't met
        Boolean conditionsUnmet = this.getData().getConditions().stream().anyMatch(conditional -> {
            if (conditional.isMet(questerData)) {
                return false; // don't do work if this condition already met
            }

            // run the listener for unmet conditions 
            // so they can ask for re-check
            conditional.startListener(questerData);
            this.stop(questerData, true); // stop and halt continuation

            // return that this condition is an unmet one
            return true;
        });

        if (conditionsUnmet) {
            return; // don't continue yet if any conditions are unmet
        }

        // stop if there are unresolved clashes
        if (!bypassClash && !questerData.resolveClashes(this)) {
            return;
        }

        // if not successful don't finish
        if (!this.isCompleted(questerData)) {
            this.failure(questerData);
            return;
        }

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
        questerData.stopListener(this);

        // stop all the FX effects
        questerData.getFX(this).forEach(effect -> {
            effect.stopEffect();
        });

        // remove this action instance from the quest client (the player basically)
        boolean wasUntracked = questerData.getQuester().untrackAction(this);

        // go to next actions
        if (!halt && wasUntracked) {
            this.proceed(questerData);

            // call action completion event
            Bukkit.getServer().getPluginManager().callEvent(
                new ActionCompletionEvent(this, questerData)
            );
        }
    }

    protected abstract Class<?> getListenerType();

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
     * Starts the FX that will indicate the action.
     * @param questerData the data about the quester.
     * @return the FX for the action
     */
    protected void startParticleFX(QuesterData questerData) {
        // don't continue if action specifies NO_FX
        if (this.getTweaks().contains(ActionTweaks.NO_FX)) {
            return;
        }

        // get the questers settings/preferences
        QuestClient quester = questerData.getQuester();
        QuestDiary questerDiary = quester.getDiary();

        // get the player to show the FX to
        Player player = quester.getPlayer();

        // get FX
        ParticleFX particleFX = questerDiary.getActionParticle();
        FXBuilder fxBuilder = new FXBuilder();

        // get the location for the particle
        Optional.ofNullable(this.getLocation()).ifPresent(l -> {
            LocationData location = new LocationData(l);
            
            // offset the location to above where the action takes place
            location.setX(location.getX() + 0.5);
            location.setY(location.getY() + 1.5);
            location.setZ(location.getZ() + 0.5);

            // add particle to FX
            fxBuilder.addParticle(particleFX, location);
        });

        // run an FX task + track it in the QuesterData
        // - it needs to be tracked so we can actually close it hehe
        questerData.addFX(this, fxBuilder.run(player));
    }

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

        // get the stage this action belongs to
        QuestStage stage = this.getStage();

        // designate this action as completed in the database
        String diaryID = questerData.getQuester().getDiary().getID();
        String questID = stage.getQuest().getID();
        StagePath actionPath = new StagePath(stage, List.of(this));
        Database.getInstance().setDiaryEntryCompletion(diaryID, questID, actionPath, true);

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
    
    /**
     * The location in which this action takes place.
     * @return a location data object
     */
    @JsonIgnore
    public abstract LocationData getLocation();

    /**
     * Method to place the NPC into the world.
     * This adds it to the QuesterData.
     * @param questerData
     */
    public QuestNPC placeNPC(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer(); // find the player
        Quest quest = this.getStage().getQuest(); // find the quest this action belongs to
        Optional<NPCOption> npcOption = this.getData().getOption(NPCOption.class); // find NPC option if applies
            
        if (npcOption.isPresent()) { // if the NPC option exists
            QuestNPC npc = npcOption.get().getNPC(quest); // get the NPC from the quest 
            questerData.addNPC(this, npc); // track the NPC
            npc.place(player); // spawn the NPC for this quester
            return npc;
        }

        throw new IllegalStateException("Tried to place an NPC for an action with no NPCOption added");
    }

    /**
     * Method to unplace the NPC from the world.
     * This removes it from the QuesterData.
     * @param questerData
     */
    protected QuestNPC unplaceNPC(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer(); // find the player
        Quest quest = this.getStage().getQuest(); // find the quest this action belongs to
        Optional<NPCOption> npcOption = this.getData().getOption(NPCOption.class); // find NPC option if applies
            
        if (npcOption.isPresent()) { // if the NPC option exists
            QuestNPC npc = npcOption.get().getNPC(quest); // get the NPC from the quest 
            questerData.removeNPC(this, npc); // track the NPC
            npc.remove(player); // unspawn the NPC for this quester
            return npc;
        }

        throw new IllegalStateException("Tried to unplace an NPC for an action with no NPCOption added");
    }
}
