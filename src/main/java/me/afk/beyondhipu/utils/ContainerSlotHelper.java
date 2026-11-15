package me.afk.beyondhipu.utils;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;

import java.util.*;

public class ContainerSlotHelper {

    /**
     * 【新增】构建按钮坐标锚点（三层兜底策略）。
     * 目标位置：Menu 的水平正中央，距离 Menu 底部向上 100 像素。
     * 为了确保这个位置在玩家背包（Inventory）区域内，使用三层策略寻找最贴近的槽位。
     *
     * @param screen 当前 GUI 界面
     * @param player 当前玩家
     * @return 包含 {x, y} 的屏幕绝对坐标
     */
    public static int[] buildButtonAnchor(AbstractContainerScreen<?> screen, Player player) {
        // 计算界面绝对中心 X 坐标 (用作 X 轴的唯一参考基准)
        int guiCenterX = screen.getGuiLeft() + (screen.getXSize() / 2);
        AbstractContainerMenu menu = screen.getMenu();
        Slot closestSlot = null;

        // ==========================================
        // 策略 1：循环筛选属于 Inventory 的 Slot ,并找到第13个...
        // ==========================================
        Slot partOne = findPlayerSlot(menu, 13);

        // ==========================================
        // 策略 2: 从inventory矩形中找y最小的一行的居中的slot.
        // ==========================================
        Slot partTwo = null;
        List<Slot> validSlots = getValidContainerSlots(menu, player, false);
        ZoneScanner.ScanResult scanResult = ZoneScanner.scan(validSlots);

        int minXDiff = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (Slot slot : menu.slots) {
           if (!ZoneScanner.isInBounds(slot, scanResult.mainInventoryBounds)) continue;

            // 【修正 1】：算出该槽位在屏幕上的绝对 X 坐标
            int slotAbsoluteX = screen.getGuiLeft() + slot.x;

            // 计算 X 轴距离（用绝对坐标和绝对坐标比）
            int xDiff = Math.abs(slotAbsoluteX - guiCenterX);

            // 核心逻辑：先比较 X 轴
            if (xDiff < minXDiff) {
                // 如果 X 轴距离更近，直接占据榜首
                minXDiff = xDiff;
                minY = slot.y; // 记录相对 Y 坐标用于比较
                partTwo = slot;
            } else if (xDiff == minXDiff) {
                // 如果 X 轴距离一样，再比较 Y 轴
                if (slot.y < minY) {
                    // 选 Y 更小的（即更靠上的那一行）
                    minY = slot.y;
                    partTwo = slot;
                }
            }
        }

       if (partOne != null && Math.abs((screen.getGuiLeft() + partOne.x) - guiCenterX) < 30){
           closestSlot = partOne;
       } else if (partTwo != null && Math.abs((screen.getGuiLeft() + partTwo.x) - guiCenterX) < 30){
           closestSlot = partTwo;
       }

        // 如果策略 1 找到了，直接把坐标锁定到这个槽位的屏幕绝对坐标
        if (closestSlot != null) {
            return new int[]{
                    screen.getGuiLeft() + closestSlot.x, // 【修正 2】：加上 guiLeft
                    screen.getGuiTop() + closestSlot.y   // 加上 guiTop
            };
        }

        // 如果以上全部失败（比如遇到极度奇葩的模组），返回一个安全的屏幕相对坐标

        return new int[]{screen.getGuiLeft() + scanResult.hotbarBounds[0], screen.getGuiTop() + scanResult.hotbarBounds[1] + 32};
    }

    /**
     * 判断一个 Slot 是否属于玩家的自身背包（主背包/快捷栏/副手/盔甲）。
     * <p>
     * 借鉴了 Inventory Profiles Next (IPN) 的逻辑，使用容器引用比对，
     * 并排除一些特殊的假槽位。
     * </p>
     *
     * @param slot   要检查的槽位
     * @param player 当前玩家
     * @return 如果属于玩家背包，返回 true
     */
    public static boolean isPlayerInventorySlot(Slot slot, Player player) {
        if (player == null || slot == null) return false;
        // 最安全的比对：直接判断引用是否是玩家的 Inventory 实例
        return slot.container == player.getInventory();
    }

    /**
     * 判断一个 Slot 是否是一个“真实有效”的容器槽位。
     * 排除了玩家背包、隐藏槽位、合成结果槽、交易结果槽等。
     *
     * @param slot   要检查的槽位
     * @param player 当前玩家
     * @return 如果是真实容器槽位，返回 true
     */
    public static boolean isValidContainerSlot(Slot slot, Player player, boolean ignoreInventory) {
        // 1. 如果是玩家背包，直接排除
        if (ignoreInventory && isPlayerInventorySlot(slot, player)) return false;

        // 2. 排除隐藏槽位或非法槽位（某些模组的假槽坐标可能是负数）
        if (slot.x < 0 || slot.y < 0) return false;

        // 3. 排除原版已知的非存储槽位
        if (slot instanceof ResultSlot) return false; // 工作台/熔炉结果槽

        // 4. 使用类名模糊匹配，排除一些常见模组的结果槽
        String className = slot.getClass().getName();
        if (className.contains("CraftingResultSlot")) return false; // 合成结果槽
        if (className.contains("TradeOutputSlot")) return false;    // 村民交易结果槽
        if (className.contains("FurnaceResultSlot")) return false;  // 熔炉结果槽
        if (className.contains("AnvilResultSlot")) return false;    // 铁砧结果槽

        // 5. (可选) 排除不可拿取的槽位
        if (!slot.mayPickup(player)) return false;

        return true;
    }

    /**
     * 获取当前菜单中所有“真实的容器槽位”。
     *
     * @param menu   当前打开的菜单
     * @param player 当前玩家
     * @return 真实容器槽位的列表
     */
    public static List<Slot> getValidContainerSlots(AbstractContainerMenu menu, Player player, boolean ignoreInventory) {
        List<Slot> result = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (isValidContainerSlot(slot, player, ignoreInventory)) {
                result.add(slot);
            }
        }
        return result;
    }

    /**
     * 获取当前菜单中所有“玩家背包槽位”。
     *
     * @param menu   当前打开的菜单
     * @param player 当前玩家
     * @return 玩家背包槽位的列表
     */
    public static List<Slot> getPlayerInventorySlots(AbstractContainerMenu menu, Player player) {
        List<Slot> result = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (isPlayerInventorySlot(slot, player)) {
                result.add(slot);
            }
        }
        return result;
    }

    /**
     * 在菜单中查找属于玩家背包的特定槽位。
     *
     * @param menu 当前打开的菜单
     * @param targetContainerSlotIndex 玩家背包内的相对索引（9-35为主背包，0-8为快捷栏）
     * @return 找到的槽位，否则返回 null
     */
    private static Slot findPlayerSlot(AbstractContainerMenu menu, int targetContainerSlotIndex) {
        for (Slot slot : menu.slots) {
            // 判断是否属于玩家背包
            if (slot.container instanceof Inventory) {
                // 判断其相对索引
                if (slot.getContainerSlot() == targetContainerSlotIndex) {
                    return slot;
                }
            }
        }
        return null;
    }
}