package playerquests.builder.gui.dynamic;

import java.util.Arrays; // generic type of array
import java.util.Objects;

import org.bukkit.Material;

import playerquests.builder.gui.component.GUIFrame; // outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // buttons of the GUI
import playerquests.builder.gui.function.UpdateScreen; // function for changing the GUI screen
import playerquests.builder.quest.npc.NPCType;
import playerquests.builder.quest.npc.QuestNPC; // represents a quest NPC
import playerquests.client.ClientDirector; // controls the plugin

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
    protected void setupCustom() {
        this.npc = (QuestNPC) this.director.getCurrentInstance(QuestNPC.class);
    }

    @Override
    protected void executeCustom() {
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
        backButton.setItem(Material.OAK_DOOR);
        backButton.addFunction(
            new UpdateScreen(
                Arrays.asList(this.previousScreen), 
                director
            )
        );

        // add dividers
        GUISlot backDivider = new GUISlot(gui, 2);
        GUISlot placeDivider = new GUISlot(gui, 8);
        backDivider.setItem(Material.BLACK_STAINED_GLASS_PANE);
        placeDivider.setItem(Material.BLACK_STAINED_GLASS_PANE);

        // add npc type buttons
        NPCType.getAllTypes()
            .stream()
            .map(typeClass -> {
                try {
                    // create QuestAction<?,?> instance from class type
                    return typeClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(Objects::nonNull) // Filter out any nulls resulting from exceptions
            .forEach(type -> {
                type.createTypeSlot(this, director, gui, gui.getEmptySlot(), this.npc); // type selector
            });

        // add place button
        if ( ! npc.isAssigned()) {
            new GUISlot(gui, 9)
                .setLabel("Cannot place before assigning")
                .setItem(Material.BARRIER);
        } else {
            npc.getAssigned().createPlaceSlot(this, director, gui, 9, npc);
        }
    }
}
