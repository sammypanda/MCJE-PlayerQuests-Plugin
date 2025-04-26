package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic type of array
import java.util.Objects;

import org.bukkit.block.data.BlockData; // managing the details of the NPC block
import org.bukkit.entity.HumanEntity; // the player type
import org.bukkit.inventory.ItemStack; // how items are identified and stored in inventories
import org.bukkit.inventory.PlayerInventory; // the player inventory type

import playerquests.builder.gui.component.GUIFrame; // outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // buttons of the GUI
import playerquests.builder.gui.function.SelectLocation; // function to get the player-chosen location
import playerquests.builder.gui.function.UpdateScreen; // function for changing the GUI screen
import playerquests.builder.quest.data.LocationData; // quest entity locations
import playerquests.builder.quest.npc.BlockNPC; // the block expression of an NPC
import playerquests.builder.quest.npc.NPCType;
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

        // add npc type buttons
        NPCType.getAllTypes()
            .stream()
            .map(typeClass -> {
                try {
                    // create QuestAction instance from class type
                    return typeClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(Objects::nonNull) // Filter out any nulls resulting from exceptions
            .forEach(type -> {
                type.createTypeSlot(this, director, gui, gui.getEmptySlot(), this.npc);
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

                this.refresh(); // re-draw to see changes
            }).execute();
        });
    }
}
