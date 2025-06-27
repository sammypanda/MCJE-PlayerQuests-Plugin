package playerquests.builder.gui.dynamic;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.ChatPrompt;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.ActionData;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageType;

/**
 * Shows a GUI used for editing a quest action.
 */
public class Dynamicactioneditor extends GUIDynamic {

    /**
     * The action being edited.
     */
    private QuestAction action;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.action = (QuestAction) this.director.getCurrentInstance(QuestAction.class);
    }

    @Override
    protected void execute_custom() {
        ActionData actionData = this.action.getData();

        // set frame title/style
        this.gui.getFrame().setTitle(String.format("%s Editor", this.action.getLabel()))
                           .setSize(9);

        // the back button
        new GUISlot(this.gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .onClick(() -> {
                // do not allow leaving if there is an issue! dramaaaa
                Optional<String> issueMessage = this.action.isValid();
                if (!issueMessage.isEmpty()) {
                    ChatUtils.message(issueMessage.get())
                        .style(MessageStyle.PRETTY)
                        .type(MessageType.WARN)
                        .player(this.director.getPlayer())
                        .send();
                    return;
                }

                // go back to the previous screen
                new UpdateScreen(
                    List.of(this.previousScreen),
                    director
                ).execute();
            });

        // set action label button
        new GUISlot(gui, 2)
            .setItem(Material.OAK_SIGN)
            .setLabel(String.format("%s action label",
                this.action.hasLabel() ? "Change" : "Set"
            ))
            .onClick(() -> {
                new ChatPrompt(
                    Arrays.asList("Type a label to help you remember the action", "none"),
                    director
                ).onFinish((func) -> {
                    ChatPrompt function = (ChatPrompt) func;
                    String response = function.getResponse();
                    this.action.setLabel(response);
                    this.refresh();
                }).execute();
            });

        // select next actions button
        new GUISlot(this.gui, 3)
            .setItem(Material.HOPPER)
            .setLabel("Next Actions")
            .setDescription(List.of("Select actions to come after this one."))
            .onClick(() -> {
                this.director.setCurrentInstance(action); // set this action as the one to edit
                new UpdateScreen(Arrays.asList("nextactioneditor"), director).execute(); // open 'next action editor' screen
            });

        // change action type button
        new GUISlot(this.gui, 4)
            .setItem(Material.FIREWORK_ROCKET)
            .setLabel("Change action type")
            .setDescription(List.of(
                String.format("Currently: %s", action.getName())
            ))
            .onClick(() -> {
                this.director.setCurrentInstance(this.action);
                new UpdateScreen(List.of("actiontypeselector"), director).execute();
            });

        // options editor button
        if (!action.getOptions().isEmpty()) {
            // only show if the action has more than one option
            new GUISlot(this.gui, 5)
                .setItem(Material.STONE_BUTTON)
                .setLabel("Edit action options")
                .onClick(() -> {
                    this.director.setCurrentInstance(actionData);
                    new UpdateScreen(List.of("optioneditor"), director).execute();
                });
        }

        // conditions editor button
        new GUISlot(this.gui, 6)
            .setItem(Material.CLOCK)
            .setLabel("Edit conditions")
            .setDescription(
                List.of("Things like what times the", "action can be completed.")
            )
            .onClick(() -> {
                this.director.setCurrentInstance(actionData);
                new UpdateScreen(List.of("actionconditions"), director).execute();
            });

        // delete action button
        new GUISlot(gui, 9)
            .setItem(Material.RED_DYE)
            .setLabel("Delete action")
            .onClick(() -> {
                Optional<String> issueMessage = this.action.delete(); // remove the action

                if (issueMessage.isEmpty()) {
                    // success
                    new UpdateScreen(List.of(this.previousScreen), director).execute(); // show deleted, by going to previous screen
                    return;
                }

                // when not success, share the issue with the player
                ChatUtils.message(issueMessage.get())
                    .style(MessageStyle.PRETTY)
                    .type(MessageType.WARN)
                    .player(this.director.getPlayer())
                    .send();
            });
    }
}
