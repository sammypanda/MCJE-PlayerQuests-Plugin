package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.RewardItemAction;
import playerquests.builder.quest.data.QuesterData;

public class RewardItemListener extends ActionListener<RewardItemAction> {

    public RewardItemListener(RewardItemAction action, QuesterData questerData) {
        super(action, questerData);
        this.autoTrigger(questerData);
    }
    
}
