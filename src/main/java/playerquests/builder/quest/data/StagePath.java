package playerquests.builder.quest.data;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonValue;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.product.Quest;

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
    private String stage;
    private String action;

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

        // for backwards compatibility and dummies; segment[0] = action_?
        if (segments[0].contains("action")) {
            this.action = segments[0];
            return;
        }

        // for correct string representation/template behaviour
        this.stage = segments[0];
        this.action = null;

        if (segments.length > 1) {
            this.action = segments[1]; 
        }
    }

    /**
     * Constructs a new {@code StagePath} from {@code QuestStage} and optional {@code QuestAction} objects.
     * 
     * The ID of the provided {@code QuestStage} is used as the stage ID. If an action is provided, its ID is used as the action ID.
     * 
     * @param stage the {@code QuestStage} object representing the stage
     * @param action the {@code QuestAction} object representing the action, or {@code null} if there is no action
     */
    public StagePath(QuestStage stage, @Nullable QuestAction action) {
        this.stage = stage.getID();
        this.action = null;

        if (action != null) {
            this.action = action.getID();
        }
    }

    /**
     * Constructs a new {@code StagePath} from disjoined strings.
     * 
     * @param stage the ID of the quest stage
     * @param action the ID of the quest action, or {@code null} if there is no action
     */
    public StagePath(String stage, @Nullable String action) {
        this.stage = stage;
        this.action = action;
    }

    /**
     * Returns the quest stage ID.
     * 
     * @return the quest stage ID
     */
    public String getStage() {
        return stage != null ? stage : "stage_0";
    }

    /**
     * Returns the {@code QuestStage} object associated with this path.
     * 
     * @param quest the {@code Quest} object containing the stages
     * @return the {@code QuestStage} object for the stored stage ID
     */
    public QuestStage getStage(Quest quest) {
        // if somehow the stage is null
        if (stage == null) {
            return quest.getStages().values().iterator().next();
        }

        return quest.getStages().get(stage);
    }

    /**
     * Returns the quest action ID.
     *
     * @return the quest action ID
     */
    public String getAction() {
        return action != null ? action : null;
    }

    /**
     * Returns the {@code QuestAction} object associated with this path.
     * 
     * @param quest the {@code Quest} object containing the actions
     * @return the {@code QuestAction} object for the stored action ID, or the default action if the ID is null
     */
    public QuestAction getAction(Quest quest) {
        QuestStage stage = this.getStage(quest);

        if (this.action == null) {
            return stage.getEntryPoint().getAction(quest); // try next entry point
        }

        return stage.getActions().get(action);
    }

    /**
     * Returns a string representation of this {@code StagePath}, in the format
     * "[stage ID].[action ID]" or "[stage ID]".
     *
     * @return a string representation of this stage and action reference
     */
    @JsonValue
    public String toString() {
        String action = this.getAction(); // will be action if it exists, otherwise null

        return String.format("%s%s",
            this.getStage(),
            action != null ? "."+action : ""
        );
    }
}