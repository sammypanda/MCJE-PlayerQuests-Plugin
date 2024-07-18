package playerquests.builder.gui.function;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.bukkit.ChatColor;

import playerquests.Core;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.PluginUtils; // used to validate function params

/**
 * Invokes the save method on a class instance.
 */
public class Save extends GUIFunction {

    /**
     * Saves an instance by key name.
     * @param params 1. key name
     * @param director to control the plugin
     */
    public Save(ArrayList<Object> params, ClientDirector director) {
        super(params, director);
    }

    @Override
    public void execute() {
        try {
            PluginUtils.validateParams(this.params, String.class);
        } catch (IllegalArgumentException e) {
            this.errored = true;
            ChatUtils.message(e.getMessage())
                .player(this.director.getPlayer())
                .type(ChatUtils.MessageType.ERROR)
                .send();
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
            if (response != null) {
                ChatUtils.message(response.toString())
                    .player(this.director.getPlayer())
                    .type(ChatUtils.MessageType.ERROR)
                    .send();
            }
            
            this.finished(); // running onFinish code
            return;
        }

        this.director.getPlayer().sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "\n" + response + ".\n" + ChatColor.RESET);

        new CloseScreen(new ArrayList<>(), this.director).execute();

        this.finished(); // running onFinish code
    }
    
}
