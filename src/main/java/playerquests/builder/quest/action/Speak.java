package playerquests.builder.quest.action;

import java.util.Arrays; // generic array type
import java.util.List; // generic list type
import java.util.Optional;

import org.bukkit.entity.Player; // represents a bukkit player

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.EmptyActionListener;
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
        return Arrays.asList(
            ActionOption.NPC,
            ActionOption.DIALOGUE
        );
    }

    @Override
    public void custom_Run(QuestClient quester) {
        // insert empty dialogue if none set
        if (this.dialogue == null) {
            this.dialogue = Arrays.asList("...");
        }
    }

    @Override
    public Optional<String> custom_Validate() {
        if (this.npc == null) {
            return Optional.of("NPC needs to be selected");
        }
        
        return Optional.empty();
    }

    @Override
    public Boolean custom_Check(QuestClient quester, ActionListener<?> listener) {
        // pass check
        return true;
    }

    @Override
    protected ActionListener<?> custom_Listener(QuestClient quester) {
        return new EmptyActionListener(this, quester);
    }

    @Override
    protected Boolean custom_Finish(QuestClient quester, ActionListener<?> listener) {
        // get the player to send to
        Player player = quester.getPlayer();

        // send dialogue
        dialogue.forEach(line -> {
            player.sendMessage(
                String.format("> %s: \"%s\"", this.getNPC().getName(), line)
            );
        });

        return true;
    }
}
