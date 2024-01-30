package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.IntStream;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.component.QuestNPC;
import playerquests.client.ClientDirector; // for controlling the plugin

public class Dynamicquestnpcs extends GUIDynamic {

    /**
     * The parent builder for the current quest.
     */
    private QuestBuilder questBuilder;

    /** 
     * The list of quest NPCs to present.
    */
    private Map<String, QuestNPC> questNPCs;

    public Dynamicquestnpcs(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // set the quest builder instance
        this.questBuilder = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);

        // set the list of quest npcs
        this.questNPCs = this.questBuilder.getQuestNPCs(true);
    }

    @Override
    protected void execute_custom() {
        this.generatePages();
    }

    private void generatePages() {

        GUIFrame guiFrame = this.gui.getFrame();
        
        guiFrame.setTitle("Quest NPCs (" + this.questBuilder.getTitle() + ")");
        guiFrame.setSize(54);

        // the quest npc menus
        System.out.println(this.questNPCs.keySet());
        this.questNPCs.keySet().forEach(key -> {
            QuestNPC npc = this.questNPCs.get(key);
            Integer nextEmptySlot = this.gui.getEmptySlot();
            GUISlot npcSlot = new GUISlot(this.gui, nextEmptySlot);

            // Different visual representation for incomplete and complete NPCs
            if (key == "npc_-1") { // if invalid or incomplete npc
                npcSlot.setItem("SPAWNER");
                npcSlot.setLabel("<Unsaved NPC>");
            } else { // a valid npc
                npcSlot.setItem("VILLAGER_SPAWN_EGG");
                npcSlot.setLabel(npc.getTitle());
            }
        });

        // add npc button
        Integer nextEmptySlot = this.gui.getEmptySlot();
        GUISlot addButton = new GUISlot(this.gui, nextEmptySlot);
        addButton.setItem("LIME_DYE");
        addButton.setLabel("Add NPC");
        addButton.onClick(() -> {
            this.gui.clearSlots(); // clear to prevent duplicates
            QuestNPC npc = new QuestNPC();
            this.execute(); // re-run to see changes
        });
    }
    
}
