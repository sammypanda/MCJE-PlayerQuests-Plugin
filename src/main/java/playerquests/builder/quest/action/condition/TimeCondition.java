package playerquests.builder.quest.action.condition;

import org.bukkit.Material;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.data.QuesterData;
import playerquests.client.ClientDirector;

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

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        return new GUISlot(gui, slot)
            .setLabel("Time")
            .setItem(Material.CLOCK);
    }
}
