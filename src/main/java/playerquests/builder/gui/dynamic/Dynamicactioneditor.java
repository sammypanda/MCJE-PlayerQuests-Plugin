package playerquests.builder.gui.dynamic;

import java.util.ArrayList; // array type of list
import java.util.Arrays; // generic array handling
import java.util.List; // generic list type
import java.util.Map; // generic map type
import java.util.Optional; // for a value that may be null
import java.util.stream.Collectors; // summising a stream to a data type
import java.util.stream.IntStream; // functional loops

import org.bukkit.inventory.ItemStack;

import playerquests.builder.gui.component.GUISlot; // modifying gui slots
import playerquests.builder.gui.function.ChatPrompt; // prompts the user for input
import playerquests.builder.gui.function.UpdateScreen; // going to previous screen
import playerquests.builder.quest.action.QuestAction; // describes a quest action
import playerquests.builder.quest.data.ActionOption; // a setting that can be set for an action
import playerquests.builder.quest.data.StagePath;
import playerquests.builder.quest.npc.QuestNPC; // describes a quest NPC
import playerquests.builder.quest.stage.QuestStage; // describes a quest stage
import playerquests.client.ClientDirector; // controlling the plugin
import playerquests.utility.ChatUtils; // working with in-game chat
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.singleton.QuestRegistry;

/**
 * Shows a dynamic GUI used for editing a quest action.
 */
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
        // dynamically fill in option slots
        this.putOptionSlots(Arrays.asList(1,2,3,10,11,12));

        // set label
        if (this.stage.getEntryPoint().getAction() == this.action.getID()) { // if this action is the entry point
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
        exitButton.onClick(() -> {
            Optional<String> validity = action.validate(); // check if action is valid

            // exit and send error if action is invalid
            if (!validity.isEmpty()) {
                ChatUtils.message(validity.get())
                    .player(this.director.getPlayer())
                    .type(MessageType.WARN)
                    .send();
                return;
            }

            // update quest
            QuestRegistry.getInstance().submit(this.stage.getQuest());

            // switch GUI
            new UpdateScreen(
                Arrays.asList("queststage"), // set the previous screen 
                director // set the client director
            ).execute();
        });

        // connection editor
        new GUISlot(this.gui, 11)
            .setItem("STICKY_PISTON")
            .setLabel("Change Sequence")
            .onClick(() -> {
                this.director.setCurrentInstance(this.action.getConnections());

                new UpdateScreen(
                    Arrays.asList("connectioneditor"), 
                    director
                ).execute();
            });

        // the delete button
        new GUISlot(this.gui, 18)
            .setLabel("Delete Action")
            .setItem("RED_DYE")
            .onClick(() -> {
                // protect from accidental deletion
                if (stage.getEntryPoint().getAction(stage.getQuest()).equals(action)) {
                    ChatUtils.message("Cannot remove the stage starting point action.")
                        .player(this.director.getPlayer())
                        .type(MessageType.WARN)
                        .send();
                    return;
                }

                // remove the action
                stage.removeAction(action);
                
                // update the quest
                QuestRegistry.getInstance().submit(this.stage.getQuest());

                // update UI
                new UpdateScreen(
                    Arrays.asList("queststage"), 
                    director
                ).execute();
            });

        // changing action type button
        GUISlot typeButton = new GUISlot(this.gui, 1);
        typeButton.setItem("FIREWORK_ROCKET");
        typeButton.setLabel("Change Type (" + this.action.toString() + ")");
        typeButton.onClick(() -> {
            new UpdateScreen(
                Arrays.asList("actiontypes"),
                director
            ).execute();
        });

        // setting current as stage entry point button
        GUISlot entrypointButton = new GUISlot(this.gui, 2);
        entrypointButton.setItem("ENDER_EYE");
        entrypointButton.setLabel("Set Action As Entry Point");
        entrypointButton.onClick(() -> {
            this.stage.setEntryPoint(new StagePath(this.stage, this.action)); // set this action as the stage entry point
            this.execute(); // re-run to see changes
        });

        IntStream.of(3, 12).forEach((int value) -> {
            GUISlot dividerSlot = new GUISlot(this.gui, value);
            dividerSlot.setItem("BLACK_STAINED_GLASS_PANE");
            dividerSlot.setLabel(" ");
        });
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
                        Arrays.asList("selectnpc"), 
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
                if (this.action.getDialogue() != null) {
                    String dialogue = action.getDialogue().get(0);
                    
                    optionSlot.setLabel(
                        String.format("%s (%s...)", 
                            option.getLabel(), 
                            dialogue.length() >= 8 ? dialogue.substring(0, 8) : dialogue
                        )
                    );
                }

                optionSlot.onClick(() -> { 
                    new ChatPrompt(
                        Arrays.asList("Enter the dialogue", "none"), 
                        this.director
                    ).onFinish((function) -> {
                        ChatPrompt prompt = (ChatPrompt) function; // cast the GUIFunction as ChatPrompt
                        String response = prompt.getResponse(); // get inputted dialogue

                        if (response == null) {
                            return;
                        }
                        
                        // set dialogue with prompt response
                        this.action.setDialogue(Arrays.asList(response));
                        
                        // refresh to see changes
                        this.execute();
                    }).execute();
                });
                break;
            case ITEMS:
                // open the items list screen
                optionSlot.onClick(() -> {
                    // fetch existing items list
                    ArrayList<ItemStack> items = (ArrayList<ItemStack>) this.action.getItems();

                    // if items list exists, set it as the current instance to use
                    if (items != null) {
                        this.director.setCurrentInstance(items);
                    }

                    new UpdateScreen(
                        Arrays.asList("itemslist"), 
                        director
                    ).onFinish((GUI) -> {
                        // get the itemslist gui instance
                        UpdateScreen updateScreen = (UpdateScreen) GUI;
                        Dynamicitemslist itemslistGUI = (Dynamicitemslist) updateScreen.getDynamicGUI();

                        itemslistGUI.onFinish((_) -> {
                            List<ItemStack> itemslist = itemslistGUI.getItems();

                            // exit if no items in the list
                            if (itemslist == null) {
                                return;
                            }
                            
                            // set this as the list of items
                            this.action.setItems(itemslist);

                            // update quest
                            this.stage.getQuest().save();
                            
                            // refresh to see changes
                            this.execute();
                        });
                    }).execute();
                });
                break;
        case FINISH_MESSAGE:
                String finishMessage = this.action.getFinishMessage();

                if (finishMessage != null) {
                    Integer maxLength = 16;
                    optionSlot.setDescription(String.format("%s%s", 
                        finishMessage.length() >= maxLength ? finishMessage.substring(0, maxLength) : finishMessage,
                        finishMessage.length() > maxLength ? "..." : ""
                    ));
                }

                // handle clicking the option slot
                optionSlot.onClick(() -> {

                    // create a new chat prompt to get the value
                    new ChatPrompt(
                        Arrays.asList("Enter the finish message", "none"), director
                    )
                    .onFinish((f) -> {
                        ChatPrompt function = (ChatPrompt) f;
                        
                        // get the value
                        this.action.setFinishMessage(function.getResponse());

                        // refresh to see updated value
                        this.execute();
                    }).execute();;
                });
                break;
        }
    }
}

