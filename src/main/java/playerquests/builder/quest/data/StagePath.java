package playerquests.builder.quest.data;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonValue;

import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.product.Quest;

/**
 * Represents a path within a quest, consisting of a stage and optionally an action.
 * 
 * This class provides various constructors for creating a {@code StagePath} from different types of input
 * and methods for retrieving the stage and action details.
 * 
 * The {@code StagePath} can be initialized using:
 * <ul>
 *   <li>A single string path (suitable for Jackson deserialization), where the string is split by a period (".") to determine the stage and action.</li>
 *   <li>Objects of type {@code QuestStage} and {@code QuestAction}, where the IDs of these objects are used to set the stage and action.</li>
 *   <li>Two separate strings for stage and action.</li>
 * </ul>
 * 
 * This class also includes methods to retrieve the corresponding {@code QuestStage} and {@code QuestAction}
 * objects from a {@code Quest} based on the stored IDs.
 */
public class StagePath {
    private String stage;
    private String action;

    /**
     * Constructs a new {@code StagePath} from string values. 
     * (good for Jackson deserialisation).
     * 
     * @param path the StagePath as a string
     * @param id the Quest ID as a string
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
     * Constructs a new {@code StagePath} from actual objects.
     * 
     * @param path the StagePath as a string
     * @param id the Quest ID as a string
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
     * @param path the StagePath as a string
     * @param id the Quest ID as a string
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
     * Returns the quest stage object.
     *
     * @return the quest stage
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
     * Returns the quest action object.
     *
     * @return the quest action
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