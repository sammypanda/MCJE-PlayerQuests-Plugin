package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData; // block representing this NPC
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.fasterxml.jackson.annotation.JsonIgnore; // to ignore serialising properties
import com.fasterxml.jackson.annotation.JsonProperty; // to set how a property serialises

import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicnpctypes;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.SelectLocation;
import playerquests.builder.gui.function.SelectMaterial;
import playerquests.builder.gui.function.data.SelectMethod;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.LocationData;
import playerquests.client.ClientDirector;
import playerquests.client.quest.QuestClient;
import playerquests.utility.MaterialUtils;

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
                Player player = director.getPlayer();
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

    @Override
    public void refund(QuestClient quester) {
        Player player = quester.getPlayer();
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

        // if player inventory is full, drop at feet
        Location playerLocation = player.getLocation();
        playerLocation.getWorld().dropItem(playerLocation, item);
    }

    @Override
    public void penalise(QuestClient quester) {
        Player player = quester.getPlayer();
        ItemStack item = new ItemStack(this.getBlock().getMaterial(), 1);
        PlayerInventory playerInventory = player.getInventory();

        // subtract the block
        playerInventory.removeItem(item);
    }

    @Override
    public void unregister(QuestAction action, QuestClient quester) {
        quester.getData().removeBlockNPC(action, this.getNPC());
    }

    @Override
    public void despawn(QuestAction action, QuestClient quester) {
        Player player = quester.getPlayer();

        // send block update to nothing
        player.sendBlockChange(
            this.getNPC().getLocation().toBukkitLocation(), 
            Material.AIR.createBlockData()
        );
    }

    @Override
    public void register(QuestAction action, QuestClient quester, Object value) {
        quester.getData().addBlockNPC(action, this.getNPC(), (BlockData) value);
    }

    @Override
    public Object spawn(QuestAction action, QuestClient quester) {
        Player player = quester.getPlayer();

        // set ghost block
        player.sendBlockChange(
            this.getNPC().getLocation().toBukkitLocation(), 
            this.getBlock()
        );

        return this.getBlock();
    }
}
