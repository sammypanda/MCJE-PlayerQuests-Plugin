package playerquests.builder.gui.component;

import java.util.ArrayList; // used to transport GUI functions
import java.util.List; // generic list type

import org.bukkit.ChatColor; // used to modify formatting of in-game chat text
import org.bukkit.Material;
import org.bukkit.entity.Player;

import playerquests.builder.gui.GUIBuilder; // the builder which enlists this slot
import playerquests.builder.gui.function.GUIFunction; // the way GUI functions are executed/managed/handled
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.serialisable.ItemSerialisable;
import playerquests.utility.serialisable.data.ItemData;

/**
 * The contents and function list of a slot.
 */
public class GUISlot {

    /**
     * The parent GUI builder that manages this slot.
     */
    private GUIBuilder builder;

    /**
     * The position of this slot within the GUI.
     */
    private Integer position;

    /**
     * The item or block displayed in this slot. Defaults to "GRAY_STAINED_GLASS_PANE".
     */
    private ItemSerialisable item = new ItemSerialisable("GRAY_STAINED_GLASS_PANE");

    /**
     * The label displayed when hovering over the slot. Defaults to a single space.
     */
    private String label = " ";
    
    /**
     * The description or subtitle displayed when hovering over the slot. Defaults to an empty string.
     */
    private List<String> description = new ArrayList<String>();

    /**
     * List of functions associated with this slot. Functions are executed when this slot is interacted with.
     */
    private List<GUIFunction> functionList = new ArrayList<>();

    /**
     * Indicates whether the slot has encountered a syntax error. Defaults to {@code false}.
     */
    private Boolean errored = false;

    /**
     * The function to be executed when the slot is clicked.
     */
    private Runnable onClick;

    /**
     * The stack count for the item in this slot, indicating quantity. Defaults to 1.
     */
    private Integer stackCount = 1; // 1 default, being no number shown

    /**
     * If the slot item is shiny.
     */
    private Boolean glinting = false;

    /**
     * Constructs a new {@code GUISlot} with the specified parent {@code GUIBuilder}.
     * @param builder The parent GUI builder managing this slot.
     * @param slotPosition The position of this slot in the GUI, starting at 1.
     */
    public GUISlot(GUIBuilder builder, Integer slotPosition) {
        this.builder = builder;
        this.setPosition(slotPosition);
    }

    /**
     * Adds a function to be executed when this slot is used.
     * @param guiFunction The {@code GUIFunction} to be added to this slot.
     * @return The modified instance of {@code GUISlot}.
     */
    public GUISlot addFunction(GUIFunction guiFunction) {
        this.functionList.add(guiFunction); // add to list of functions
        return this;
    }

    /**
     * Sets the position of this slot within the GUI.
     * @param position The position of the slot, starting from 1.
     * @return The modified instance of {@code GUISlot}.
     */
    public GUISlot setPosition(Integer position) {
        this.position = position; // set in our current GUISlot class

        this.builder.setSlot(position, this); // put our current GUISlot in builder
        return this;
    }

    /**
     * Gets the position of this slot within the GUI.
     * @return The position of the slot.
     */
    public Integer getPosition() {
        return this.position;
    }

    /**
     * Sets the item or block to be displayed in this slot.
     * @param item The item or block.
     * @return The modified instance of {@code GUISlot}.
     */
    public GUISlot setItem(ItemSerialisable item) {
        // handle if invalid item
        if (item.getItemData().equals(ItemData.AIR)) {
            this.errored = true;
            this.item = new ItemSerialisable("RED_STAINED_GLASS_PANE"); // express that there was a problem visually by using an alarming item
            ChatUtils.message("Failed to setItem in GUISlot " + item)
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .style(MessageStyle.SIMPLE)
                .send();
            return this;
        }

        this.item = item;
        return this;
    }

    /**
     * Sets the hover label for this slot.
     * @param label The label text to be displayed when hovering over the slot.
     * @return The modified instance of {@code GUISlot}.
     */
    public GUISlot setLabel(String label) {
        String errorLabel = "(Error)";

        // Evaluate label for error prefix and avoid malformatting labels
        label = String.format("%s%s%s%s", 
            ChatColor.RESET, // remove the italics set when changing from default item display name
            this.hasError() ? errorLabel : "", // add an error notice if applicable
            this.hasError() && !label.equals(" ") ? " " : "", // put whitespace if applicable
            this.hasError() && label.equals(" ") ? label.trim() : label // add the real label if applicable
        );
        
        this.label = label;
        return this;
    }

    /**
     * Determines whether this slot has encountered a syntax error.
     * @return {@code true} if there is an error, {@code false} otherwise.
     */
    public Boolean hasError() {
        return this.errored;
    }

    /**
     * Gets the item or block to be displayed in this slot.
     * @return The raw representation of the item or block.
     */
    public ItemSerialisable getItem() {
        return this.item;
    }

    /**
     * Gets the hover label for this slot.
     * @return The label text displayed when hovering over the slot.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Executes the functions associated with this slot.
     * @param player The player for whom the functions are executed.
     */
    public void execute(Player player) {
        if (this.functionList.isEmpty()) { return; }

        // get first function (the function will request the next when it is ready)
        GUIFunction function = this.functionList.get(0);

        // execute the function
        function.execute();
    }

    /**
     * Executes the next function in the list of functions associated with this slot.
     * @param player The player for whom the next function is executed.
     */
    public void executeNext(Player player) {
        // if no more functions, don't continue
        if (this.functionList.size() <= 1) { return; }

        // pop the first function off the list to reveal the next
        this.functionList.remove(0);

        // execute the next function
        this.execute(player);
    }

    /**
     * Sets the function to be executed when the slot is clicked.
     * @param onClick The {@code Runnable} to be executed on click.
     * @return the state of the GUI slot.
     */
    public GUISlot onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    /**
     * Executes the function set to run when the slot is clicked.
     */
    public void clicked() {
        if (this.onClick != null) {
            onClick.run();
        }
    }

    /**
     * Sets the hover description or subtitle for this slot.
     * @param descriptionLines The description text to be displayed when hovering over the slot.
     * @return The modified instance of {@code GUISlot}.
     */
    public GUISlot setDescription(List<String> descriptionLines) {
        String errorDescription = "";
        List<String> descriptionLinesProcessed = new ArrayList<>();

        descriptionLines.forEach(description -> {
            if (description.isEmpty()) {
                return;
            }

            // Evaluate label for error prefix and avoid malformatting labels
            description = String.format("%s%s%s%s", 
                description.isBlank() ? "" : ChatColor.RESET, // remove the italics set when changing from default item display name
                this.hasError() ? errorDescription : "", // add an error notice if applicable
                this.hasError() && !description.equals("") ? "" : "", // put whitespace if applicable
                this.hasError() && description.equals("") ? description.trim() : description // add the real label if applicable
            );

            descriptionLinesProcessed.add(description);
        });
        
        this.description = descriptionLinesProcessed;
        return this;
    }

    /**
     * Gets the hover description for this slot.
     * @return The description text displayed when hovering over the slot.
     */
    public List<String> getDescription() {
        return this.description;
    }

    /**
     * Sets the size of the ItemStack for this slot.
     * @param count The stack size, typically up to 64.
     */
    public void setCount(Integer count) {
        this.stackCount = count;
    }

    /**
     * Gets the size of the ItemStack for this slot.
     * @return The stack size.
     */
    public Integer getCount() {
        return this.stackCount;
    }

    /**
     * Sets whether the item is shining/glinting.
     * @param glinting if there is a glint.
     * @return the state of the GUI slot.
     */
    public GUISlot setGlinting(Boolean glinting) {
        this.glinting = glinting;
        return this;
    }

    /**
     * Gets whether the item is shining/glinting.
     * @return if there is a glint.
     */
    public boolean isGlinting() {
        return this.glinting;
    }

    /**
     * Simple translation method that allows for setting lame Material enums
     * as item; simply translates it into ItemSerialisable.
     * @param material the generic material
     * @return a generic item in a detailed container
     */
    public GUISlot setItem(Material material) {
        this.item = new ItemSerialisable(material.name());
        return this;
    }
}