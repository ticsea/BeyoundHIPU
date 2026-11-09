package me.afk.beyondhipu.network.packet.c2s;

import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.api.storage.key.KeyAmount;
import com.wintercogs.beyonddimensions.api.storage.key.impl.ItemStackKey;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InsertAllToNetPacket {
    public InsertAllToNetPacket() {
    }

    public static void encode(InsertAllToNetPacket packet, FriendlyByteBuf byteBuf) {
    }

    public static InsertAllToNetPacket decode(FriendlyByteBuf byteBuf) {
        return new InsertAllToNetPacket();
    }

    public static void handle(InsertAllToNetPacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            insertToNet(sender);
            ctx.get().setPacketHandled(true);
        });
    }

    // this code modify from beyond_storage_addon mod.
    // https://github.com/wwwqqq001/beyond_storage_addon/blob/main/src/main/java/com/example/beyondstorage/network/StoreLogic.java
    // 如有侵权,请联系我.
    private static void insertToNet(ServerPlayer player) {
        // 1. 超越维度检查
        /*if (!DimensionsNet.hasAnyNet(player)) {
            player.displayClientMessage(Component.translatable("message.beyondstorage.no_net").withStyle(ChatFormatting.RED), true);
            return;
        }*/

        DimensionsNet net = DimensionsNet.getPrimaryNetFromPlayer(player);
        if (net == null) {
            player.displayClientMessage(Component.translatable("message.beyondstorage.no_net").withStyle(ChatFormatting.RED), true);
            return;
        }

        UnifiedStorage storage = net.getUnifiedStorage();

        // 2. 容器检查：必须是"非玩家背包"的容器
        AbstractContainerMenu container = player.containerMenu;
        if (container instanceof InventoryMenu) {
            // 玩家只打开了背包界面，里面全是玩家背包槽，没有意义
            player.displayClientMessage(Component.translatable("message.beyondstorage.open_container").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        int movedStacks = 0;
        boolean networkFull = false;

        // 3. 遍历容器槽位
        for (Slot slot : container.slots) {
            // 排除玩家背包槽（工作台/箱子等界面会把玩家背包也加进 slots）
            if (slot.container instanceof Inventory) continue;

            // 排除合成结果槽，防止绕过 onTake 刷物品
            if (slot instanceof ResultSlot) continue;

            // 排除幽灵槽/禁用槽
            if (!slot.isActive()) continue;

            // 无物品或不可拿取
            if (!slot.hasItem() || !slot.mayPickup(player)) continue;

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
            player.displayClientMessage(Component.translatable("message.beyondstorage.stored", movedStacks) .withStyle(ChatFormatting.GREEN), true);
        } else if (networkFull) {
            player.displayClientMessage(Component.translatable("message.beyondstorage.network_full").withStyle(ChatFormatting.YELLOW), true);
        } else {
            player.displayClientMessage(Component.translatable("message.beyondstorage.nothing_to_store").withStyle(ChatFormatting.GRAY), true);
        }
    }
}
