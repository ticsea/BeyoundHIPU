package me.afk.beyondhipu.event;

import me.afk.beyondhipu.BeyondHIPU;
import me.afk.beyondhipu.Config;
import me.afk.beyondhipu.gui.BHButton;
import me.afk.beyondhipu.network.BHPacketHandle;
import me.afk.beyondhipu.network.packet.c2s.InsertAllToNetPacket;
import me.afk.beyondhipu.utils.WhiteListUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeyondHIPU.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)

public class ContainerOpen {


    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        // 判断当前 Screen 是否是容器界面（箱子、木桶、熔炉等都继承自 AbstractContainerScreen）
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }

        String screenKey = WhiteListUtil.getScreenKey(containerScreen);

        if (Config.isDebugPrintMode) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player != null && screenKey != null) {
                minecraft.player.displayClientMessage(Component.literal(screenKey).withStyle(style -> style.withClickEvent(
                                new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, screenKey)
                        ))
                        .withStyle(ChatFormatting.AQUA), false);
            }
        }

        if (!Config.isInWhitelist(screenKey)) return;

        // 创建按钮
        Button button = new BHButton(0, 0);
        event.addListener(button);

        // 获取容器界面的矩形信息
        int guiLeft = containerScreen.getGuiLeft();
        int guiTop = containerScreen.getGuiTop();
        int guiWidth = containerScreen.getXSize();
        int guiHeight = containerScreen.getYSize();

        // 计算位置
        AbstractContainerMenu menu = containerScreen.getMenu();
        int bx = 0;
        int by = 0;
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory && slot.getContainerSlot() == 13) {
                // 找到了！这个 slot 就是你需要的
//                int menuIndex = menu.slots.indexOf(slot); // 拿到菜单索引
                int screenX = guiLeft + slot.x;
                int screenY = guiTop + slot.y;
                bx = screenX;
                by = screenY - button.getHeight();
                break;
            }
        }

        // 1. X轴：绝对是容器的水平居中
        int buttonX = guiLeft + (guiWidth - button.getWidth()) / 2;
        int buttonY = guiTop + guiHeight - 99;

        button.setX(bx);
        button.setY(by);
    }
}
