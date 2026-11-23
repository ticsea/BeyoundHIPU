package me.afk.beyondhipu.network.packet.c2s;

//? if 1.21.11 {
/*import com.wintercogs.beyonddimensions.Api.DataBase.DimensionsNet;
import com.wintercogs.beyonddimensions.Api.DataBase.Stack.ItemStackKey;
import com.wintercogs.beyonddimensions.Api.DataBase.Stack.KeyAmount;
import com.wintercogs.beyonddimensions.Api.DataBase.Storage.UnifiedStorage;
*///?} elif 1.21.1 || >=26.1.2 {
import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
//?}

import me.afk.beyondhipu.BeyondHIPU;
import me.afk.beyondhipu.config.ClientConfig;
import me.afk.beyondhipu.config.ServerConfig;
import me.afk.beyondhipu.utils.ContainerSlotHelper;
import me.afk.beyondhipu.utils.WhiteListUtil;
import me.afk.beyondhipu.utils.ZoneScanner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record InsertAllToNetPacket() implements CustomPacketPayload {

    // 定义包的唯一标识符
    public static final CustomPacketPayload.Type<InsertAllToNetPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    Identifier.fromNamespaceAndPath(BeyondHIPU.MOD_ID, "insert_all")
            );

    // 空包专用的编解码器
    public static final StreamCodec<RegistryFriendlyByteBuf, InsertAllToNetPacket> STREAM_CODEC =
            StreamCodec.unit(new InsertAllToNetPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 接收到包之后的处理逻辑
    public static void handle(InsertAllToNetPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 获取发送者
            if (!(context.player() instanceof ServerPlayer sender)) return;

            insertToNet(sender);
        });
    }

    // this code modify from beyond_storage_addon mod.
    // https://github.com/wwwqqq001/beyond_storage_addon/blob/main/src/main/java/com/example/beyondstorage/network/StoreLogic.java
    // 如有侵权,请联系我.
    private static void insertToNet(ServerPlayer player) {

        //? if 1.21.11
        //DimensionsNet net = DimensionsNet.getNetFromPlayer(player);
        //? elif 1.21.1 || >=26.1.2
        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null) {
            player.sendSystemMessage(Component.translatable("message.beyondstorage.no_net").withStyle(ChatFormatting.RED), true);
            return;
        }

        UnifiedStorage storage = net.getUnifiedStorage();

        // 2. 容器检查：必须是"非玩家背包"的容器
        AbstractContainerMenu container = player.containerMenu;
        if (container instanceof InventoryMenu) {
            // 玩家只打开了背包界面，里面全是玩家背包槽，没有意义
            player.sendSystemMessage(Component.translatable("message.beyondstorage.open_container").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        List<Slot> validSlots = ContainerSlotHelper.getValidContainerSlots(container, player, !ClientConfig.isUseVisualGrid());

        doInsert(player, validSlots, storage, container, ClientConfig.isUseVisualGrid());
    }

    private static void doInsert(ServerPlayer player, List<Slot> validSlots, UnifiedStorage storage, AbstractContainerMenu container, boolean isUseVisualGrid) {
        int movedStacks = 0;
        boolean networkFull = false;

        int[] bounds = null;
        if (isUseVisualGrid) {
            String menuKey = WhiteListUtil.getMenuKey(container);
            int[] ints = ServerConfig.getBounds(menuKey);
            if (ints == null) {
                ZoneScanner.ScanResult scanResult = ZoneScanner.scan(validSlots);
                // todo there may get null
                bounds = scanResult.containerBounds;
                ServerConfig.setBounds(menuKey, bounds[0], bounds[1], bounds[2], bounds[3]);
            } else {
                bounds = ints;
            }

        }

        for (Slot slot : validSlots) {
            if (!slot.hasItem()) continue;
            // 过滤非要求槽位
            if (isUseVisualGrid && !ZoneScanner.isInBounds(slot, bounds)) continue;

            ItemStack stack = slot.getItem();
            ItemStackKey key = new ItemStackKey(stack);

            KeyAmount result = storage.insert(key, stack.getCount(), false);

            long original = stack.getCount();
            long remaining = result.amount();

            if (remaining < original) {
                movedStacks++;
                if (remaining > 0) {
                    // 网络只装下一部分，把剩余数量写回槽位
                    ItemStack left = stack.copy();
                    left.setCount((int) remaining);
                    slot.set(left);
                    networkFull = true;
                } else {
                    // 全部存入
                    slot.set(ItemStack.EMPTY);
                }
            }
        }

        // 4. 同步 + 反馈
        if (movedStacks > 0) {
            container.broadcastChanges();
            player.sendSystemMessage(Component.translatable("message.beyondstorage.stored", movedStacks) .withStyle(ChatFormatting.GREEN), true);
        } else if (networkFull) {
            player.sendSystemMessage(Component.translatable("message.beyondstorage.network_full").withStyle(ChatFormatting.YELLOW), true);
        } else {
            player.sendSystemMessage(Component.translatable("message.beyondstorage.nothing_to_store").withStyle(ChatFormatting.GRAY), true);
        }
    }
}