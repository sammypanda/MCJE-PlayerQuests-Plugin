package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.QuestBuilder;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.ClientDirector;

/**
 * A dynamic GUI screen for selecting NPCs within a quest builder.
 * <p>
 * This screen allows users to select from a list of quest NPCs. The NPCs are displayed as slots in the GUI,
 * and users can select an NPC to perform actions related to that NPC.
 * Users can also navigate back to the previous screen.
 * </p>
 */
public class Dynamicselectnpc extends GUIDynamic {

    /**
     * The quest builder associated with this screen.
     */
    private QuestBuilder quest;

    /**
     * The list of NPCs available for selection.
     */
    private List<QuestNPC> npcList;

    /**
     * The currently selected NPC.
     */
    private QuestNPC selectedNPC;

    /**
     * The code to run when an NPC is selected.
     */
    private Consumer<QuestNPC> onSelect;

    /**
     * Constructs a new {@code Dynamicselectnpc} instance.
     * @param director the client director that manages GUI interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicselectnpc(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.quest = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);

        this.npcList = new ArrayList<>(this.quest.getQuestNPCs().values());
    }

    @Override
    protected void execute_custom() {
        // set GUI size according to amount of NPCs
        gui.getFrame().setSize(
            Math.clamp(
                (((this.npcList.size() + 8) / 9) * 9),
            9, 54)
        );

        // set GUI title
        gui.getFrame().setTitle("NPC Selector");

        // populate list of quest NPCs
        // (shifted by 1 to appear start in the second slot)
        IntStream.range(1, this.npcList.size() + 1).forEach(index -> {
            QuestNPC npc = this.npcList.get(index - 1);
            Integer slot = index + 1;

            new GUISlot(gui, slot)
                .setLabel(npc.getName())
                .setItem(Material.VILLAGER_SPAWN_EGG)
                .onClick(() -> {
                    this.select(npc);
                });
        });

        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem(Material.OAK_DOOR)
            .addFunction(
                new UpdateScreen(
                    Arrays.asList(this.previousScreen),
                    director
                )
            );
    }

    /**
     * Sets the code to run when an NPC is selected.
     * <p>
     * This method allows for custom handling of the selected NPC.
     * </p>
     * @param onSelect a {@link Consumer} that processes the selected NPC.
     * @return the currently selected NPC.
     */
    public QuestNPC onSelect(Consumer<QuestNPC> onSelect) {
        this.onSelect = onSelect;
        return this.selectedNPC;
    }

    /**
     * Called when an NPC is selected.
     * <p>
     * This method sets the selected NPC and executes any provided selection logic.
     * </p>
     * @param npc the selected NPC.
     */
    private void select(QuestNPC npc) {
        this.selectedNPC = npc;

        if (this.onSelect != null) {
            onSelect.accept(npc);
        }

        new UpdateScreen(
            Arrays.asList(this.previousScreen),
            director
        ).execute();
    }
}
