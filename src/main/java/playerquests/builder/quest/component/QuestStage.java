package playerquests.builder.quest.component;

public class QuestStage {
    /**
     * The id for the stage
     */
    private String stageID = "stage_-1";

    public QuestStage(Integer stageIDNumber) {
        this.stageID = "stage_"+stageIDNumber;
    }

    public String getID() {
        return this.stageID;
    }
}
