package me.afk.beyondhipu.network;

import me.afk.beyondhipu.BeyondHIPU;
import me.afk.beyondhipu.network.packet.c2s.InsertAllToNetPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = BeyondHIPU.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BHPacketHandle {
    public static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryBuild(BeyondHIPU.MODID, "simple_channel"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 1;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        // 注意：commonSetup 可能是并行事件，但 registerMessage 本身要求按顺序调用，
        // 这里为了简单直接调用；如需严格安全，用 event.enqueueWork(BHPacketHandle::registerPackets)
        event.enqueueWork(BHPacketHandle::registerPackets);
    }

    private static void registerPackets() {
        registerC2S();
    }

    // ---- 辅助方法 ----

    private static <MSG> void registerC2S(
            Class<MSG> type,
            BiConsumer<MSG, net.minecraft.network.FriendlyByteBuf> encoder,
            Function<net.minecraft.network.FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler
    ) {
        INSTANCE.registerMessage(
                packetId++,
                type,
                encoder,
                decoder,
                handler,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    private static <MSG> void registerS2C(
            Class<MSG> type,
            BiConsumer<MSG, net.minecraft.network.FriendlyByteBuf> encoder,
            Function<net.minecraft.network.FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler
    ) {
        INSTANCE.registerMessage(
                packetId++,
                type,
                encoder,
                decoder,
                handler,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    // ---- 注册 C2S 包 ----

    private static void registerC2S() {
        registerC2S(
                InsertAllToNetPacket.class,
                InsertAllToNetPacket::encode,
                InsertAllToNetPacket::decode,
                InsertAllToNetPacket::handle
        );

        // 以后加新包就继续往下追加：
        // registerC2S(AnotherC2SPacket.class,
        //         AnotherC2SPacket::encode,
        //         AnotherC2SPacket::decode,
        //         AnotherC2SPacket::handle);
    }
}