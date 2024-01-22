package playerquests.builder.quest.component;

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised

public class QuestStage {
    /**
     * The id for the stage
     */
    private String stageID = "stage_-1";

    public QuestStage(Integer stageIDNumber) {
        this.stageID = "stage_"+stageIDNumber;
    }

    @JsonIgnore
    public String getID() {
        return this.stageID;
    }
}
