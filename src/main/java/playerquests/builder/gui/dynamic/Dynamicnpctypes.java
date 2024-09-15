package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic type of array

import org.bukkit.Material; // identifying block of 'Block' NPC type
import org.bukkit.block.data.BlockData; // managing the details of the NPC block
import org.bukkit.entity.HumanEntity; // the player type
import org.bukkit.inventory.ItemStack; // how items are identified and stored in inventories
import org.bukkit.inventory.PlayerInventory; // the player inventory type

import playerquests.builder.gui.component.GUIFrame; // outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // buttons of the GUI
import playerquests.builder.gui.function.SelectMaterial; // function to get the player-chosen block
import playerquests.builder.gui.function.SelectLocation; // function to get the player-chosen location
import playerquests.builder.gui.function.UpdateScreen; // function for changing the GUI screen
import playerquests.builder.gui.function.data.SelectMethod; // defining which methods to select something
import playerquests.builder.quest.data.LocationData; // quest entity locations
import playerquests.builder.quest.npc.BlockNPC; // the block expression of an NPC
import playerquests.builder.quest.npc.QuestNPC; // represents a quest NPC
import playerquests.client.ClientDirector; // controls the plugin
import playerquests.utility.MaterialUtils; // helper used to get ItemStack from simplified input

/**
 * A dynamic GUI screen for managing NPC types.
 * <p>
 * This screen allows users to assign a type to a quest NPC, including selecting a block type and placing the NPC at a location.
 * The screen provides options to go back, choose a block type for the NPC, and place or relocate the NPC.
 * </p>
 */
public class Dynamicnpctypes extends GUIDynamic {

    /**
     * The NPC a type is being assigned to
     */
    private QuestNPC npc;

    /**
     * Constructs a new {@code Dynamicnpctypes} instance.
     * @param director the client director that manages the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicnpctypes(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.npc = (QuestNPC) this.director.getCurrentInstance(QuestNPC.class);
    }

    @Override
    protected void execute_custom() {
        GUIFrame frame = this.gui.getFrame();
        
        frame.setTitle( // set the GUI title
            String.format( // ...dynamically
                "Assign %s to:",
                this.npc.getName() != null ? this.npc.getName() : "NPC" // put NPC name if available, otherwise "NPC"
            )
        );

        // add back button
        GUISlot backButton = new GUISlot(gui, 1);
        backButton.setLabel("Back");
        backButton.setItem("OAK_DOOR");
        backButton.addFunction(
            new UpdateScreen(
                Arrays.asList(this.previousScreen), 
                director
            )
        );

        // add dividers
        GUISlot backDivider = new GUISlot(gui, 2);
        GUISlot placeDivider = new GUISlot(gui, 8);
        backDivider.setItem("BLACK_STAINED_GLASS_PANE");
        placeDivider.setItem("BLACK_STAINED_GLASS_PANE");

        // add block type button
        GUISlot blockOption = new GUISlot(gui, 3);
        blockOption.setLabel("A Block");
        blockOption.setItem("GRASS_BLOCK");
        blockOption.onClick(() -> {
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
                    BlockNPC blockNPC = new BlockNPC(block.createBlockData(), this.npc); // create NPC type
                    
                    // set this npc type
                    this.npc.assign(
                        blockNPC
                    );
                }

                this.gui.getResult().display();
                this.execute();
            }).execute();
        });

        // add place NPC button
        GUISlot placeButton = new GUISlot(gui, 9);

        placeButton.setLabel(
            String.format("%s", 
                (this.npc.getLocation() == null) ? 
                    ((this.npc.isAssigned()) ? 
                    "Place NPC (" + this.npc.getAssigned().getType() + ")" : 
                    "Cannot place before assigning") :
                "Relocate NPC (" + this.npc.getAssigned().getType() + ")"
            )
        );
        placeButton.setItem(
            String.format("%s",
                this.npc.isAssigned() ? this.npc.getBlock().getMaterial().toString() : "BARRIER"  
            )
        );
        placeButton.onClick(() -> {
            HumanEntity player = this.director.getPlayer();
            PlayerInventory playerInventory = player.getInventory();
            ItemStack[] playerInventoryContents = playerInventory.getContents();
            
            // temporarily empty the player inventory
            playerInventory.clear();

            // give the player the block to place
            playerInventory.setItemInMainHand(
                MaterialUtils.toItemStack(this.npc.getBlock().getMaterial().toString())
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
                    this.npc.setLocation(location);
                }

                if (block != null) {
                    this.npc.assign(new BlockNPC(block, this.npc));
                }

                // return the players old inventory
                playerInventory.setContents(playerInventoryContents);

                this.execute(); // re-draw to see changes
            }).execute();
        });
    }
}
