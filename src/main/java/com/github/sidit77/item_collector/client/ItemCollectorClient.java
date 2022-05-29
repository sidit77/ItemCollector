package com.github.sidit77.item_collector.client;

import com.github.sidit77.item_collector.ItemCollector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.api.EnvType.CLIENT;

@Environment(CLIENT)
public class ItemCollectorClient implements ClientModInitializer {

    private static final KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.item_collector.collect",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.gameplay"
    ));
    private boolean keyDown = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (keyBinding.isPressed()) {
                if(!keyDown){
                    //client.player.sendMessage(new LiteralText("Key 1 was pressed!"), false);
                    ItemCollector.log(Level.DEBUG, "Pressed collect button");
                    ClientPlayNetworking.send(ItemCollector.COLLECT_TOGGLE_PACKET_ID, PacketByteBufs.empty());
                    keyDown = true;
                }
            } else {
                keyDown = false;
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(ItemCollector.COLLECT_RESPONSE_PACKET_ID, (client, handler, buf, responseSender) -> {
            ItemCollector.log(Level.DEBUG, "Collector response received");
            ClientPlayerEntity player = client.player;
            if (player == null)
                return;
            if (buf.readBoolean()) {
                player.sendMessage(new TranslatableText("msg.item_collector.activated"), true);
            } else {
                player.sendMessage(new TranslatableText("msg.item_collector.deactivated"), true);
            }
        });
    }
}
