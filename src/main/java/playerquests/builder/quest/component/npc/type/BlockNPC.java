package playerquests.builder.quest.component.npc.type;

import org.bukkit.Material; // the block material for this NPC
import org.bukkit.World; // world the NPC exists in

import com.fasterxml.jackson.annotation.JsonIgnore; // to ignore serialising properties
import com.fasterxml.jackson.annotation.JsonProperty; // to set how a property serialises

public class BlockNPC extends NPCType {

    public BlockNPC(String value) {
        super(value);
    }

    public BlockNPC(Material block) {
        super(block.toString());
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
     * Place the NPC block in the world.
     * @param world which world the NPC belongs in
     * @param x the x coordinate double
     * @param y the y coordinate double
     * @param z the z coordinate double
     */
    @Override
    @JsonIgnore
    public void place(World world, double x, double y, double z) {
        world.setBlockData(
            (int) x,
            (int) y,
            (int) z,
            this.getBlock().createBlockData()
        );
    }
}
