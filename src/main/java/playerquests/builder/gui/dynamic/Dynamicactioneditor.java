package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.stream.Collectors; // summising a stream to a data type
import java.util.stream.IntStream; // functional loops

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.QuestBuilder; // used to edit a quest
import playerquests.builder.quest.action.QuestAction; // describes a quest action
import playerquests.builder.quest.data.ActionOption; // a setting that can be set for an action
import playerquests.builder.quest.npc.QuestNPC; // describes a quest NPC
import playerquests.builder.quest.stage.QuestStage; // describes a quest stage
import playerquests.client.ClientDirector; // controlling the plugin
import playerquests.utility.ChatUtils;

/**
 * Shows a dynamic GUI used for editing a quest action.
 */
// TODO: pagination for action options
// TODO: replacing entry point setter button with action name changer button
// TODO: conditional back and forward buttons in slots 10,11 for params list
// TODO: divider on the right side of the GUI for setting: as entry point, next, current and prev connections
public class Dynamicactioneditor extends GUIDynamic {

    /**
     * The current quest action.
     */
    QuestAction action;

    /**
     * The parent quest stage for this quest action.
     */
    QuestStage stage;

    /**
     * Creates a dynamic GUI to edit a quest stage action.
     * @param director director for the client
     * @param previousScreen the screen to go back to
     */
    public Dynamicactioneditor(ClientDirector director, String previousScreen) {
        super(director, previousScreen);
    }

    @Override
    protected void setUp_custom() {
        // set the quest action stage parent
        this.stage = (QuestStage) this.director.getCurrentInstance(QuestStage.class);

        // set the quest action to modify
        this.action = this.stage.getActions().get(this.stage.getActionToEdit());
    }

    @Override
    protected void execute_custom() {
        // set label
        if (this.stage.getEntryPoint().getID() == this.action.getID()) { // if this action is the entry point
            this.gui.getFrame().setTitle(this.action + " Editor (Entry Point)");
        } else {
            this.gui.getFrame().setTitle(this.action + " Editor");
        }

        // set the gui size
        this.gui.getFrame().setSize(18);

        // the back button
        GUISlot exitButton = new GUISlot(this.gui, 10);
        exitButton.setLabel("Back");
        exitButton.setItem("OAK_DOOR");
        exitButton.addFunction(new UpdateScreen( // set function as 'UpdateScreen'
            new ArrayList<>(Arrays.asList("queststage")), // set the previous screen 
            director // set the client director
        ));

        // the delete button
        new GUISlot(this.gui, 11)
            .setLabel("Delete Action")
            .setItem("RED_DYE")
            .onClick(() -> {
                if (stage.getEntryPoint().equals(action)) {
                    ChatUtils.sendError(director.getPlayer(), "Cannot remove the stage starting point action.");
                    return;
                }

                stage.removeAction(action);

                new UpdateScreen(
                    new ArrayList<>(Arrays.asList("queststage")), 
                    director
                ).execute();
            });

        // changing action type button
        GUISlot typeButton = new GUISlot(this.gui, 1);
        typeButton.setItem("FIREWORK_ROCKET");
        typeButton.setLabel("Change Type (" + this.action.toString() + ")");
        typeButton.addFunction(new UpdateScreen(
            new ArrayList<>(Arrays.asList("actiontypes")),
            director
        ));

        // setting current as stage entry point button
        GUISlot entrypointButton = new GUISlot(this.gui, 2);
        entrypointButton.setItem("ENDER_EYE");
        entrypointButton.setLabel("Set Action As Entry Point");
        entrypointButton.onClick(() -> {
            this.stage.setEntryPoint(this.action.getID()); // set this action as the stage entry point
            this.execute(); // re-run to see changes
        });

        IntStream.of(3, 12).forEach((int value) -> {
            GUISlot dividerSlot = new GUISlot(this.gui, value);
            dividerSlot.setItem("BLACK_STAINED_GLASS_PANE");
            dividerSlot.setLabel(" ");
        });

        // dynamically fill in option slots
        this.putOptionSlots(Arrays.asList(1,2,3,10,11,12));
    }

    /**
     * Create GUI slots that are options for this action.
     * @param gui the GUI to create the slots in
     * @param deniedSlots a list of slots that cannot have the option buttons set on
     */
    private void putOptionSlots(List<Integer> deniedSlots) {
        // error if trying to access this class directly instead of by an extended member
        if (this.action.getClass().getSimpleName().equals("ActionType")) {
            throw new IllegalStateException("Tried to build option slots without defining the type of action.");
        }

        List<ActionOption> options = this.action.getActionOptions();
        List<Integer> allowedSlots = IntStream.range(1, gui.getFrame().getSize())
                                            .filter(slot -> !deniedSlots.contains(slot))
                                            .boxed()
                                            .toList();

        Map<Integer, ActionOption> resultMap = IntStream.range(0, Math.min(options.size(), allowedSlots.size()))
            .boxed()
            .collect(Collectors.toMap(i -> allowedSlots.get(i), i -> options.get(i)));
        
        // fill in the GUI slots
        resultMap.forEach((slot, option) -> {
            this.putOptionSlot(slot, option);
        });
    }

    private void putOptionSlot(Integer slot, ActionOption option) {
        QuestBuilder quest = (QuestBuilder) this.director.getCurrentInstance(QuestBuilder.class);
        QuestNPC currentNPC = this.action.getNPC();

        GUISlot optionSlot = new GUISlot(gui, slot)
                                .setLabel(option.getLabel())
                                .setItem(option.getItem());

        switch (option) {
            case NPC:
                if (currentNPC != null) {
                    optionSlot.setLabel(
                        String.format("%s (%s)", option.getLabel(), currentNPC.getName())
                    );
                }

                optionSlot.onClick(() -> {
                    new UpdateScreen(
                        new ArrayList<>(Arrays.asList("selectnpc")), 
                        director
                    ).onFinish(function -> {
                        UpdateScreen functionUpdateScreen = (UpdateScreen) function;
                        Dynamicselectnpc NPCSelector = (Dynamicselectnpc) functionUpdateScreen.getDynamicGUI();

                        NPCSelector.onSelect((selectedNPC) -> {
                            this.action.setNPC(selectedNPC);
                        });
                    }).execute();;
                });
                break;
            case DIALOGUE:
                optionSlot.onClick(() -> {
                    this.director.getPlayer().sendMessage("[PlayerQuests] WIP: setting demo dialogue");
                    this.action.setDialogue(Arrays.asList("demo", "dialogue"));
                });
        }
    }
}

