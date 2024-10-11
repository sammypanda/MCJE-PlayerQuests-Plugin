package playerquests.builder.quest.action;

import playerquests.builder.quest.action.listener.ActionListener;
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
    protected void prepare(ActionData<? extends ActionListener<?>> context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'prepare'");
    }

    @Override
    protected Boolean validate(ActionData<? extends ActionListener<?>> context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validate'");
    }

    @Override
    protected Runnable onSuccess(ActionData<? extends ActionListener<?>> context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onSuccess'");
    }

    @Override
    protected Runnable onFailure(ActionData<? extends ActionListener<?>> context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onFailure'");
    }

    @Override
    protected ActionListener<?> startListener(ActionData<? extends ActionListener<?>> context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'startListener'");
    }
    
}
