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

/**
 * Represents an NPC that is associated with a specific block in the world.
 * 
 * This class manages the block data of the NPC and provides methods to place, remove, refund, and penalize the NPC.
 */
public class BlockNPC extends NPCType {

    /**
     * Defaut constructor (for Jackson)
    */
    public BlockNPC() {}

    /**
     * Constructs a BlockNPC with a specified block data and associated quest NPC.
     * 
     * @param value the block data string
     * @param npc the associated QuestNPC
     */
    public BlockNPC(String value, QuestNPC npc) {
        super(value, npc);
        this.type = "Block";
    }

    /**
     * Constructs a BlockNPC using a BlockData object.
     * 
     * @param block the block data
     * @param npc the associated QuestNPC
     */
    public BlockNPC(BlockData block, QuestNPC npc) {
        this(block.getAsString(true), npc);
    }

    /**
     * Gets the block data representing this NPC.
     * 
     * @return the block data of the NPC
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
     * Gets the string representation of the block data for this NPC.
     * 
     * @return the block data as a string
     */
    @JsonProperty("value")
    public String getBlockString() {
        return this.value;
    }

    /**
     * Gets the QuestNPC associated with this BlockNPC.
     * 
     * @return the QuestNPC object
     */
    public QuestNPC getNPC() {
        return this.npc;
    }

    /**
     * Places the NPC block in the world by registering it with the PlayerQuests instance.
     */
    @Override
    @JsonIgnore
    public void place() {
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            PlayerQuests.getInstance().putBlockNPC(this);
        });   
    }

    /**
     * Removes the NPC block from the world and unregisters it from the PlayerQuests instance.
     */
    @Override
    @JsonIgnore
    public void remove() {
        this.npc.setLocation(null);

        PlayerQuests.getInstance().putBlockNPC(this);
    }

    /**
     * Refunds the block item to the player's inventory. If the inventory is full, drops the item at the player's location.
     * 
     * @param player the player to refund the item to
     */
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

    /**
     * Penalizes the player by removing the block item from their inventory.
     * 
     * @param player the player to penalize
     */
    @Override
    @JsonIgnore
    public void penalise(Player player) {
        ItemStack item = new ItemStack(this.getBlock().getMaterial(), 1);
        PlayerInventory playerInventory = player.getInventory();

        // subtract the block
        playerInventory.removeItem(item);
    }
}
