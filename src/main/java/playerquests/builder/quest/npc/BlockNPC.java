package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData; // block representing this NPC
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.fasterxml.jackson.annotation.JsonIgnore; // to ignore serialising properties
import com.fasterxml.jackson.annotation.JsonProperty; // to set how a property serialises

import playerquests.Core;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicnpctypes;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.SelectLocation;
import playerquests.builder.gui.function.SelectMaterial;
import playerquests.builder.gui.function.data.SelectMethod;
import playerquests.builder.quest.data.LocationData;
import playerquests.client.ClientDirector;
import playerquests.utility.MaterialUtils;
import playerquests.utility.listener.BlockListener;
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
     * Places the NPC block in the world.
     * @param player the player who can see the placement
     */
    public void place(Player player) {
        Bukkit.getScheduler().runTask(Core.getPlugin(), () -> {
            // get the listener that detects interactions with the block npc
            BlockListener blockListener = PlayerQuests.getBlockListener();

            // if no location has been set, don't try to register
            if (this.getNPC() == null || this.getNPC().getLocation() == null) {
                blockListener.unregisterBlockNPC(this, player);
                return;
            }

            // register the block
            blockListener.registerBlockNPC(this, player);
        });   
    }

    /**
     * Removes the NPC block from the world and unregisters it from the PlayerQuests instance.
     */
    public void remove() {
        PlayerQuests.getBlockListener().remove(this.npc.getQuest(), this);
    }

    /**
     * Removes the NPC block from the world and unregisters it from the PlayerQuests instance.
     */
    public void remove(Player player) {
        PlayerQuests.getBlockListener().remove(this.npc.getQuest(), this, player);
    }

    /**
     * Refunds the block item to the player's inventory. If the inventory is full, drops the item at the player's location.
     * 
     * @param player the player to refund the item to
     */
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
    public void penalise(Player player) {
        ItemStack item = new ItemStack(this.getBlock().getMaterial(), 1);
        PlayerInventory playerInventory = player.getInventory();

        // subtract the block
        playerInventory.removeItem(item);
    }

    @Override
    public GUISlot createTypeSlot(GUIDynamic screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc) {
        return new GUISlot(gui, slot)
            .setLabel("A Block")
            .setItem("GRASS_BLOCK")
            .onClick(() -> {
            new SelectMaterial(
                Arrays.asList(
                    "Select a block from your inventory", // the prompt message
                    Arrays.asList( // denylisted blocks:
                        "BARRIER",
                        "DRAGON_EGG"
                    ),
                    Arrays.asList( // denied select methods:
                        SelectMethod.HIT,
                        SelectMethod.CHAT
                    ),
                    true // has to be a block
                ), 
                director
            ).onFinish((f) -> {
                // get the block that was selected
                SelectMaterial function = (SelectMaterial) f;
                Material block = function.getResult();

                // assign this block as the quest NPC
                if (block != null) {
                    BlockNPC blockNPC = new BlockNPC(block.createBlockData(), npc); // create NPC type
                    
                    // set this npc type
                    npc.assign(
                        blockNPC
                    );
                }

                gui.getResult().display();
                screen.refresh();
            }).execute();
        });
    }

    @Override
    public GUISlot createPlaceSlot(Dynamicnpctypes screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc) {
        LocationData locationData = npc.getLocation();

        return new GUISlot(gui, slot)
            .setLabel(
                String.format("%s", 
                    (npc.getLocation() == null) ? 
                        "Place NPC (" + npc.getAssigned().getType() + ")" :
                        "Relocate NPC (" + npc.getAssigned().getType() + ")"
                )
            )
            .setDescription(
                locationData != null ?
                    List.of( // %.0f = representing floats with 0 decimal point places
                        String.format("X: %.0f", locationData.getX()),
                        String.format("Y: %.0f", locationData.getY()),
                        String.format("Z: %.0f", locationData.getZ())
                    ) :
                List.of()
            )
            .setItem(
                String.format("%s",
                    npc.isAssigned() ? npc.getBlock().getMaterial().toString() : "BARRIER"  
                )
            )
            .onClick(() -> {
                HumanEntity player = director.getPlayer();
                PlayerInventory playerInventory = player.getInventory();
                ItemStack[] playerInventoryContents = playerInventory.getContents();
                
                // temporarily empty the player inventory
                playerInventory.clear();

                // give the player the block to place
                playerInventory.setItemInMainHand(
                    MaterialUtils.toItemStack(npc.getBlock().getMaterial().toString())
                );

                new SelectLocation(
                    Arrays.asList(
                        "Place the NPC Block"
                    ),
                    director
                ).onFinish((f) -> {
                    // get the block that was selected
                    SelectLocation function = (SelectLocation) f;
                    LocationData location = function.getResult();
                    BlockData block = function.getBlockData();

                    if (location != null) {
                        npc.setLocation(location);
                    }

                    if (block != null) {
                        npc.assign(new BlockNPC(block, npc));
                    }

                    // return the players old inventory
                    playerInventory.setContents(playerInventoryContents);

                    screen.refresh(); // re-draw to see changes
                }).execute();
            });
    }
}
