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

    // 当前帧索引
    private int currentFrame = 0;
    // 动画计时器 (每帧增加 1，达到阈值重置)
    private int tickCounter = 0;
    // 每过多少 Tick 切换一帧 (100ms = 2 Ticks)
    private static final int TICKS_PER_FRAME = 2;

    public BHButton(int x, int y) {
        // 注意：这里去掉了原本写死的 0,0。坐标交由外部 Screen 控制更灵活。
        super(x, y, FRAME_WIDTH, FRAME_HEIGHT, Component.empty(), btn -> {
            // 发送 C2S 包，注意 BHPacketHandle.INSTANCE 可能为 null (前面提过的初始化问题)
            BHPacketHandle.INSTANCE.sendToServer(new InsertAllToNetPacket());
        });
    }

    public void tick() {
        tickCounter++;
        if (tickCounter >= TICKS_PER_FRAME) {
            tickCounter = 0;
            currentFrame++;
            if (currentFrame >= TOTAL_FRAMES) {
                currentFrame = 0;
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int vOffset = currentFrame * FRAME_HEIGHT;

        // 3. 绘制
        // 参数依次为：纹理, 屏幕X, 屏幕Y, 纹理U, 纹理V, 宽度, 高度, 图集总宽度, 图集总高度
        // 注意：图集总宽 = 16，总高 = 16 * 9 = 144。
        // 如果你的原图被 padding 过，请修改这里的 TEXTURE_WIDTH 和 TEXTURE_HEIGHT
        int textureAtlasHeight = FRAME_HEIGHT * TOTAL_FRAMES;

        guiGraphics.blit(TEXTURE,
                this.getX(), this.getY(),
                0, vOffset, // 这里 vOffset 随时间动态变化
                this.width, this.height, FRAME_WIDTH, textureAtlasHeight);

        // 可选：如果你想在鼠标悬停时画一个白色的半透明遮罩表示选中
        if (this.isHovered) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }
    }
}