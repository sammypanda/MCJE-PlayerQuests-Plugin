package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.md_5.bungee.api.ChatColor;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicnpctypes;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.SelectEntity;
import playerquests.builder.gui.function.SelectLocation;
import playerquests.builder.quest.data.LocationData;
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

    @Override
    public GUISlot createPlaceSlot(Dynamicnpctypes screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc) {
        LocationData locationData = npc.getLocation();

        return new GUISlot(gui, slot)
            .setLabel(
                String.format("%s", 
                    (npc.getLocation() == null) ? 
                        "Place NPC (" + npc.getAssigned().getType() + ")" :
                        "Relocate NPC (" + npc.getAssigned().getType() + ")"
                )
            )
            .setDescription(
                locationData != null ?
                    List.of( // %.0f = representing floats with 0 decimal point places
                        String.format("X: %.0f", locationData.getX()),
                        String.format("Y: %.0f", locationData.getY()),
                        String.format("Z: %.0f", locationData.getZ())
                    ) :
                List.of()
            )
            .setItem(Material.GREEN_STAINED_GLASS)
            .onClick(() -> {
                HumanEntity player = director.getPlayer();
                PlayerInventory playerInventory = player.getInventory();
                ItemStack[] playerInventoryContents = playerInventory.getContents();
                
                // temporarily empty the player inventory
                playerInventory.clear();

                // give the player the block to place
                playerInventory.setItemInMainHand(
                    new ItemStack(Material.GREEN_STAINED_GLASS)
                );

                new SelectLocation(
                    Arrays.asList(
                        "Place the NPC Block"
                    ),
                    director
                ).onFinish((f) -> {
                    // get the block that was selected
                    SelectLocation function = (SelectLocation) f;
                    LocationData location = function.getResult();

                    if (location != null) {
                        npc.setLocation(location);
                    }

                    // return the players old inventory
                    playerInventory.setContents(playerInventoryContents);

                    screen.refresh(); // re-draw to see changes
                }).execute();
            });
    }
}
