package com.github.sidit77.item_collector;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ItemCollector implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "item_collector";
    public static final String MOD_NAME = "Item Collector";

    public static final Identifier COLLECT_TOGGLE_PACKET_ID = new Identifier(MOD_ID, "collect_toggle_packet");
    public static final Identifier COLLECT_RESPONSE_PACKET_ID = new Identifier(MOD_ID, "collect_response_packet");

    private final Set<UUID> collectingPlayers = new HashSet<>();

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing..");
        collectingPlayers.clear();

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            List<UUID> deadPlayers = new ArrayList<>();
            for(UUID playerId : collectingPlayers) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if(player == null) {
                    deadPlayers.add(playerId);
                    continue;
                }
                Vec3d playerPos = player.getPos().add(0,player.getStandingEyeHeight() * 0.6,0);
                List<ItemEntity> items = player
                        .getServerWorld()
                        .getEntitiesByType(EntityType.ITEM,
                                player.getBoundingBox().expand(16.0D),
                                EntityPredicates.VALID_ENTITY);
                for(ItemEntity item : items) {
                    //item.setPosition(playerPos.x, playerPos.y, playerPos.z);
                    Vec3d vel = playerPos.subtract(item.getPos());
                    double dist = vel.length();
                    //if (dist > 2.0 * 2.0) {
                    vel = vel.normalize().multiply(Math.min(0.1, dist * 0.03));
                    item.addVelocity(vel.x, vel.y, vel.z);
                   // }
                }
            }
            deadPlayers.forEach(collectingPlayers::remove);
        });

        ServerPlayNetworking.registerGlobalReceiver(COLLECT_TOGGLE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            log(Level.DEBUG, "Received collect packet");
            if (!collectingPlayers.add(player.getUuid())){
                collectingPlayers.remove(player.getUuid());
            }
            PacketByteBuf resp = PacketByteBufs.create();
            resp.writeBoolean(collectingPlayers.contains(player.getUuid()));
            responseSender.sendPacket(COLLECT_RESPONSE_PACKET_ID, resp);
        });
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }

}
