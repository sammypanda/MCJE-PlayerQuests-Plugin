package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.md_5.bungee.api.ChatColor;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.SelectEntity;
import playerquests.client.ClientDirector;
import playerquests.utility.singleton.PlayerQuests;

public class EntityNPC extends NPCType {

    /**
     * Defaut constructor (for Jackson)
    */
    public EntityNPC() {}

    /**
     * Constructs an EntityNPC with specified entity data and associated quest NPC.
     * @param value the block data string
     * @param npc the associated QuestNPC
     */
    public EntityNPC(String value, QuestNPC npc) {
        super(value, npc);
        this.type = "Entity";
    }

    /**
     * Constructs an EntityNPC using an entity object.
     * @param entity the entity
     * @param npc the associated QuestNPC
     */
    public EntityNPC(Entity entity, QuestNPC npc) {
        this(entity.getType().toString(), npc);
    }

    @Override
    public void place(Player player) {
        PlayerQuests.getEntityListener().registerEntityNPC(this, player);
    }

    @Override
    public void remove() {
        PlayerQuests.getEntityListener().unregisterEntityNPC(this);
    }

    @Override
    public void remove(Player player) {
        PlayerQuests.getEntityListener().unregisterEntityNPC(this, player);
    }

    @Override
    public void refund(Player player) {}

    @Override
    public void penalise(Player player) {}

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
                        SelectEntity selectEntity = (SelectEntity) f;
                        Entity entity = selectEntity.getResult();

                        // assign this block as the quest NPC
                        if (entity != null) {
                            EntityNPC entityNPC = new EntityNPC(entity, npc); // create NPC type
                            
                            // set this npc type
                            npc.assign(
                                entityNPC
                            );
                        }

                        gui.getResult().display();
                        screen.refresh();
                    }).execute();
            });
    }

    /**
     * Gets the entity representing this NPC.
     * @return the block data of the NPC
     */
    @JsonIgnore
    public EntityType getEntity() {
        EntityType finalEntity = EntityType.VILLAGER;

        try {
            finalEntity = EntityType.valueOf(value);
        } catch (IllegalArgumentException e) {
            System.err.println("malformed entity data in a quest.");
            this.value = finalEntity.toString();
        }

        return finalEntity;
    }
}
