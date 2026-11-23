package me.afk.beyondhipu.mixin;

import me.afk.beyondhipu.config.ClientConfig;
import me.afk.beyondhipu.network.BHPacketHandle;
import me.afk.beyondhipu.network.packet.c2s.InsertAllToNetPacket;
import me.afk.beyondhipu.utils.WhiteListUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >= 1.21.11
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
//? elif 1.21.1
//import net.neoforged.neoforge.network.PacketDistributor;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Inject(method = "handleContainerContent", at = @At("TAIL"))
    private void autoStoreToNet(ClientboundContainerSetContentPacket pPacket, CallbackInfo ci) {
        var mc = Minecraft.getInstance();
        if (!(mc.gui.screen() instanceof AbstractContainerScreen<?> containerScreen)) return;

        // 1. 白名单和开关判断
        String screenKey = WhiteListUtil.getScreenKey(containerScreen);
        if (!ClientConfig.isOn() || !ClientConfig.isInWhitelist(screenKey)) return;

        //? if >= 1.21.11
        ClientPacketDistributor.sendToServer(new InsertAllToNetPacket());
        //? elif 1.21.1
        //PacketDistributor.sendToServer(new InsertAllToNetPacket());
    }
}