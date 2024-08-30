package playerquests.builder.quest.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import playerquests.builder.quest.data.ActionOption;
import playerquests.builder.quest.stage.QuestStage;
import playerquests.client.quest.QuestClient;

/**
 * Action for players gathering items.
 */
public class GatherItem extends QuestAction {

    /**
     * Default constructor (for Jackson)
    */
    public GatherItem() {}
    
    /**
     * Produces dialogue from an NPC.
     * @param stage stage this action belongs to
     */
    public GatherItem(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        List<ActionOption> options = new ArrayList<ActionOption>();

        options.add(ActionOption.ITEMS);

        return options;
    }

    @Override
    public void Run(QuestClient quester) {
        // TODO:
        System.out.println("TODO: create listener to check if player has gathered items and then send message when they have");
        
        // for now just skip
        quester.gotoNext(this);
    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }
    
}
