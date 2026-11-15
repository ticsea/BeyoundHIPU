package me.afk.beyondhipu.utils;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import java.util.*;

public class ZoneScanner {

    /**
     * 区域类型枚举
     */
    public enum ZoneType {
        CONTAINER,       // 容器
        MAIN_INVENTORY,  // 玩家主背包 (3x9)
        HOTBAR           // 玩家快捷栏 (1x9)
    }

    /**
     * 结果封装
     */
    public static class ScanResult {
        public final List<Zone> zones = new ArrayList<>(); // 所有的岛屿区域（从上到下）

        // 三大独立区域的边界
        public int[] containerBounds = null;
        public int[] mainInventoryBounds = null;
        public int[] hotbarBounds = null;
    }

    /**
     * 纯粹的几何矩形区域
     */
    public static class Zone {
        public ZoneType type = ZoneType.CONTAINER;
        public int minX = Integer.MAX_VALUE;
        public int minY = Integer.MAX_VALUE;
        public int maxX = Integer.MIN_VALUE;
        public int maxY = Integer.MIN_VALUE;

        public Zone() {}

        public void updateBounds(int x, int y) {
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        // 极速判定：槽位是否在这个矩形内
        public boolean contains(Slot slot) {
            return slot.x >= minX && slot.x <= maxX + 16
                    && slot.y >= minY && slot.y <= maxY + 16;
        }
    }

    public static ScanResult scan(List<Slot> validSlots) {
        ScanResult result = new ScanResult();
        if (validSlots == null) return result;

        // 1. 提取有效的 Y 坐标并去重排序
        TreeSet<Integer> uniqueYs = new TreeSet<>();
        for (Slot slot : validSlots) {
            if (slot.x < 0 || slot.y < 0) continue;
            Integer floor = uniqueYs.floor(slot.y + 2);
            if (floor != null && Math.abs(floor - slot.y) <= 2) continue;
            uniqueYs.add(slot.y);
        }

        if (uniqueYs.isEmpty()) return result;

        // 2. 寻找 Gap，生成 Zone 区间
        List<int[]> zoneRanges = new ArrayList<>();
        int startY = -1;
        int prevY = -1;

        for (int y : uniqueYs) {
            if (startY == -1) {
                startY = y;
            } else {
                if (y - prevY > 20) { // 切断阈值：20像素
                    zoneRanges.add(new int[]{startY, prevY});
                    startY = y;
                }
            }
            prevY = y;
        }
        zoneRanges.add(new int[]{startY, prevY});

        // 3. 初始化 Zone
        for (int i = 0; i < zoneRanges.size(); i++) {
            result.zones.add(new Zone());
        }

        // 4. 遍历槽位，更新极值
        for (Slot slot : validSlots) {
            if (slot.x < 0 || slot.y < 0) continue;

            for (int i = 0; i < zoneRanges.size(); i++) {
                int[] range = zoneRanges.get(i);
                if (slot.y >= range[0] - 2 && slot.y <= range[1] + 2) {
                    result.zones.get(i).updateBounds(slot.x, slot.y);
                    break;
                }
            }
        }

        // 5. 论资排辈：严格区分【容器】【主背包】【快捷栏】
        int totalZones = result.zones.size();

        if (totalZones >= 2) {
            // 倒数第一个必定是快捷栏
            result.zones.get(totalZones - 1).type = ZoneType.HOTBAR;
            // 倒数第二个必定是主背包
            result.zones.get(totalZones - 2).type = ZoneType.MAIN_INVENTORY;
        }
        // 倒数第三个及以上的所有 Zone，默认都是 CONTAINER

        // 6. 遍历所有 Zone，把各自的边界收集出来
        // 用 6 个变量单独计算，绝不合并！
        int[] cont = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        int[] mainInv = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        int[] hotbar = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};

        for (Zone zone : result.zones) {
            if (zone.minX == Integer.MAX_VALUE) continue; // 空区跳过

            switch (zone.type) {
                case CONTAINER:
                    cont[0] = Math.min(cont[0], zone.minX);
                    cont[1] = Math.min(cont[1], zone.minY);
                    cont[2] = Math.max(cont[2], zone.maxX);
                    cont[3] = Math.max(cont[3], zone.maxY);
                    break;
                case MAIN_INVENTORY:
                    mainInv[0] = Math.min(mainInv[0], zone.minX);
                    mainInv[1] = Math.min(mainInv[1], zone.minY);
                    mainInv[2] = Math.max(mainInv[2], zone.maxX);
                    mainInv[3] = Math.max(mainInv[3], zone.maxY);
                    break;
                case HOTBAR:
                    hotbar[0] = Math.min(hotbar[0], zone.minX);
                    hotbar[1] = Math.min(hotbar[1], zone.minY);
                    hotbar[2] = Math.max(hotbar[2], zone.maxX);
                    hotbar[3] = Math.max(hotbar[3], zone.maxY);
                    break;
            }
        }

        // 7. 组装返回格式（+16 补全边缘）
        if (cont[0] != Integer.MAX_VALUE) {
            result.containerBounds = new int[]{cont[0], cont[1], cont[2] + 16, cont[3] + 16};
        }
        if (mainInv[0] != Integer.MAX_VALUE) {
            result.mainInventoryBounds = new int[]{mainInv[0], mainInv[1], mainInv[2] + 16, mainInv[3] + 16};
        }
        if (hotbar[0] != Integer.MAX_VALUE) {
            result.hotbarBounds = new int[]{hotbar[0], hotbar[1], hotbar[2] + 16, hotbar[3] + 16};
        }

        return result;
    }

    public static boolean isInBounds(Slot slot, int[] bounds) {
        if (bounds == null || slot == null) return false;
        // 因为 bounds[2] 和 bounds[3] 已经包含了 +16 的宽度/高度补偿
        // 所以这里直接判断 slot.x 是否在 [minX, maxX] 之间即可
        return slot.x >= bounds[0] && slot.x <= bounds[2] &&
                slot.y >= bounds[1] && slot.y <= bounds[3];
    }

}