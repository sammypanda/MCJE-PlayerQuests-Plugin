package playerquests.builder.quest.action.option;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.ChatPrompt;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;

/**
 * The action option for defining dialogue.
 */
public class DialogueOption extends ActionOption {

    /**
     * The Dialogue text.
     */
    @JsonProperty("text")
    List<String> text = new ArrayList<>();

    /**
     * Default constructor for Jackson.
     */
    public DialogueOption() {
        // Nothing here
    }

    /**
     * Constructor including the QuestAction.
     * @param actionData the parent action
     */
    public DialogueOption(ActionData actionData) {
        super(actionData);
    }

    @Override
    public synchronized GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        // get the text tooltip to show, indicating what the dialogue is 
        String joinedText = String.join(", ", this.getText()); // join all the text list elements
        String shortenedText = String.format("%s", ChatUtils.shortenString(joinedText, 8)); // shorten the text

        return new GUISlot(gui, slot)
            .setLabel(this.text.isEmpty() ? "Set the Dialogue" : String.format("Change the Dialogue (%s)", shortenedText))
            .setItem(Material.OAK_SIGN)
            .onClick(() -> {
                new ChatPrompt(List.of("Enter dialogue", "none"), director)
                    .onFinish((f) -> {
                        ChatPrompt function = (ChatPrompt) f; // retrieve the function state
                        String response = function.getResponse(); // retrieve the 'ChatPrompt' response from the function state
                        this.setText(List.of(response)); // set the text
                        this.actionData.setOption(this); // set the option
                        screen.refresh();
                    })
                    .execute();
            });
    }

    public void setText(List<String> text) {
        this.text = text;
    }

    /**
     * Get all the dialogue text entries.
     * @return the lines of dialogue
     */
    public List<String> getText() {
        return this.text;
    }

    @Override
    public Optional<String> isValid() {
        if (this.getText().isEmpty()) {
            return Optional.of("Dialogue is missing, try setting the action's dialogue in the options menu.");
        }

        return Optional.empty();
    }
}
