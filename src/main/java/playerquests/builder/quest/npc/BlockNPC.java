package playerquests.builder.quest.npc;

import org.bukkit.Material; // the block material for this NPC

import com.fasterxml.jackson.annotation.JsonIgnore; // to ignore serialising properties
import com.fasterxml.jackson.annotation.JsonProperty; // to set how a property serialises

import playerquests.utility.singleton.PlayerQuests;

public class BlockNPC extends NPCType {

    public BlockNPC(String value, QuestNPC npc) {
        super(value, npc);
    }

    public BlockNPC(Material block, QuestNPC npc) {
        super(block.toString(), npc);
    }

    @Override
    @JsonProperty("type")
    public String toString() {
        return "Block";
    }

    /**
     * Get what block the NPC is assigned to.
     * @return the block material the NPC is.
     */
    @JsonProperty("value")
    public Material getBlock() {
        return Material.getMaterial(value);
    }

    /**
     * Get the rest of the details about this NPC.
     * @return the NPC object.
     */
    public QuestNPC getNPC() {
        return this.npc;
    }

    /**
     * Place the NPC block in the world.
     */
    @Override
    @JsonIgnore
    public void place() {
        PlayerQuests.getInstance().putBlockNPC(this);
    }

    /**
     * Remove the NPC block from the world.
     */
    @Override
    @JsonIgnore
    public void remove() {
        this.npc.setLocation(null);

        PlayerQuests.getInstance().putBlockNPC(this);
    }
}
