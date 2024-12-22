package playerquests.builder.quest.action.condition;

import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;

public class TimeCondition extends ActionCondition {

    /**
     * Default constructor for Jackson.
     */
    public TimeCondition() {}

    public TimeCondition(ActionData actionData) {
        super(actionData);
    }

    @Override
    public Boolean isMet(QuesterData questerData) {
        long worldTime = questerData.getQuester().getPlayer().getWorld().getTime(); 
        System.out.println(worldTime);

        if (worldTime >= 0 && worldTime <= 1000) {
            return true;
        }

        return false;
    }
}
