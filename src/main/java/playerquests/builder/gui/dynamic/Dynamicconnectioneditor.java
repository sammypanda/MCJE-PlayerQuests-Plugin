package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;

import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.builder.quest.data.ConnectionsData;
import playerquests.client.ClientDirector;

public class Dynamicconnectioneditor extends GUIDynamic {

    /**
     * The connections we are editing.
     */
    ConnectionsData connections;

    public Dynamicconnectioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        this.connections = (ConnectionsData) this.director.getCurrentInstance(ConnectionsData.class);
    }

    @Override
    protected void execute_custom() {
        String prev = this.connections.getPrev();
        String curr = this.connections.getCurr();
        String next = this.connections.getNext();

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

    UpdateScreen selectMenu(String connection) {
        return (UpdateScreen) new UpdateScreen(
            new ArrayList<>(Arrays.asList("selectconnection")), 
            director
        ).onFinish(function -> {
            UpdateScreen functionUpdateScreen = (UpdateScreen) function;
            Dynamicselectconnection connectionSelector = (Dynamicselectconnection) functionUpdateScreen.getDynamicGUI();

            connectionSelector.onSelect((selectionObject) -> {
                String selection = selectionObject.toString();

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
            });
        });
    }
    
}
