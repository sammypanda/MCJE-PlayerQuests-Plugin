package playerquests.builder.quest.action;

import java.util.ArrayList; // array list type
import java.util.Arrays; // generic array type
import java.util.List; // generic list type

import org.bukkit.Bukkit; // bukkit API
import org.bukkit.entity.Player; // represents a bukkit player

import playerquests.builder.quest.data.ActionOption; // enums for possible options to add to an action
import playerquests.builder.quest.stage.QuestStage; // refers to a stage which this action may belong to
import playerquests.client.quest.QuestClient; // the quester themselves

/**
 * Makes an NPC speak to the quester.
 */
public class Speak extends QuestAction {

    /**
     * Default constructor (for Jackson)
    */
    public Speak() {}
    
    /**
     * Produces dialogue from an NPC.
     * @param stage stage this action belongs to
     */
    public Speak(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        List<ActionOption> options = new ArrayList<ActionOption>();

        options.add(ActionOption.NPC);
        options.add(ActionOption.DIALOGUE);

        return options;
    }

    @Override
    public void Run(QuestClient quester) {
        Player player = Bukkit.getPlayer(quester.getPlayer().getUniqueId());

        // insert empty dialogue if none set
        if (this.dialogue == null) {
            this.dialogue = new ArrayList<>(
                Arrays.asList("...")
            );
        }

        // produce dialogue
        dialogue.forEach(line -> {
            player.sendMessage(
                String.format("> %s: \"%s\"", this.getNPC().getName(), line)
            );
        });
    }
}
