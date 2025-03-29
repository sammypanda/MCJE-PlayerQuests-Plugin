package playerquests.builder.quest.action.listener;

import playerquests.builder.quest.action.RequestItemAction;
import playerquests.builder.quest.data.QuesterData;

/**
 * Listens for if the request item has been collected.
 */
public class RequestItemListener extends ActionListener<RequestItemAction> {

    public RequestItemListener(RequestItemAction action, QuesterData questerData) {
        super(action, questerData);
    }
    
}
