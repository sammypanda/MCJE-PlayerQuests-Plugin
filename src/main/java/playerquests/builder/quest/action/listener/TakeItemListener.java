package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.TakeItemAction;
import playerquests.builder.quest.data.QuesterData;

public class TakeItemListener extends ActionListener<TakeItemAction> {

    public TakeItemListener(TakeItemAction action, QuesterData questerData) {
        super(action, questerData);
        this.autoTrigger(questerData);
    }
    
}
