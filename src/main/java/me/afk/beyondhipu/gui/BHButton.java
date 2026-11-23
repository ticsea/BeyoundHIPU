package me.afk.beyondhipu.gui;

import com.wintercogs.beyonddimensions.common.init.BDItems;
import me.afk.beyondhipu.network.packet.c2s.InsertAllToNetPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

//? if >= 1.21.11
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
//? elif 1.21.1
//import net.neoforged.neoforge.network.PacketDistributor;

//? if >=26.1.2
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? elif <=1.21.11
//import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BHButton extends ExtendedButton {
    // 使用 beyonddimensions 的物品贴图作为序列帧图集
//    private static final Identifier TEXTURE = Identifier.tryBuild("beyonddimensions", "textures/item/net_creater.png");
    private static ItemStack icon = null;

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
            //? if >=1.21.11
            ClientPacketDistributor.sendToServer(new InsertAllToNetPacket());
            //? elif 1.21.1
            //PacketDistributor.sendToServer(new InsertAllToNetPacket());
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
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

        if (icon != null) {
            //? if >=26.1.2
            guiGraphics.fakeItem(icon, this.getX(), this.getY());
            //? else
            //guiGraphics.renderItem(icon, this.getX(), this.getY());
        } else {
            getItem();
        }
        // 可选：如果你想在鼠标悬停时画一个白色的半透明遮罩表示选中
        if (this.isHovered) {
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
        }
    }

    private void getItem() {
        if (icon == null) {
            icon = new ItemStack(BDItems.NET_CREATER.get());
        }
    }
}