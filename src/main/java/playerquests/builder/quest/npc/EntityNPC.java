package playerquests.builder.quest.npc;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import playerquests.Core;
import playerquests.builder.gui.GUIBuilder;
import playerquests.builder.gui.component.GUISlot;
import playerquests.builder.gui.dynamic.Dynamicnpctypes;
import playerquests.builder.gui.dynamic.GUIDynamic;
import playerquests.builder.gui.function.SelectEntity;
import playerquests.builder.gui.function.SelectLocation;
import playerquests.builder.quest.action.QuestAction;
import playerquests.builder.quest.data.LocationData;
import playerquests.client.ClientDirector;
import playerquests.client.quest.QuestClient;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.serialisable.EntitySerialisable;
import playerquests.utility.singleton.PlayerQuests;

public class EntityNPC extends NPCType {

    /**
     * Defaut constructor (for Jackson)
    */
    public EntityNPC() {}

    /**
     * Constructs an EntityNPC with specified entity data and associated quest NPC.
     * @param value the data string
     * @param npc the associated QuestNPC
     */
    public EntityNPC(String value, QuestNPC npc) {
        super(value, npc);
        this.type = "Entity";
    }

    /**
     * Constructs an EntityNPC using an entity object.
     * @param entitySerialisable the entity data
     * @param npc the associated QuestNPC
     */
    public EntityNPC(EntitySerialisable entitySerialisable, QuestNPC npc) {
        this(entitySerialisable.toString(), npc);
    }

    @Override
    public GUISlot createTypeSlot(GUIDynamic screen, ClientDirector director, GUIBuilder gui, Integer slot, QuestNPC npc) {
        final boolean hasCitizens2 = PlayerQuests.getInstance().hasCitizens2();

        return new GUISlot(gui, slot)
            .setLabel("An Entity")
            .setDescription(
                hasCitizens2 ?
                    Component.empty()
                        .append(Component.text("Work in Progress").color(NamedTextColor.DARK_GRAY))
                :
                    Component.empty()
                        .append(Component.text("Entity NPCs require"))
                        .append(Component.text("the Citizens2 plugin"))
            )
            .setItem(hasCitizens2 ? Material.EGG : Material.BARRIER)
            .onClick(() -> {
                if ( ! hasCitizens2) {
                    return;
                }

                new SelectEntity(
                    Arrays.asList(
                        "Select an entity", // the prompt message
                        List.of(), // denied entities (none)
                        List.of() // denied SelectMethods (none)
                    ),
                    director).onFinish((f) -> {
                        SelectEntity selectEntity = (SelectEntity) f;
                        EntitySerialisable entitySerialisable = selectEntity.getResult();

                        // assign this as the quest NPC
                        if (entitySerialisable != null) {
                            EntityNPC entityNPC = new EntityNPC(entitySerialisable, npc); // create NPC type

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
     * @return the data of the NPC
     */
    @JsonIgnore
    public EntitySerialisable getEntity() {
        EntitySerialisable entityData;

        try {
            entityData = new EntitySerialisable(this.value);
        } catch (IllegalArgumentException e) {
            ChatUtils.message("malformed entity data in a quest. " + e.getMessage())
                .target(MessageTarget.CONSOLE)
                .type(MessageType.ERROR)
                .send();
            entityData = new EntitySerialisable("entity:VILLAGER");
            this.value = entityData.toString(); // replace invalid data
        }

        return entityData;
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
                Player player = director.getPlayer();
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

    @Override
    protected void unregister(QuestAction<?,?> action, QuestClient quester) {
        quester.getData().removeEntityNPC(action, this.getNPC());
    }

    @Override
    protected void despawn(QuestAction<?,?> action, QuestClient quester) {
        NPC citizen = quester.getData().getCitizenNPC(action, this.getNPC());

        if (citizen == null) {
            return;
        }

        citizen.destroy();
    }

    @Override
    protected void register(QuestAction<?,?> action, QuestClient quester, Object value) {
        quester.getData().addCitizenNPC(action, this.getNPC(), (NPC) value);
    }

    @Override
    protected Object spawn(QuestAction<?,?> action, QuestClient quester) {
        QuestNPC questNPC = this.getNPC();
        Location location = questNPC.getLocation().toBukkitLocation();
        Player player = quester.getPlayer();

        // don't do anything if no player
        if (player == null) {
            return null;
        }

        // center on origin
        location.add(.5, 0, .5);

        // set to spawn facing player
        Vector calculatedVector = player.getLocation().toVector().subtract(location.toVector());
        location.setDirection(calculatedVector);

        // spawn entity in world
        NPC citizen = this.getEntity().spawn(location);
        Entity entity = citizen.getEntity();

        // hide nametag
        citizen.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);

        // set to look at closest player
        LookClose lookClose = citizen.getOrAddTrait(LookClose.class);
        lookClose.setRange(15); // default is 8 blocks
        lookClose.setRealisticLooking(true); // smoother movements (default: false)
        lookClose.toggle(); // enable

        // hide for everyone
        Bukkit.getServer().getOnlinePlayers().forEach(onlinePlayer -> {
            onlinePlayer.hideEntity(Core.getPlugin(), entity);
        });
        // except ourself
        player.showEntity(Core.getPlugin(), entity);

        // disable damage
        entity.setInvulnerable(true);

        // disable movement
        entity.setGravity(false);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0);
            livingEntity.setCollidable(false);
        }

        return citizen;
    }

    @Override
    public void refund(QuestClient quester) {
        // no resources
    }

    @Override
    public void penalise(QuestClient quester) {
        // no resources
    }
}
