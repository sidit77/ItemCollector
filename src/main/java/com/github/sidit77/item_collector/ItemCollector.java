package com.github.sidit77.item_collector;

import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ItemCollector implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "item_collector";
    public static final String MOD_NAME = "Item Collector";

    public static final Identifier COLLECT_PACKET_ID = new Identifier(MOD_ID, "collect_packet");

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing..");
        ServerPlayNetworking.registerGlobalReceiver(COLLECT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            log(Level.DEBUG, "Received collect packet");
            server.execute(() -> {
                Vec3d playerPos = player.getPos();
                List<ItemEntity> items = player
                        .getServerWorld()
                        .getEntitiesByType(EntityType.ITEM,
                                player.getBoundingBox().expand(16.0D),
                                EntityPredicates.VALID_ENTITY);
                for(ItemEntity item : items) {
                    item.setPosition(playerPos.x, playerPos.y, playerPos.z);
                }
            });
        });
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }

}
