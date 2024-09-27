package playerquests.builder.quest.action;

import java.util.ArrayList; // array list type
import java.util.List; // generic list type
import java.util.Optional;

import playerquests.builder.quest.action.listener.ActionListener;
import playerquests.builder.quest.action.listener.EmptyActionListener;
import playerquests.builder.quest.data.ActionOption; // enums for possible options to add to an action
import playerquests.builder.quest.stage.QuestStage; // refers to a stage which this action may belong to
import playerquests.client.quest.QuestClient; // the quester themselves

/**
 * Skips the quest action.
 */
public class None extends QuestAction {

    /**
     * Default constructor (for Jackson)
    */
    public None() {}
    
    /**
     * Skips the currently tasked quest action.
     * @param stage stage this action belongs to
     */
    public None(QuestStage stage) {
        super(stage);
    }

    @Override
    public List<ActionOption> initOptions() {
        return new ArrayList<ActionOption>();
    }

    @Override
    protected Boolean custom_Finish(QuestClient quester, ActionListener<?> listener) {
        return true;
    }

    @Override
    protected ActionListener<?> custom_Listener(QuestClient quester) {
        return new EmptyActionListener(this, quester);
    }

    @Override
    protected void custom_Run(QuestClient quester) {}

    @Override
    protected Optional<String> custom_Validate() {
        return Optional.empty(); // valid
    }

    @Override
    protected Boolean custom_Check(QuestClient quester, ActionListener<?> listener) {
        return true;
    }
}
