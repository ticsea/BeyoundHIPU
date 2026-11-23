package me.afk.beyondhipu.event;

import me.afk.beyondhipu.BeyondHIPU;
import me.afk.beyondhipu.config.ClientConfig;
import me.afk.beyondhipu.gui.BHButton;
import me.afk.beyondhipu.utils.ContainerSlotHelper;
import me.afk.beyondhipu.utils.WhiteListUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = BeyondHIPU.MOD_ID, value = Dist.CLIENT)

public class ContainerOpen {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        // 判断当前 Screen 是否是容器界面（箱子、木桶、熔炉等都继承自 AbstractContainerScreen）
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        String screenKey = WhiteListUtil.getScreenKey(containerScreen);
        if (screenKey == null) {
            return;
        }

        if (ClientConfig.isDebugPrintMode) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player != null) {
                //? if >=26.1.2
                minecraft.player.sendSystemMessage(Component.literal(screenKey).withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(screenKey))).withStyle(ChatFormatting.AQUA));
                //? if 1.21.11
                //minecraft.player.sendSystemMessage(Component.literal(screenKey).withStyle(style -> style.withClickEvent(new ClickEvent.CopyToClipboard(screenKey))).withStyle(ChatFormatting.AQUA), false);
                //? if 1.21.1
                //minecraft.player.sendSystemMessage(Component.literal(screenKey).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, screenKey))).withStyle(ChatFormatting.AQUA), false);
            }
        }

        if (!ClientConfig.isInWhitelist(screenKey)) return;

        // --- 获取玩家实例 ---
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 创建按钮
        BHButton button = new BHButton(0, 0);
        event.addListener(button);

        // 计算位置
        int[] xy = ClientConfig.getButtonOffset(screenKey);
        if (xy == null) {
            xy = ContainerSlotHelper.buildButtonAnchor(containerScreen, player);
            ClientConfig.setButtonOffset(screenKey, xy[0], xy[1]);
        }
//        int bx = ContainerSlotHelper.getPlayerSlotScreenX(containerScreen, 13);
//        int by = ContainerSlotHelper.getPlayerSlotScreenY(containerScreen, 13) - button.getHeight();

        button.setX(xy[0]);
        button.setY(xy[1] - button.getHeight());
    }
}
