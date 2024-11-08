package playerquests.builder.quest.action.option;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;

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
    public DialogueOption() {}

    /**
     * Constructor including the QuestAction.
     * @param actionData the parent action
     */
    public DialogueOption(ActionData actionData) {
        super(actionData);
    }

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {

        // get the text tooltip to show, indicating what the dialogue is 
        String joinedText = String.join(", ", this.text); // join all the text list elements
        String shortenedText = String.format("%s...", // shorten the text
            joinedText.length() >= 0.8 ? joinedText.substring(0, 8) : joinedText); // cut off at index 8 or put whole dialogue

        return new GUISlot(gui, slot)
            .setLabel(this.text.isEmpty() ? "Set the Dialogue" : String.format("Change the Dialogue (%s)", shortenedText))
            .setItem(Material.OAK_SIGN)
            .onClick(() -> {
                Bukkit.broadcastMessage("Implement Me :)"); // TODO: implement setting dialogue text entries
            });
    }

    /**
     * Get all the dialogue text entries.
     * @return the lines of dialogue
     */
    public List<String> getText() {
        this.text.clear();
        this.text.add("[Dialogue]");
        // TODO: remove test entry ^

        return this.text;
    }
}
