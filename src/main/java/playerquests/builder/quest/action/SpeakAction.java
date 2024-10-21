package playerquests.builder.quest.action;

import java.util.List;

import org.bukkit.Material;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.SpeakListener;
import playerquests.builder.quest.data.QuesterData;

/**
 * Action for an NPC speaking.
 */
public class SpeakAction extends QuestAction {

    /**
     * Default constructor for Jackson.
     */
    public SpeakAction() {}

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

    @Override
    public void createSlot(GUIBuilder gui, Integer slot) {
        new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Makes an NPC speak."))
            .setItem(Material.OAK_SIGN);
    }
    
}
