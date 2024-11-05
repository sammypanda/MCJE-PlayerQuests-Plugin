package playerquests.builder.quest.action;

import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.SpeakListener;
import playerquests.builder.quest.action.option.ActionOption;
import playerquests.builder.quest.action.option.NPCOption;
import playerquests.builder.quest.data.QuesterData;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.product.Quest;

/**
 * Action for an NPC speaking.
 */
public class SpeakAction extends QuestAction {

    /**
     * the NPC added into the world.
     */
    QuestNPC npc;

    /**
     * Default constructor for Jackson.
     */
    public SpeakAction() {}

    @Override
    public String getName() {
        return "Speak";
    }

    @Override
    protected void prepare(QuesterData questerData) {
        this.placeNPC(questerData);
    }

    @Override
    protected Boolean validate(QuesterData questerData) {
        return true;
    }

    @Override
    protected void success(QuesterData questerData) {
        // send message
        questerData.getQuester().getPlayer().sendMessage(
            String.format("<%s> %s", this.npc.getName(), "[Dialogue]")
        );

        // remove the NPC
        this.npc.remove();
    }

    @Override
    protected void failure(QuesterData questerData) {}

    @Override
    protected ActionListener<?> startListener(QuesterData questerData) {
        return new SpeakListener(this, questerData);
    }

    @Override
    public GUISlot createSlot(GUIBuilder gui, Integer slot) {
        return new GUISlot(gui, slot)
            .setLabel(this.getName())
            .setDescription(List.of("Makes an NPC speak."))
            .setItem(Material.OAK_SIGN);
    }

    @Override
    public List<Class<? extends ActionOption>> getOptions() {
        return List.of(
            NPCOption.class
        );
    }

    /**
     * Method to place the NPC into the world.
     * This adds it to the class instance state.
     * @param questerData
     */
    private void placeNPC(QuesterData questerData) {
        Player player = questerData.getQuester().getPlayer(); // find the player
        Quest quest = this.getStage().getQuest(); // find the quest this action belongs to
        Optional<NPCOption> npcOption = this.getData().getOption(NPCOption.class); // find NPC option if applies
            
        if (npcOption.isPresent()) { // if the NPC option exists
            this.npc = npcOption.get().getNPC(quest); // get the NPC from the quest 
            this.npc.place(player); // spawn the NPC for this quester
        }
    }
}
