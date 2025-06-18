package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic array type
import java.util.Optional; // for handling nullables

import org.bukkit.Material;

import playerquests.builder.gui.component.GUIFrame; // the style of the outer GUI frame
import playerquests.builder.gui.component.GUISlot; // object for GUI slots
import playerquests.builder.gui.function.ChatPrompt; // prompting user input
import playerquests.builder.gui.function.UpdateScreen; // function to change the GUI screen
import playerquests.builder.quest.QuestBuilder; // the quest itself
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.utility.singleton.QuestRegistry;

/**
 * Creates a dynamic GUI for editing a quest NPC.
 */
public class Dynamicnpceditor extends GUIDynamic {

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
    public Dynamicnpceditor(ClientDirector director, String previousScreen) {
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
        backButton.setItem(Material.OAK_DOOR);
        backButton.setLabel("Back");
        backButton.addFunction(
            new UpdateScreen(
                Arrays.asList(this.previousScreen), 
                director
            )
        );

        // add 'change NPC name' button
        GUISlot nameButton = new GUISlot(this.gui, 3);
        String label = this.npc.getName() == null ? "Set NPC Name" : "Change NPC Name (" + this.npc.getName() + ")";
        nameButton.setItem(Material.NAME_TAG);
        nameButton.setLabel(label);
        nameButton.addFunction(
            new ChatPrompt(
                Arrays.asList("Set the name for this NPC", "npc.name"), 
                director
            ).onFinish((_f) -> {
                this.execute();
            })
        );

        // add 'assign NPC to' button
        GUISlot assignButton = new GUISlot(this.gui, 4);
        assignButton.setLabel( // set the GUI title
            String.format( // ...dynamically
                "Assign %s to...",
                this.npc.getName() != null ? this.npc.getName() : "NPC" // put NPC name if available, otherwise "NPC"
            )
        );
        assignButton.setItem(this.npc.getBlock().getMaterial());
        assignButton.addFunction(
            new UpdateScreen(
                Arrays.asList("npctypes"), 
                director
            )
        );

        // add delete button
        GUISlot deleteButton = new GUISlot(this.gui, 8);
        deleteButton.setItem(Material.RED_DYE);
        deleteButton.setLabel("Delete NPC");
        deleteButton.onClick(() -> {
            quest.removeNPC(npc);
            quest.build().save();

            new UpdateScreen(
                Arrays.asList(this.previousScreen), 
                director
            ).execute();
        });

        // add save button
        GUISlot saveButton = new GUISlot(this.gui, 9);
        saveButton.setItem(Material.GREEN_DYE);
        saveButton.setLabel("Save NPC");
        saveButton.onClick(() -> {
            if (npc.getQuest() == null) {
                this.quest.addNPC(npc);
            }

            Boolean success = npc.save(this.quest, this.npc);

            if (success) { // if the npc was successfully saved..
                // update the quest
                QuestRegistry.getInstance().submit(this.npc.getQuest());

                // go back to previous screen
                new UpdateScreen(
                    Arrays.asList(this.previousScreen), 
                    director
                ).execute();
            }
        });

        // add divider slots
        GUISlot backDivider = new GUISlot(this.gui, 2);
        GUISlot saveDivider = new GUISlot(this.gui, 7);
        backDivider.setItem(Material.BLACK_STAINED_GLASS_PANE);
        saveDivider.setItem(Material.BLACK_STAINED_GLASS_PANE);
    }
}
