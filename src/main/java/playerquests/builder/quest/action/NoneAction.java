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
    protected void prepare(ActionData<? extends ActionListener<?>> context) {}

    @Override
    protected Boolean validate(ActionData<? extends ActionListener<?>> context) {
        return true;
    }

    @Override
    protected void onSuccess(ActionData<? extends ActionListener<?>> context) {}

    @Override
    protected void onFailure(ActionData<? extends ActionListener<?>> context) {}

    @Override
    protected ActionListener<?> startListener(ActionData<? extends ActionListener<?>> context) {
        return new NoneListener(this, context);
    }
    
}
