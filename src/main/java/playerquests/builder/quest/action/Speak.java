package playerquests.builder.quest.action;

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.entity.Player; // represents a bukkit player

import playerquests.builder.quest.data.ActionOption; // enums for possible options to add to an action
import playerquests.builder.quest.data.ActionOptionData; // the options on this action
import playerquests.builder.quest.stage.QuestStage; // refers to a stage which this action may belong to
import playerquests.client.quest.QuestClient; // the quester themselves

/**
 * Makes an NPC speak to the quester.
 */
// TODO: decorate dialogue
public class Speak extends QuestAction {
    
    /**
     * Produces dialogue from an NPC.
     * @param parentStage stage this action belongs to
     */
    public Speak(QuestStage parentStage) {
        super(parentStage);
    }

    @Override
    public ActionOptionData getActionOptionData() {
        ActionOptionData options = new ActionOptionData();

        options.add(ActionOption.NPC);
        options.add(ActionOption.DIALOGUE);

        return options;
    }

    @Override
    public void Run(QuestClient quester) {
        Player player = Bukkit.getPlayer(quester.getPlayer().getUniqueId());

        dialogue.forEach(line -> {
            player.sendMessage("> " + line);
        });
    }
}
