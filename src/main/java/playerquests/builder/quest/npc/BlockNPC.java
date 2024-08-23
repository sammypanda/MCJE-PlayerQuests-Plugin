package playerquests.builder.quest.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material; // deprecated: material representing the block, representing this NPC
import org.bukkit.block.data.BlockData; // block representing this NPC
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.fasterxml.jackson.annotation.JsonIgnore; // to ignore serialising properties
import com.fasterxml.jackson.annotation.JsonProperty; // to set how a property serialises

import playerquests.Core;
import playerquests.utility.singleton.PlayerQuests;

public class BlockNPC extends NPCType {

    /**
     * Defaut constructor (for Jackson)
    */
    public BlockNPC() {}

    public BlockNPC(String value, QuestNPC npc) {
        super(value, npc);
        this.type = "Block";
    }

    public BlockNPC(BlockData block, QuestNPC npc) {
        this(block.getAsString(true), npc);
    }

    /**
     * Get what block the NPC is assigned to.
     * @return the block material the NPC is.
     */
    @JsonIgnore
    public BlockData getBlock() {
        BlockData finalBlockData = Material.RED_WOOL.createBlockData(); // fallback block

        try {
            finalBlockData = Bukkit.getServer().createBlockData(value);
        } catch (IllegalArgumentException e) {
            System.err.println("malformed block data in a quest.");

            // try to get as a material (the old value type)
            Material fallbackMaterial = Material.getMaterial(value);
            if (fallbackMaterial != null) {
                finalBlockData = fallbackMaterial.createBlockData(); 
            }

            this.value = finalBlockData.getAsString(true);
        }

        return finalBlockData;
    }

    /**
     * Get the string of the BlockNPC's block data.
     * @return the block data as a string
     */
    @JsonProperty("value")
    public String getBlockString() {
        return this.value;
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
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            PlayerQuests.getInstance().putBlockNPC(this);
        });   
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

    @Override
    @JsonIgnore
    public void refund(Player player) {
        ItemStack item = new ItemStack(this.getBlock().getMaterial());
        PlayerInventory playerInventory = player.getInventory();

        // if player inventory is not full
        if (playerInventory.firstEmpty() != -1) {

            // return the NPC block to the player
            playerInventory.addItem(
                item
            );

            return; // don't continue
        }

        // if player inventory is full
        Location playerLocation = player.getLocation();
        playerLocation.getWorld().dropItem(playerLocation, item);
    }

    @Override
    @JsonIgnore
    public void penalise(Player player) {
        ItemStack item = new ItemStack(this.getBlock().getMaterial(), 1);
        PlayerInventory playerInventory = player.getInventory();

        // subtract the block
        playerInventory.removeItem(item);
    }
}
