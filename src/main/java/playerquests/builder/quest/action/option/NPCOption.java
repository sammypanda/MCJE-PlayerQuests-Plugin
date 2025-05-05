package playerquests.builder.quest.action.option;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import playerquests.Core;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicselectnpc;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ActionData;
import playerquests.builder.quest.npc.QuestNPC;
import playerquests.client.ClientDirector;
import playerquests.product.Quest;

/**
 * The action option for defining an NPC.
 */
public class NPCOption extends ActionOption {

    /**
     * The NPC selected.
     */
    @JsonProperty("npc_id")
    String npcID;

    /**
     * Default constructor for Jackson.
     */
    public NPCOption() {}

    /**
     * Constructor including the QuestAction.
     * @param actionData the parent action
     */
    public NPCOption(ActionData actionData) {
        super(actionData);
    }

    @Override
    public GUISlot createSlot(GUIDynamic screen, GUIBuilder gui, Integer slot, ClientDirector director) {
        // get a map of NPCs in the quest (helps find the current NPC object from the NPC ID)
        Map<String, QuestNPC> NPCMap = this.getActionData().getAction().getStage().getQuest().getNPCs();

        return new GUISlot(gui, slot)
            .setLabel(this.npcID == null ? "Set the NPC" : String.format("Change the NPC (%s)", NPCMap.get(this.npcID).getName()))
            .setItem(Material.VILLAGER_SPAWN_EGG)
            .onClick(() -> {
                new UpdateScreen(List.of("selectnpc"), director)
                    .onFinish((f) -> {
                        UpdateScreen updateScreen = (UpdateScreen) f;
                        Dynamicselectnpc selectNPC = (Dynamicselectnpc) updateScreen.getDynamicGUI();

                        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
                            selectNPC.onSelect(npc -> {
                                this.setNPC(npc.getID());
                                this.actionData.setOption(this);
                            });

                            selectNPC.onFinish(_gui -> {
                                screen.refresh();
                            });
                        });
                    })
                    .execute();
            });
    }

    /**
    * Sets the NPC for this option.
    * @param id the ID of the quest npc
    */
    public void setNPC(String id) {
        this.npcID = id;
    }

    /**
     * Gets the NPC for this option.
     * @return the ID of the associated quest npc
     */
    @JsonIgnore
    public String getNPC() {
        return this.npcID;
    }

    /**
     * Gets the NPC object for this option.
     * @param quest the quest to search for the NPC in
     * @return the quest NPC object
     */
    public QuestNPC getNPC(Quest quest) {
        return quest.getNPCs().get(this.getNPC());
    }

    @Override
    public boolean isValid() {
        return this.getNPC() != null;
    }
}
