package playerquests.builder.gui.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.ChatColor;

import playerquests.Core;
import playerquests.builder.gui.component.GUISlot;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.PluginUtils; // used to validate function params

/**
 * Invokes the save method on a class instance.
 */
public class Save extends GUIFunction {

    /**
     * Not intended to be created directly.
     * <p>
     * Saves an instance by key name.
     * @param params 1. key name
     * @param director to control the plugin
     * @param slot slot this function belongs to
     */
    public Save(ArrayList<Object> params, ClientDirector director, GUISlot slot) {
        super(params, director, slot);
    }

    @Override
    public void execute() {
        try {
            PluginUtils.validateParams(this.params, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.sendError(this.director.getPlayer(), e.getMessage());
        }

        String key = (String) this.params.get(0);

        Class<?> keyClass = Core.getKeyHandler().getClassFromKey(key);

        Object currentClass = this.director.getCurrentInstance(keyClass);

        Object response = "A failure occured";

        try {
            Method saveMethod = currentClass.getClass().getMethod("save");
            response = saveMethod.invoke(currentClass);
            
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            this.errored = true;
            response = e.getCause().getMessage();
        }

        if (this.errored) {
            ChatUtils.sendError(this.director.getPlayer(), response.toString());
            this.finished(); // onFinish runnable
            return;
        }

        this.director.getPlayer().sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "\n" + response + ".\n" + ChatColor.RESET);

        new CloseScreen(new ArrayList<>(), this.director, this.slot).execute();

        this.finished(); // onFinish runnable
    }
    
}
