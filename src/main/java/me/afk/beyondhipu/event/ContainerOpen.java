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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondHIPU.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)

public class ContainerOpen {

    private static BHButton activeButton = null;

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
            if (player != null && screenKey != null) {
                minecraft.player.displayClientMessage(Component.literal(screenKey).withStyle(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, screenKey)
                        ))
                        .withStyle(ChatFormatting.AQUA), false);
            }
        }

        if (!ClientConfig.isInWhitelist(screenKey)) return;

        // --- 获取玩家实例 ---
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 创建按钮
        BHButton button = new BHButton(0, 0);
        event.addListener(button);

        activeButton = button;

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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // 只在 Tick 结束阶段处理，防止一帧跑两次
        if (event.phase != TickEvent.Phase.END) return;

        // 如果当前有活跃的按钮，并且玩家确实还在查看界面，就更新它
        if (activeButton != null) {
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
                activeButton.tick(); // 调用你刚才写好的 tick 方法
            } else {
                // 如果界面关了，清空缓存
                activeButton = null;
            }
        }
    }
}
