package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic array type
import java.util.List; // generic list type
import java.util.Map; // generic map type

import org.bukkit.Material;

import playerquests.builder.gui.component.GUIFrame; // the outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // object for GUI slots
import playerquests.builder.gui.function.UpdateScreen; // used to change the GUI screen
import playerquests.builder.quest.QuestBuilder; // object for quest composition
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.ClientDirector; // for controlling the plugin
import playerquests.utility.ChatUtils;

/**
 * Shows a dynamic GUI list of the current quest NPCs.
 */
public class Dynamicquestnpcs extends GUIDynamic {

    /**
     * The parent builder for the current quest.
     */
    private QuestBuilder questBuilder;

    /** 
     * The list of quest NPCs to present.
    */
    private Map<String, QuestNPC> questNPCs;

    /**
     * Creates a dynamic GUI listing the quest NPCs.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicquestnpcs(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setupCustom() {
        // set the quest builder instance
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);

        // set the list of quest npcs
        this.questNPCs = this.questBuilder.getQuestNPCs(true);
    }

    @Override
    protected void executeCustom() {
        this.generatePages();
    }

    private void generatePages() {
        // get the gui frame to edit
        GUIFrame guiFrame = this.gui.getFrame();

        // sort npc keys (ids) into a descending list (newest first)
        List<String> keys = questNPCs.keySet().stream()
        .sorted((key1, key2) -> {
            int intValue1 = Integer.parseInt(key1.split("_")[1]);
            int intValue2 = Integer.parseInt(key2.split("_")[1]);
            return Integer.compare(intValue2, intValue1); // compare in descending order
        })
        .toList();
        
        // set frame options
        guiFrame.setTitle("Quest NPCs (" + ChatUtils.shortenString(this.questBuilder.getTitle(), 18) + ")");
        guiFrame.setSize(54);

        // add the quest npcs
        keys.subList(0, Math.min(keys.size(), 36)).forEach(key -> { // only allow up to 36 NPC slots
            QuestNPC npc = this.questNPCs.get(key);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot npcSlot = new GUISlot(this.gui, nextEmptySlot);

            // different visual representation for incomplete and complete NPCs
            if (key.equals("npc_-1")) { // if invalid or incomplete npc
                npcSlot.setItem(Material.SPAWNER);
                npcSlot.setLabel("<Unsaved NPC>");
            } else { // a valid npc
                npcSlot.setItem(Material.VILLAGER_SPAWN_EGG);
                npcSlot.setLabel(npc.getTitle());
            }

            npcSlot.onClick(() -> {
                // set the current npc we are editing
                this.director.setCurrentInstance(npc);

                // declare swapping to 'questnpc' screen
                new UpdateScreen(Arrays.asList("npceditor"), director).execute();
            });
        });

        // add new npc button
        Integer nextEmptySlot = this.gui.getEmptySlot();
        GUISlot addButton = new GUISlot(this.gui, nextEmptySlot);
        addButton.setItem(Material.LIME_DYE);
        addButton.setLabel("Add NPC");
        addButton.onClick(() -> {
            QuestNPC npc = new QuestNPC(); // create new empty npc
            npc.setQuest(questBuilder.build());
            this.director.setCurrentInstance(npc);

            new UpdateScreen(
                Arrays.asList("npceditor"), 
                director
            ).execute();
        });

        // add back button
        GUISlot backButton = new GUISlot(this.gui, 46);
        backButton.setLabel("Back");
        backButton.setItem(Material.OAK_DOOR);
        backButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            Arrays.asList(this.previousScreen), // set the previous screen 
            director // set the client director
        ));
    }
}
