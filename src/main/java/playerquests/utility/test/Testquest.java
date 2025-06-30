package playerquests.utility.test;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import playerquests.Core;
import playerquests.client.ClientDirector;
import playerquests.product.Quest;
import playerquests.utility.annotation.PlayerQuestsTest;
import playerquests.utility.singleton.QuestRegistry;

public class Testquest extends TestUtility {

    public Testquest(ClientDirector clientDirector) {
        super(clientDirector);
    }

    @PlayerQuestsTest
    public CompletableFuture<Boolean> testNoneAction() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Player player = this.clientDirector.getPlayer();
        Location originalLocation = player.getLocation();
        
        // create platform for test witness
        Location platformLocation = new Location(originalLocation.getWorld(), 10, 199, 0);
        BlockData prePlatformBlockData = platformLocation.getBlock().getBlockData();
        platformLocation.getBlock().setBlockData(Material.REDSTONE_BLOCK.createBlockData());
        Location npcLocation = new Location(originalLocation.getWorld(), 0, 200, 0);

        // teleport the player onto the platform (looking towards the test spot)
        player.teleport(new Location(originalLocation.getWorld(), 10, 200, 0, 90, 0));

        // clean palette for test
        npcLocation.getBlock().setBlockData(Material.AIR.createBlockData()); // make sure where the NPC would be is AIR to start off with

        // create the quest
        Quest testNoneActionQuest = Quest.fromJSONString("""
        {
            "title" : "testNoneAction",
            "npcs" : {
                "npc_0" : {
                "id" : "npc_0",
                "name" : "testNoneActionNPC",
                "assigned" : {
                    "type" : "BlockNPC",
                    "value" : "minecraft:grass_block[snowy=false]"
                },
                "location" : {
                    "world" : "world",
                    "x" : 0.0,
                    "y" : 200.0,
                    "z" : 0.0,
                    "pitch" : 0.0,
                    "yaw" : 0.0
                }
                }
            },
            "stages" : {
                "stage_0" : {
                "id" : "stage_0",
                "actions" : {
                    "action_0" : {
                    "type" : "NoneAction",
                    "data" : {
                        "id" : "action_0",
                        "next" : [ ],
                        "options" : [ {
                        "option" : "NPC",
                        "npc_id" : null
                        } ],
                        "conditions" : [ ]
                    },
                    "label" : null
                    }
                },
                "startpoints" : [ ],
                "label" : null
                }
            },
            "creator" : null,
            "id" : "testNoneAction",
            "startpoints" : [ "stage_0.action_0" ]
        }       
        """);
        testNoneActionQuest.save(); // bring to life

        // test the quest
        Boolean isNPCAir = npcLocation.getBlock().getType().equals(Material.AIR);

        // Schedule cleanup after 100 ticks
        Bukkit.getScheduler().runTaskLater(Core.getPlugin(), () -> {
            // pre-result clean-up
            player.teleport(originalLocation);
            platformLocation.getBlock().setBlockData(prePlatformBlockData);

            // send result
            result.complete(
                isNPCAir &&
                QuestRegistry.getInstance().hasQuest(testNoneActionQuest.getID(), true)
            );

            // post-result clean-up
            QuestRegistry.getInstance().delete(testNoneActionQuest, true, false, true);
        }, 100);

        return result;
    }
    
}
