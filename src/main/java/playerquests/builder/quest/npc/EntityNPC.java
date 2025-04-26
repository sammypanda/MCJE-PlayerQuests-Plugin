package playerquests.builder.quest.npc;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.client.ClientDirector;

public class EntityNPC extends NPCType {

    @Override
    public void place(Player player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'place'");
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void remove(Player player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'remove'");
    }

    @Override
    public void refund(Player player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refund'");
    }

    @Override
    public void penalise(Player player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'penalise'");
    }

    @Override
    public GUISlot createTypeSlot(GUIDynamic screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc) {
        return new GUISlot(gui, slot)
            .setLabel("An Entity")
            .setDescription(
                List.of(
                    String.format("%sComing soon", ChatColor.DARK_GRAY)
                ))
            .setItem(Material.EGG);
    }
}
