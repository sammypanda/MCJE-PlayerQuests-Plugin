package playerquests.builder.quest.component;

import com.fasterxml.jackson.annotation.JsonIgnore; // remove fields from showing when json serialised

import playerquests.client.ClientDirector;

public class QuestStage {

    /**
     * Director to retrieve values
     */
    private ClientDirector director;

    /**
     * The id for the stage
     */
    private String stageID = "stage_-1";

    public QuestStage(ClientDirector director, Integer stageIDNumber) {
        this.director = director;
        this.stageID = "stage_"+stageIDNumber;

        // set as the current instance in the director
        director.setCurrentInstance(this);
    }

    @JsonIgnore
    public String getID() {
        return this.stageID;
    }

    @JsonIgnore
    public String getTitle() {
        return this.stageID;
    }
}
