package com.github.sidit77.item_collector;

import com.oroarmor.config.command.ConfigCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.command.ServerCommandSource;
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

    public static final ItemCollectorConfig CONFIG = new ItemCollectorConfig();

    private final Set<UUID> collectingPlayers = new HashSet<>();

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing..");

        CONFIG.readConfigFromFile();
        CONFIG.saveConfigToFile();
        ServerLifecycleEvents.SERVER_STOPPED.register(instance -> CONFIG.saveConfigToFile());
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> new ConfigCommand(CONFIG).register(dispatcher, dedicated));

        collectingPlayers.clear();

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            double range = ItemCollectorConfig.range.getValue();
            double maxAcc = ItemCollectorConfig.maxAcceleration.getValue();
            double accFac = ItemCollectorConfig.accelerationFactor.getValue();
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
                                player.getBoundingBox().expand(range),
                                EntityPredicates.VALID_ENTITY);
                for(ItemEntity item : items) {
                    //item.setPosition(playerPos.x, playerPos.y, playerPos.z);
                    Vec3d vel = playerPos.subtract(item.getPos());
                    double dist = vel.length();
                    //if (dist > 2.0 * 2.0) {
                    vel = vel.normalize().multiply(Math.min(maxAcc, dist * accFac));
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
