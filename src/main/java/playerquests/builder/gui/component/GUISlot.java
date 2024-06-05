package playerquests.builder.gui.component;

import java.lang.reflect.InvocationTargetException; // used to check if a GUI function could not execute
import java.util.ArrayList; // used to transport GUI functions
import java.util.List; // generic list type
import java.util.Objects; // used for easy null checking

import org.bukkit.ChatColor; // used to modify formatting of in-game chat text
import org.bukkit.entity.HumanEntity; // refers to the player

import com.fasterxml.jackson.core.JsonProcessingException; // throws if the json is invalid
import com.fasterxml.jackson.core.type.TypeReference; // for passing in more specific types by allowing traits 
import com.fasterxml.jackson.databind.JsonNode; // the java-friendly object for holding the JSON info
import com.fasterxml.jackson.databind.ObjectMapper; // reads the JSON

import playerquests.builder.gui.GUIBuilder; // the builder which enlists this slot
import playerquests.builder.gui.function.GUIFunction; // the way GUI functions are executed/managed/handled
import playerquests.client.ClientDirector; // abstracted controls for the plugin
import playerquests.utility.MaterialUtils; // converts string of item to presentable itemstack

/**
 * The contents and function list of a slot.
 */
public class GUISlot {

    /**
     * the parent gui builder.
     */
    private GUIBuilder builder;

    /**
     * position this slot occupies in the GUI.
     */
    private Integer position;

    /**
     * default item/block
     */
    private String item = "GRAY_STAINED_GLASS_PANE";

    /**
     * label of the item in the slot (requires whitespace to show as empty)
     */
    private String label = " ";
    
    /**
     * description of the item in the slot
     */
    private String description = "";

    /**
     * list of functions associated with this slot/button.
     */
    private List<GUIFunction> functionList = new ArrayList<GUIFunction>();

    /**
     * has this slot encountered a syntax error
     */
    private Boolean errored = false;

    /**
     * function that can be set to run when this slot is clicked
     */
    private Runnable onClick;

    /**
     * Constructs a new GUISlot with the specified parent GUIBuilder.
     * <p>
     * This should not be accessed directly. Use GUIBuilder.newSlot() instead.
     * 
     * @param builder a parent GUI which manages the window/screen.
     * @param slotPosition where the slot should be in the GUI window, starting at 1.
     */
    public GUISlot(GUIBuilder builder, Integer slotPosition) {
        this.builder = builder;
        this.setPosition(slotPosition);
    }

    /**
     * Take the functions array and add each function with it's params to a list in the {@link GUISlot} instance.
     * @param functions the functions array from the JSON template.
     */
    public void parseFunctions(JsonNode functions) {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        String slotPosition = this.position.toString();
        String frameTitle = this.builder.getFrame().getTitle();

        functions.elements().forEachRemaining(function -> {
            JsonNode functionNameNode = Objects.requireNonNull(function.get("name"), "A function name is missing in an entry for slot " + slotPosition + " of the " + frameTitle + " screen.");
            String functionName = functionNameNode.asText();

            JsonNode paramsNode = Objects.requireNonNull(function.get("params"), "The 'params' list is missing for the " + functionName + " function, in slot " + slotPosition + " of the " + frameTitle + " screen." + " (create it even if it's empty)");
            String params = paramsNode.toString();

            ArrayList<Object> paramList;

            // Learn and prepare all the params to be bundled with the GUI Function
            try {
                paramList = jsonObjectMapper.readValue(params, new TypeReference<ArrayList<Object>>() {});
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("The 'params' list invalid or empty for the " + functionName + " function, in slot " + slotPosition + " of the " + frameTitle + " screen template.");
            }

            // construct a GUIFunction and add it to the GUI Slot instance
            try {
                Class<?> classRef = Class.forName("playerquests.builder.gui.function." + functionName);
                try {
                    GUIFunction guiFunction = (GUIFunction) classRef
                        .getDeclaredConstructor(ArrayList.class, ClientDirector.class)
                        .newInstance(paramList, this.builder.getDirector()); // create an instance of whichever function class
                    guiFunction.onFinish((f) -> {
                        this.executeNext(this.builder.getDirector().getPlayer()); // run the next function
                    });
                    this.addFunction(guiFunction); // ship the packaged GUI Function to be kept in the current GUI Slot instance
                    // NOTE: now we could run these parsed functions we put in the GUI Slot with: currentSlot.execute();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException("Error instantiating or invoking the " + functionName + " function, in slot " + slotPosition + " of the " + frameTitle + " screen template.", e);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found for the " + functionName + " function, in slot " + slotPosition + " of the " + frameTitle + " screen template.");
            } catch (SecurityException e) {
                throw new RuntimeException("Security exception while accessing the " + functionName + " function, in slot " + slotPosition + " of the " + frameTitle + " screen template.");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid arguments passed to the " + functionName + " function, in slot " + slotPosition + " of the " + frameTitle + " screen template.");
            }
        });
    }

    /**
     * Add a GUI Function ('Meta Action') to be executed when this GUI Slot
     * is used.
     * @param guiFunction the Meta Action function instance.
     * @return the modified instance of the slot builder
     */
    public GUISlot addFunction(GUIFunction guiFunction) {
        this.functionList.add(guiFunction); // add to list of functions
        return this;
    }

    /**
     * Sets the slot position for this instance of {@link GUISlot}.
     * <p>
     * This will replace whatever is existing in the passed in position.
     * @param position the actual position of the slot, starting from 1
     * @return the modified instance of the slot builder
     */
    public GUISlot setPosition(Integer position) {
        this.position = position; // set in our current GUISlot class

        this.builder.setSlot(position, this); // put our current GUISlot in builder
        return this;
    }

    /**
     * Gets the slot position for this instance of {@link GUISlot}.
     * <p>
     * @return the integer of the slot this button occupies
     */
    public Integer getPosition(Integer position) {
        return this.position;
    }

    /**
     * Sets the item/block that will fill the slot for this instance of {@link GUISlot}.
     * @param item the closest string representation to the {@link org.bukkit.Material} ENUM.
     * @return the modified instance of the slot builder
     */
    public GUISlot setItem(String item) {
        try { // check if the item would create a valid ItemStack (the Material exists and isn't legacy)
            MaterialUtils.toItemStack(item);
            this.item = item; // if no issue caught overwrite the default slot item
        } catch (IllegalArgumentException exception) { // this means the ItemStack failed to construct
            this.errored = true;
            this.item = "RED_STAINED_GLASS_PANE"; // express that there was a problem visually by using an alarming item
            System.err.println(exception.getMessage());
        }
        
        return this;
    }

    /**
     * Sets the hover label for this instance of {@link GUISlot}.
     * <p>
     * Includes some processing/formatting of the label.
     * @param label the title for what the button does/is for.
     * @return the modified instance of the slot builder
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
     * Determine whether the instance has encountered an error.
     * @return errored true or false value for if it has errored.
     */
    public Boolean hasError() {
        return this.errored;
    }

    /**
     * Gets the item/block that will fill the slot for this instance of {@link GUISlot}.
     * @return item the raw {@link String} representation of the item.
     */
    public String getItem() {
        return this.item;
    }

    /**
     * Gets the hover label for this instance of {@link GUISlot}.
     * @return the label when hovering over the slot.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Run the functions that are described in the GUI screen expression.
     * @param player who to execute the function(s) for
     */
    public void execute(HumanEntity player) {
        if (this.functionList.isEmpty()) { return; }

        // get first function (the function will request the next when it is ready)
        GUIFunction function = this.functionList.get(0);

        // execute the function
        function.execute();
    }

    /**
     * Run the NEXT function that is described in the GUI screen expression
     * @param player who to execute the next function for.
     */
    public void executeNext(HumanEntity player) {
        // if no more functions, don't continue
        if (this.functionList.size() <= 1) { return; }

        // pop the first function off the list to reveal the next
        this.functionList.remove(0);

        // execute the next function
        this.execute(player);
    }

    /**
     * Sets a function to be executed when the slot is clicked.
     * <p>
     * Template functions take precedent (GUIFunction).
     * If a function is to be added from within the code-land, it 
     * should use this to set the function to be executed.
     * @param onClick the function to run when the GUISlot is pressed
     */
    public void onClick(Runnable onClick) {
        this.onClick = onClick;
    }

    /**
     * Run the function set on this slot.
     * @see #onClick(Runnable)
     */
    public void clicked() {
        if (this.onClick != null) {
            onClick.run();
        }
    }

    /**
     * Sets the hover subtitle/description for this instance of {@link GUISlot}.
     * <p>
     * Includes some processing/formatting of the label.
     * @param description the description of the button
     * @return the modified instance of the slot builder
     */
    public GUISlot setDescription(String description) {
        String errorDescription = "";

        // Evaluate label for error prefix and avoid malformatting labels
        description = String.format("%s%s%s%s", 
            ChatColor.RESET, // remove the italics set when changing from default item display name
            this.hasError() ? errorDescription : "", // add an error notice if applicable
            this.hasError() && !description.equals("") ? "" : "", // put whitespace if applicable
            this.hasError() && description.equals("") ? description.trim() : description // add the real label if applicable
        );
        
        this.description = description;
        return this;
    }

    /**
     * Gets the hover description for this instance of {@link GUISlot}.
     * @return the description when hovering over the slot.
     */
    public String getDescription() {
        return this.description;
    }
}