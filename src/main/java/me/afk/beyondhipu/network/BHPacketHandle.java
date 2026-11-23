package me.afk.beyondhipu.network;

import me.afk.beyondhipu.BeyondHIPU;
import me.afk.beyondhipu.network.packet.c2s.InsertAllToNetPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BeyondHIPU.MOD_ID)
public class BHPacketHandle {
    public static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // 获取注册器，并指定协议版本
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // 注册 C2S (客户端发给服务端) 的纯信号包
        registrar.playToServer(
                InsertAllToNetPacket.TYPE,
                InsertAllToNetPacket.STREAM_CODEC,
                InsertAllToNetPacket::handle
        );
    }
}