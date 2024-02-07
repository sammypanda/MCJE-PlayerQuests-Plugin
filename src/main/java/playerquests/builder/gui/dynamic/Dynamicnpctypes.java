package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // list type of array
import java.util.Arrays; // generic type of array

import playerquests.builder.gui.component.GUIFrame; // outer frame of the GUI
import playerquests.builder.gui.component.GUISlot; // buttons of the GUI
import playerquests.builder.gui.function.SelectBlock; // function to get the block
import playerquests.builder.quest.component.QuestNPC; // object for the npc
import playerquests.client.ClientDirector; // controls the plugin

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

        GUISlot blockOption = new GUISlot(gui, 1);
        blockOption.setLabel("A Block");
        blockOption.setItem("GRASS_BLOCK");
        blockOption.addFunction(
            new SelectBlock(
                new ArrayList<>(Arrays.asList(
                    "Bok xD", // the prompt message
                    Arrays.asList( // blacklisted blocks:
                        "BARRIER"
                    )
                )), 
                director, 
                blockOption
            ).onFinish((function) -> {
                // function.getResult();
                this.npc.assign("block", "COAL");
            })
        );
    }
    
}
