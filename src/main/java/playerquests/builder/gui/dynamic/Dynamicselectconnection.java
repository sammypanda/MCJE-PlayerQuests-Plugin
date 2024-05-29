package playerquests.builder.gui.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import playerquests.builder.gui.component.GUIFrame;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.function.UpdateScreen;
import playerquests.client.ClientDirector;

public class Dynamicselectconnection extends GUIDynamic {

    public Dynamicselectconnection(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
    }

    @Override
    protected void execute_custom() {
        GUIFrame frame = gui.getFrame();
        
        frame.setTitle("Select Connection");
        frame.setSize(27);

        IntStream.range(1, 19).forEach((int slot) -> {
            new GUISlot(gui, slot)
                .setLabel("[A Stage]")
                .setItem("CHEST");
        });

        new GUISlot(gui, 19)
            .setLabel("Back")
            .setItem("OAK_DOOR")
            .onClick(() -> {
                new UpdateScreen(
                    new ArrayList<>(Arrays.asList(previousScreen)),
                    director
                ).execute();
            });
    }
    
}
