package playerquests.builder.quest.action;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.NoneListener;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.stage.QuestStage;

/**
 * An action that does nothing. :D      
 */
public class NoneAction extends QuestAction {

    /**
     * Skips the currently tasked quest action.
     * @param stage stage this action belongs to
     */
    public NoneAction(QuestStage stage) {
        super(stage);
    }

    @Override
    public String getName() {
        return "None";
    }

    @Override
    protected void prepare(ActionData actionData) {}

    @Override
    protected Boolean validate(ActionData actionData) {
        return true;
    }

    @Override
    protected void onSuccess(ActionData actionData) {}

    @Override
    protected void onFailure(ActionData actionData) {}

    @Override
    protected ActionListener<?> startListener(ActionData actionData) {
        return new NoneListener(this, actionData);
    }
    
}
