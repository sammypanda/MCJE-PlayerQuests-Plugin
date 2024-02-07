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
}
