package playerquests.builder.quest.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonValue;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.product.Quest;
import playerquests.utility.exception.MissingActionException;
import playerquests.utility.exception.MissingStageException;

/**
 * Represents a path within a quest, consisting of a stage and optionally an action.
 * 
 * This class encapsulates a path within a quest by storing a stage ID and optionally an action ID. 
 * It supports initialization from different types of input, including:
 * <ul>
 *   <li>A single string representation in the format "stage[action]" or "stage" (suitable for Jackson deserialization).</li>
 *   <li>{@code QuestStage} and {@code QuestAction} objects, where their IDs are used to set the stage and action.</li>
 *   <li>Separate strings for stage and action IDs.</li>
 * </ul>
 * 
 * The class provides methods to retrieve the associated {@code QuestStage} and {@code QuestAction} 
 * objects from a {@code Quest} based on the stored IDs. It also supports serialization and deserialization 
 * of paths.
 */
public class StagePath {

    /**
     * The ID of the stage.
     */
    private String stage;

    /**
     * The ID of the action.
     */
    private List<String> actions = new ArrayList<String>();

    /**
     * Constructs a new {@code StagePath} from a string value. 
     * (suitable for Jackson deserialization).
     * 
     * The string is expected to be in the format "stage[action]" or "stage". 
     * The part before the period is considered the stage ID, and the part after (if present) is considered the action ID.
     * 
     * @param path the path string representing the stage and optionally an action
     */
    public StagePath(String path) {
        // segment[0] = stage_?
        // segment[1] (if exists) = action_?
        String[] segments = path.split("\\.");

        // get the stage
        this.stage = segments[0];

        // get the actions
        if (segments.length > 1) {
            this.actions = List.of(segments[1].split(","));
        }
    }

    /**
     * Constructs a new {@code StagePath} from {@code QuestStage} and optional {@code QuestAction} objects.
     * 
     * - The ID of the provided {@code QuestStage} is used as the stage ID. 
     * - If {@code QuestAction}s are provided, their IDs are kept as a list.
     * 
     * @param stage the {@code QuestStage} object representing the stage
     * @param action the {@code QuestAction} object representing the action
     */
    public StagePath(QuestStage stage, @Nullable List<QuestAction> action) {
        // store stage ID
        this.stage = stage.getID();

        if (action != null) {
            // store action IDs
            this.actions = action.stream()
                .map(QuestAction::getID)
                .collect(Collectors.toList());
        }
    }

    /**
     * Returns the quest stage ID.
     * 
     * @return the quest stage ID
     */
    public String getStage() {
        if (this.stage == null) {
            throw new MissingStageException(
                "Stage ID is null.",
                new IllegalStateException("Stage ID must always exist in StagePath.")
            );
        }

        return this.stage;
    }

    /**
     * Determine whether this StagePath has any actions 
     * listed in it.
     * 
     * @return if the StagePath has actions.
     */
    public boolean hasActions() {
        return !this.actions.isEmpty();
    }

    /**
     * Gets all the action IDs in this path.
     * 
     * @return list of action IDs.
     */
    public List<String> getActions() {
        if (!this.hasActions()) {
            throw new MissingActionException(
                "No actions are in this StagePath.",
                new IllegalStateException("Action IDs must be set before retrieval.")
            );
        }

        return this.actions;
    }

    /**
     * Returns the {@code QuestStage} object associated with this path.
     * 
     * @param quest the {@code Quest} object containing the stage
     * @return the {@code QuestStage} object for the stored stage ID
     */
    public QuestStage getStage(Quest quest) {
        if (this.stage == null) {
            // Attempt to retrieve the first available stage
            Map<String, QuestStage> stages = quest.getStages();

            // shouldn't be requesting a stage that won't exist
            if (stages.isEmpty()) {
                throw new MissingStageException(
                    String.format("No stage found for a StagePath in quest: %s", quest.getID()),
                    new NoSuchElementException("StagePath contains no stages.")
                );
            }

            return stages.values().iterator().next(); // Return the first stage
        }

        // Retrieve the stage associated with the stored ID
        QuestStage stage = quest.getStages().get(this.stage);
        if (stage == null) {
            throw new MissingStageException(
                String.format("No stage found for ID: %s in quest: %s.", this.stage, quest.getID()),
                new NoSuchElementException("Stage not present in the quest stages.")
            );
        }

        return stage;
    }

    /**
     * Returns the {@code QuestAction} list associated with this path.
     * 
     * @param quest the {@code Quest} object containing the actions
     * @return the {@code QuestAction} list based on the action IDs in the path
     */
    public List<QuestAction> getActions(Quest quest) {
        List<QuestAction> actions = new ArrayList<QuestAction>();

        this.actions.forEach(action_id -> {
            QuestAction action = this.getStage(quest).getActions().get(action_id);

            // check the action exists in the actions map
            if (action == null) {
                return; // exit if it's not in the actions map
            }

            // add it to the list
            actions.add(action);
        });

        return actions;
    }

    /**
     * Returns a string representation of this {@code StagePath}, in the format
     * "[stage ID].[action ID]" or "[stage ID]".
     *
     * @return a string representation of this stage and action reference
     */
    @JsonValue
    public String toString() {
        return String.format("%s%s",
            this.getStage(),
            this.hasActions() ? "."+String.join(",", this.actions) : ""
        );
    }
}