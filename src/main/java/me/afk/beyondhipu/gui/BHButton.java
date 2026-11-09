package me.afk.beyondhipu.gui;

import me.afk.beyondhipu.network.BHPacketHandle;
import me.afk.beyondhipu.network.packet.c2s.InsertAllToNetPacket;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class BHButton extends ExtendedButton {
    // 使用 beyonddimensions 的物品贴图作为序列帧图集
    private static final ResourceLocation TEXTURE = ResourceLocation.tryBuild("beyonddimensions", "textures/item/net_creater.png");

    // 序列帧参数定义
    private static final int FRAME_WIDTH = 16;      // 每一帧的宽度
    private static final int FRAME_HEIGHT = 16;     // 每一帧的高度
    private static final int TOTAL_FRAMES = 9;      // 序列帧的总帧数
    private static final int FRAME_TIME_MS = 100;   // 每一帧停留的毫秒数 (越小越快)

    public BHButton(int x, int y) {
        // 注意：这里去掉了原本写死的 0,0。坐标交由外部 Screen 控制更灵活。
        super(x, y, FRAME_WIDTH, FRAME_HEIGHT, Component.empty(), btn -> {
            // 发送 C2S 包，注意 BHPacketHandle.INSTANCE 可能为 null (前面提过的初始化问题)
            if (BHPacketHandle.INSTANCE != null) {
                BHPacketHandle.INSTANCE.sendToServer(new InsertAllToNetPacket());
            }
        });
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 计算当前应该显示第几帧 (0 到 8)
        long currentTime = Util.getMillis();

        // 如果你想让鼠标悬停时才播放动画，取消下面这行的注释：
        // long effectiveTime = this.isHoveredOrFocused() ? currentTime : 0L;
        // 否则默认一直播放
        long effectiveTime = currentTime;

        int currentFrame = (int) ((effectiveTime / FRAME_TIME_MS) % TOTAL_FRAMES);

        // 2. 计算当前帧在纹理图集中的 V 坐标 (Y轴偏移)
        int vOffset = currentFrame * FRAME_HEIGHT;

        // 3. 绘制
        // 参数依次为：纹理, 屏幕X, 屏幕Y, 纹理U, 纹理V, 宽度, 高度, 图集总宽度, 图集总高度
        // 注意：图集总宽 = 16，总高 = 16 * 9 = 144。
        // 如果你的原图被 padding 过，请修改这里的 TEXTURE_WIDTH 和 TEXTURE_HEIGHT
        int textureAtlasWidth = FRAME_WIDTH;
        int textureAtlasHeight = FRAME_HEIGHT * TOTAL_FRAMES;

        guiGraphics.blit(TEXTURE,
                this.getX(), this.getY(),
                0, vOffset, // 这里 vOffset 随时间动态变化
                this.width, this.height,
                textureAtlasWidth, textureAtlasHeight);

        // 可选：如果你想在鼠标悬停时画一个白色的半透明遮罩表示选中
        if (this.isHovered) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }
    }
}