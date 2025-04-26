package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.GUIFunction;
import playerquests.builder.gui.function.SelectEntity;
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
            .setItem(Material.EGG)
            .onClick(() -> {
                new SelectEntity(
                    Arrays.asList(
                        "Select an entity", // the prompt message
                        List.of(), // denied entities (none)
                        List.of() // denied SelectMethods (none)
                    ), 
                    director).onFinish((f) -> {
                        GUIFunction guiFunction = (GUIFunction) f;
                        SelectEntity selectEntity = (SelectEntity) guiFunction;

                        System.out.println(selectEntity.getResult());
                    }).execute();
            });
    }
}
