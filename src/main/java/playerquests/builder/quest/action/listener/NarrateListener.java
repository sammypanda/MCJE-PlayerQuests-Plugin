package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.NarrateAction;
import playerquests.builder.quest.data.QuesterData;

public class NarrateListener extends ActionListener<NarrateAction> {
    public NarrateListener(NarrateAction action, QuesterData questerData) {
        super(action, questerData);
        this.autoTrigger(questerData);
    }
}
