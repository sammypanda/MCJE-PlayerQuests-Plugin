package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic array type
import java.util.Optional; // for handling nullables

import playerquests.builder.gui.component.GUIFrame; // the style of the outer GUI frame
import playerquests.builder.gui.component.GUISlot; // object for GUI slots
import playerquests.builder.gui.function.ChatPrompt; // prompting user input
import playerquests.builder.gui.function.UpdateScreen; // function to change the GUI screen
import playerquests.builder.quest.QuestBuilder; // the quest itself
import playerquests.builder.quest.component.QuestNPC; // object for quest NPCs
import playerquests.client.ClientDirector; // for controlling the plugin

/**
 * Creates a dynamic GUI for editing a quest NPC.
 */
public class Dynamicquestnpc extends GUIDynamic {

    /**
     * The quest NPC we are editing.
     */
    private QuestNPC npc;

    /**
     * The quest the NPC belongs to.
     */
    private QuestBuilder quest;

    /**
     * Creates a dynamic GUI for editing a quest NPC.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquestnpc(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // get the current quest npc for editing
        this.npc = (QuestNPC) this.director.getCurrentInstance(QuestNPC.class);

        // get the current quest
        this.quest = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
    }

    @Override
    protected void execute_custom() {
        this.generatePage();
    }

    private void generatePage() {
        GUIFrame guiFrame = this.gui.getFrame();

        // set frame options
        Optional.ofNullable(this.npc.getName()).ifPresentOrElse( // compact if-else statement for nullable
            npcName -> guiFrame.setTitle("NPC Editor ("+npcName+")"), // if present: set with npc name in title
            () -> guiFrame.setTitle("NPC Editor") // if no name present: just the gui title
        );

        // add back button
        GUISlot backButton = new GUISlot(this.gui, 1);
        backButton.setItem("OAK_DOOR");
        backButton.setLabel("Back");
        backButton.addFunction(
            new UpdateScreen(
                new ArrayList<>(Arrays.asList(this.previousScreen)), 
                director, 
                backButton
            )
        );

        // add 'change NPC name' button
        GUISlot nameButton = new GUISlot(this.gui, 3);
        String label = this.npc.getName() == null ? "Set NPC Name" : "Change NPC Name (" + this.npc.getName() + ")";
        nameButton.setItem("NAME_TAG");
        nameButton.setLabel(label);
        nameButton.addFunction(
            new ChatPrompt(
                new ArrayList<>(Arrays.asList("Set the name for this NPC", "npc.name")), 
                director, 
                nameButton
            ).onFinish((function) -> {
                this.execute();
            })
        );

        // add 'assign NPC to' button
        GUISlot assignButton = new GUISlot(this.gui, 4);
        assignButton.setLabel("Assign NPC to...");
        assignButton.setItem("RED_STAINED_GLASS");
        // TODO: change label and icon based on what is currently assigned to
        assignButton.addFunction(
            new UpdateScreen(
                new ArrayList<>(Arrays.asList("npctypes")), 
                director, 
                assignButton
            )
        );

        // add save button
        GUISlot saveButton = new GUISlot(this.gui, 9);
        saveButton.setItem("GREEN_DYE");
        saveButton.setLabel("Save NPC");
        saveButton.onClick(() -> {
            Boolean success = npc.save(this.quest, this.npc);

            if (success) { // if the npc was successfully saved..
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(this.previousScreen)), 
                    director, 
                    saveButton
                ).execute();
            }
        });

        // add divider slots
        GUISlot backDivider = new GUISlot(this.gui, 2);
        GUISlot saveDivider = new GUISlot(this.gui, 8);
        backDivider.setItem("BLACK_STAINED_GLASS_PANE");
        saveDivider.setItem("BLACK_STAINED_GLASS_PANE");
    }
    
}
