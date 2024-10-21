package playerquests.builder.quest.action;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.SpeakListener;
import playerquests.builder.quest.data.QuesterData;

public class SpeakAction extends QuestAction {

    @Override
    public String getName() {
        return "Speak";
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
        return new SpeakListener(this, questerData);
    }
    
}
