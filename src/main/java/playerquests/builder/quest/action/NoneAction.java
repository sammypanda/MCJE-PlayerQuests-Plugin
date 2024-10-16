package playerquests.builder.quest.action;

import java.util.List;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.NoneListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.stage.QuestStage;

/**
 * An action that does nothing. :D      
 */
public class NoneAction extends QuestAction {

    /**
     * Constructor for jackson.
     */
    public NoneAction() {}

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
    protected void prepare() {}

    @Override
    protected Boolean validate(QuesterData questerData) {
        return true;
    }

    @Override
    protected void onSuccess(QuesterData questerData) {}

    @Override
    protected void onFailure(QuesterData questerData) {}

    @Override
    protected ActionListener<?> startListener(QuesterData questerData) {
        return new NoneListener(this, questerData);
    }

    @Override
    public List<ActionOption> getOptions() {
        return List.of(); // empty for now :)
    }
    
}
