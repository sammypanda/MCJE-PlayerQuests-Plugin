package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.builder.quest.data.StagePath;
import playerquests.client.ClientDirector;

/**
 * A dynamic GUI screen for editing connections between stages in a quest.
 * <p>
 * This screen allows users to set or change the previous, current, and next stages
 * in a sequence. It provides buttons for each stage and allows for navigation 
 * back to the previous screen.
 * </p>
 */
public class Dynamicconnectioneditor extends GUIDynamic {

    /**
     * The connections data currently being edited.
     */
    ConnectionsData connections;

    /**
     * Constructs a new {@code Dynamicconnectioneditor} instance.
     * @param director the client director that handles the GUI and interactions.
     * @param previousScreen the identifier of the previous screen to navigate back to.
     */
    public Dynamicconnectioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.connections = (ConnectionsData) this.director.getCurrentInstance(ConnectionsData.class);
    }

    @Override
    protected void execute_custom() {
        StagePath prev = this.connections.getPrev();
        StagePath curr = this.connections.getCurr();
        StagePath next = this.connections.getNext();

        this.gui.getFrame().setTitle("Sequence Editor");

        new GUISlot(gui, 1)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .onClick(() -> {
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(previousScreen)),
                    director
                ).execute();
            });

        new GUISlot(gui, 2)
            .setItem("PISTON")
            .setLabel(
                String.format("%s %s", 
                    prev != null ? "Change Previous Step" : "Set Previous Step",
                    prev != null ? "("+prev+")" : ""
                )
            )
            .onClick(() -> {
                this.selectMenu("prev").execute();
            });

        new GUISlot(gui, 3)
            .setItem("PISTON")
            .setLabel(
                String.format("%s %s", 
                    curr != null ? "Change Current Step" : "Set Current Step",
                    curr != null ? "("+curr+")" : ""
                )
            )
            .onClick(() -> {
                this.selectMenu("curr").execute();
            });

        new GUISlot(gui, 4)
            .setItem("PISTON")
            .setLabel(
                String.format("%s %s", 
                    next != null ? "Change Next Step" : "Set Next Step",
                    next != null ? "("+next+")" : ""
                )
            )
            .onClick(() -> {
                this.selectMenu("next").execute();
            });
    }

    /**
     * Creates an {@code UpdateScreen} instance for selecting a stage path.
     * <p>
     * This method sets up the selection menu for the specified connection type
     * (previous, current, or next). Once a selection is made, the corresponding
     * connection is updated, and the user is returned to the origin screen.
     * </p>
     * @param connection the type of connection to update ("prev", "curr", or "next").
     * @return the {@code UpdateScreen} instance for stage selection.
     */
    UpdateScreen selectMenu(String connection) {
        return (UpdateScreen) new UpdateScreen(
            new ArrayList<>(Arrays.asList("selectconnection")), 
            director
        ).onFinish(function -> {
            UpdateScreen functionUpdateScreen = (UpdateScreen) function;
            Dynamicselectconnection connectionSelector = (Dynamicselectconnection) functionUpdateScreen.getDynamicGUI();

            connectionSelector.onSelect((selectionObject) -> {
                StagePath selection = (StagePath) selectionObject;

                switch (connection) {
                    case "prev":
                        this.connections.setPrev(selection);
                        break;
                    case "curr":
                        this.connections.setCurr(selection);
                        break;
                    case "next":
                        this.connections.setNext(selection);
                        break;
                }

                // go back to origin screen when done selecting
                // (user DAMSTACEY UI recommendation)
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(this.previousScreen)), director
                ).execute();
            });
        });
    }
}
