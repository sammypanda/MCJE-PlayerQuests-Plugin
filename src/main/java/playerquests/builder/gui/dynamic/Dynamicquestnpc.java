package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list array type
import java.util.Arrays; // generic array type
import java.util.Optional; // for handling nullables

import playerquests.builder.gui.component.GUIFrame; // the style of the outer GUI frame
import playerquests.builder.gui.component.GUISlot; // object for GUI slots
import playerquests.builder.gui.function.ChatPrompt; // prompting user input
import playerquests.builder.gui.function.UpdateScreen; // function to change the GUI screen
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
        nameButton.setItem("NAME_TAG");
        nameButton.setLabel("Change NPC Name (" + this.npc.getName() + ")");
        nameButton.addFunction(
            new ChatPrompt(
                new ArrayList<>(Arrays.asList("Set the name for this NPC", "npc.name")), 
                director, 
                nameButton
            ).onFinish(() -> {
                this.execute();
            })
        );
    }
    
}
