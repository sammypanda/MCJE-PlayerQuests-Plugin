package playerquests.builder.quest.component.npc.type;

import org.bukkit.Material; // the block material for this NPC

public class BlockNPC extends NPCType {

    public BlockNPC(String value) {
        super(value);
    }

    public BlockNPC(Material block) {
        super(block.toString());
    }

    @Override
    public String toString() {
        return "Block";
    }

    /**
     * Get what block the NPC is assigned to.
     * @return the block material the NPC is.
     */
    public Material getBlock() {
        return Material.getMaterial(value);
    }
}
