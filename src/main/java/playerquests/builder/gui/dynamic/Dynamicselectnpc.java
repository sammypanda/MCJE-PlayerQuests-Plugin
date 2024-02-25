package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.ClientDirector;

public class Dynamicselectnpc extends GUIDynamic {

    private QuestBuilder quest;

    private List<QuestNPC> npcList;

    private QuestNPC selectedNPC;

    private Consumer<QuestNPC> onSelect;

    public Dynamicselectnpc(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.quest = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);

        this.npcList = new ArrayList<QuestNPC>(this.quest.getQuestNPCs().values());
    }

    @Override
    protected void execute_custom() {
        gui.getFrame().setSize(Math.max(9, Math.round(this.npcList.size() / 9) * 9));
        gui.getFrame().setTitle("NPC Selector");

        // populate list of quest NPCs
        // (shifted by 1 to appear start in the second slot)
        IntStream.range(1, this.npcList.size() + 1).forEach(index -> {
            QuestNPC npc = this.npcList.get(index - 1);
            Integer slot = index + 1;
            
            new GUISlot(gui, slot)
                .setLabel(npc.getName())
                .setItem("VILLAGER_SPAWN_EGG")
                .onClick(() -> {
                    this.select(npc);
                });
        });

        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .addFunction(
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(this.previousScreen)), 
                    director
                )
            );
    }

    /**
     * Code to run when an NPC is selected.
     * @param onSelect code operation
     * @return the NPC that was selected
     */
    public QuestNPC onSelect(Consumer<QuestNPC> onSelect) {
        this.onSelect = onSelect;
        return this.selectedNPC;
    }

    /**
     * Called when an NPC is selected.
     * @param npc the selected npc
     */
    private void select(QuestNPC npc) {
        this.selectedNPC = npc;
        
        if (this.onSelect != null) {
            onSelect.accept(npc);
        }

        new UpdateScreen(
            new ArrayList<>(Arrays.asList(this.previousScreen)), 
            director
        ).execute();
    }
}
