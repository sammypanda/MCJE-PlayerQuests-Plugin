package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list type of array
import java.util.Arrays; // generic type of array

import org.bukkit.Material; // identifying block of 'Block' NPC type
import org.bukkit.entity.HumanEntity; // the player type
import org.bukkit.inventory.ItemStack; // how items are identified and stored in inventories
import org.bukkit.inventory.PlayerInventory; // the player inventory type

import playerquests.builder.gui.component.GUIFrame; // outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // buttons of the GUI
import playerquests.builder.gui.function.SelectBlock; // function to get the player-chosen block
import playerquests.builder.gui.function.SelectLocation; // function to get the player-chosen location
import playerquests.builder.gui.function.UpdateScreen; // function for changing the GUI screen
import playerquests.builder.quest.type.Location; // playerquests location object
import playerquests.builder.quest.component.QuestNPC; // object for the npc
import playerquests.builder.quest.component.npc.type.BlockNPC; // NPCs as blocks
import playerquests.client.ClientDirector; // controls the plugin
import playerquests.utility.MaterialUtils; // helper used to get ItemStack from simplified input

public class Dynamicnpctypes extends GUIDynamic {

    /**
     * The NPC a type is being assigned to
     */
    private QuestNPC npc;

    /**
     * Creates a dynamic GUI with a list of npc types.
     * <ul>
     * <li>Block
     * </ul>
     * @param director director for the client
     * @param previousScreen the screen to go back to
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
                new ArrayList<>(Arrays.asList(this.previousScreen)), 
                director, 
                backButton
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
            new SelectBlock(
                new ArrayList<>(Arrays.asList(
                    "Select a block", // the prompt message
                    Arrays.asList( // denylisted blocks:
                        "BARRIER",
                        "DRAGON_EGG"
                    )
                )), 
                director, 
                blockOption
            ).onFinish((f) -> {
                // get the block that was selected
                SelectBlock function = (SelectBlock) f;
                Material block = function.getResult();

                // assign this block as the quest NPC
                if (block != null) {
                    this.npc.assign( // set this npc as:
                        new BlockNPC(block)
                    );
                }

                this.execute(); // re-draw to see changes
            }).execute();
        });

        // add place NPC button
        GUISlot placeButton = new GUISlot(gui, 9);

        placeButton.setLabel(
            String.format("%s", 
                (this.npc.getLocation() == null) ? 
                    ((this.npc.isAssigned()) ? 
                    "Place NPC (" + this.npc.getAssigned().toString() + ")" : 
                    "Cannot place before assigning") :
                "Relocate NPC (" + this.npc.getAssigned().toString() + ")"
            )
        );
        placeButton.setItem(
            String.format("%s",
                this.npc.isAssigned() ? this.npc.getMaterial().toString() : "BARRIER"  
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
                MaterialUtils.toItemStack(this.npc.getMaterial().toString())
            );

            new SelectLocation(
                new ArrayList<>(Arrays.asList(
                    "Place the NPC Block"
                )),
                director,
                placeButton
            ).onFinish((f) -> {
                // get the block that was selected
                SelectLocation function = (SelectLocation) f;
                Location location = function.getResult();

                if (location != null) {
                    this.npc.setLocation(location);
                    this.npc.place(); // place the block in the world
                }

                // return the players old inventory
                playerInventory.setContents(playerInventoryContents);

                this.execute(); // re-draw to see changes
            }).execute();
        });
    }
}
