package me.afk.beyondhipu.utils;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ContainerSlotHelper {

    private static int distanceSquared(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return dx * dx + dy * dy;
    }

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

        // ==========================================
        // 策略 1：循环筛选属于 Inventory 的 Slot，找最近的
        // ==========================================
        Slot closestSlot = null;
        int minXDiff = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        for (Slot slot : screen.getMenu().slots) {
            // 只筛选玩家背包槽位
            if (!isPlayerInventorySlot(slot, player)) continue;

            // 【修正 1】：算出该槽位在屏幕上的绝对 X 坐标
            int slotAbsoluteX = screen.getGuiLeft() + slot.x;

            // 计算 X 轴距离（用绝对坐标和绝对坐标比）
            int xDiff = Math.abs(slotAbsoluteX - guiCenterX);

            // 核心逻辑：先比较 X 轴
            if (xDiff < minXDiff) {
                // 如果 X 轴距离更近，直接占据榜首
                minXDiff = xDiff;
                minY = slot.y; // 记录相对 Y 坐标用于比较
                closestSlot = slot;
            } else if (xDiff == minXDiff) {
                // 如果 X 轴距离一样，再比较 Y 轴
                if (slot.y < minY) {
                    // 选 Y 更小的（即更靠上的那一行）
                    minY = slot.y;
                    closestSlot = slot;
                }
            }
        }

        // 如果策略 1 找到了，直接把坐标锁定到这个槽位的屏幕绝对坐标
        if (closestSlot != null) {
            return new int[]{
                    screen.getGuiLeft() + closestSlot.x, // 【修正 2】：加上 guiLeft
                    screen.getGuiTop() + closestSlot.y   // 加上 guiTop
            };
        }
        // ==========================================
        // 策略 2：调用原本就有的方法，找玩家主背包第一格 (最小 X, Y)
        // ==========================================
        int[] topLeftPos = findPlayerMainInvTopLeft(screen, player);
        if (topLeftPos != null) {
            // 目标点在主背包第一格中心的基础上，向外偏移一点
            return new int[]{topLeftPos[0], topLeftPos[1]};
        }

        // ==========================================
        // 策略 3：网格划定 Inventory 方法（终极视觉兜底）
        // ==========================================
        PlayerInvResult visualResult = scanPlayerInvByVisualGap(screen.getMenu());
        if (visualResult.isValid() && !visualResult.mainInventory.isEmpty()) {
            // 找到了 3x9 的主背包网格，直接使用第一格
            Slot firstSlot = visualResult.mainInventory.get(0);
            return new int[]{
                    screen.getGuiLeft() + firstSlot.x,
                    screen.getGuiTop() + firstSlot.y
            };
        }

        // 如果以上全部失败（比如遇到极度奇葩的模组），返回一个安全的屏幕相对坐标
        return new int[]{0, 0};
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
        if (!ignoreInventory && isPlayerInventorySlot(slot, player)) return false;

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
     * 根据玩家背包的相对索引（0-8快捷栏，9-35主背包，40副手）找到对应的菜单槽位，
     * 并返回其在屏幕上的绝对 X 坐标。
     * <p>
     * 常用于在特定槽位（比如主背包第 5 格）上方绘制按钮。
     * </p>
     *
     * @param screen                 当前的 GUI 界面
     * @param targetContainerSlotIndex 玩家背包内的相对索引
     * @return 绝对 X 坐标；如果未找到返回 -1
     */
    public static int getPlayerSlotScreenX(AbstractContainerScreen<?> screen, int targetContainerSlotIndex) {
        Slot slot = findPlayerSlot(screen.getMenu(), targetContainerSlotIndex);
        return slot != null ? screen.getGuiLeft() + slot.x : -1;
    }

    /**
     * 根据玩家背包的相对索引找到对应的菜单槽位，
     * 并返回其在屏幕上的绝对 Y 坐标。
     *
     * @param screen                 当前的 GUI 界面
     * @param targetContainerSlotIndex 玩家背包内的相对索引
     * @return 绝对 Y 坐标；如果未找到返回 -1
     */
    public static int getPlayerSlotScreenY(AbstractContainerScreen<?> screen, int targetContainerSlotIndex) {
        Slot slot = findPlayerSlot(screen.getMenu(), targetContainerSlotIndex);
        return slot != null ? screen.getGuiTop() + slot.y : -1;
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

    /**
     * 【重点修正】安全地找到玩家主背包的第一个槽位的屏幕绝对坐标。
     * 摒弃了依赖不可靠的相对索引（getContainerSlot）。
     * 使用相对位置特征：它在所有玩家背包槽位中，Y 坐标最小，且 X 坐标最小。
     *
     * @return 包含 x 和 y 的数组，如果找不到返回 null
     */
    public static int[] findPlayerMainInvTopLeft(AbstractContainerScreen<?> screen, Player player) {
        AbstractContainerMenu menu = screen.getMenu();
        Slot topLeftSlot = null;

        for (Slot slot : menu.slots) {
            // 1. 必须是当前玩家的真实背包
            if (!isPlayerInventorySlot(slot, player)) continue;

            // 2. 排除快捷栏、副手、盔甲槽（原版中，主背包的 Y 坐标通常比快捷栏小，且 X 坐标通常为 8 的倍数）
            // 最稳妥的判断：找所有玩家背包槽位中，Y 坐标最小、X 坐标最小的那个。它必定是主背包第一格。
            if (topLeftSlot == null) {
                topLeftSlot = slot;
            } else {
                if (slot.y < topLeftSlot.y || (slot.y == topLeftSlot.y && slot.x < topLeftSlot.x)) {
                    topLeftSlot = slot;
                }
            }
        }

        if (topLeftSlot != null) {
            // 将槽位相对坐标转为屏幕绝对坐标
            return new int[]{
                    screen.getGuiLeft() + topLeftSlot.x,
                    screen.getGuiTop() + topLeftSlot.y
            };
        }
        return null;
    }

    /**
     * 用于承载视觉扫描结果的内部类。
     */
    public static class PlayerInvResult {
        public final List<Slot> mainInventory = new ArrayList<>(); // 3x9 主背包
        public final List<Slot> hotbar = new ArrayList<>();        // 1x9 快捷栏
        private final java.util.Set<Slot> allPlayerSlots = new java.util.HashSet<>();

        public boolean isValid() {
            return mainInventory.size() == 27 && hotbar.size() == 9;
        }

        public void buildCache() {
            allPlayerSlots.clear();
            allPlayerSlots.addAll(mainInventory);
            allPlayerSlots.addAll(hotbar);
        }

        public boolean contains(Slot slot) {
            return slot != null && allPlayerSlots.contains(slot);
        }
    }

    /**
     * 终极扫描：完全摒弃底层类判断，纯粹依靠屏幕上槽位的排列特征（3x9 和 1x9 的大间隙）
     * 来精准分割玩家背包。
     */
    public static PlayerInvResult scanPlayerInvByVisualGap(AbstractContainerMenu menu) {
        PlayerInvResult result = new PlayerInvResult();
        if (menu == null) return result;

        // 1. 过滤可见槽位并按 Y 坐标分组
        List<List<Slot>> rows = new ArrayList<>();
        List<Slot> validSlots = new ArrayList<>();
        for (Slot slot : menu.slots) {
            if (slot.x >= 0 && slot.y >= 0) validSlots.add(slot);
        }
        validSlots.sort(Comparator.comparingInt(s -> s.y));

        for (Slot slot : validSlots) {
            boolean added = false;
            for (List<Slot> row : rows) {
                if (Math.abs(row.get(0).y - slot.y) <= 1) {
                    row.add(slot);
                    added = true;
                    break;
                }
            }
            if (!added) {
                List<Slot> newRow = new ArrayList<>();
                newRow.add(slot);
                rows.add(newRow);
            }
        }

        // 2. 行按 Y 坐标从大到小（从下往上）排序
        rows.sort((r1, r2) -> Integer.compare(r2.get(0).y, r1.get(0).y));

        // 3. 自下而上寻找特征
        for (int i = 0; i < rows.size() - 1; i++) {
            List<Slot> currentRow = rows.get(i);      // 可能是快捷栏
            List<Slot> aboveRow = rows.get(i + 1);    // 可能是主背包底行

            if (currentRow.size() >= 9 && aboveRow.size() >= 9) {
                currentRow.sort(Comparator.comparingInt(s -> s.x));
                aboveRow.sort(Comparator.comparingInt(s -> s.x));

                int hotbarY = currentRow.get(0).y;
                int mainInvBottomY = aboveRow.get(0).y;

                // 核心判定：间距特征
                int gap = mainInvBottomY - hotbarY;
                int standardGap = 18;
                if (i + 2 < rows.size() && rows.get(i + 2).size() >= 9) {
                    standardGap = mainInvBottomY - rows.get(i + 2).get(0).y;
                }

                // 如果主背包底行与快捷栏的间距 > 内部间距，说明找到了！
                if (gap > standardGap || gap >= 22) {
                    // 提取快捷栏 (只抓最左边的 9 个，防魔改)
                    for (int k = 0; k < Math.min(9, currentRow.size()); k++) {
                        result.hotbar.add(currentRow.get(k));
                    }

                    // 提取主背包底行
                    for (int k = 0; k < Math.min(9, aboveRow.size()); k++) {
                        result.mainInventory.add(aboveRow.get(k));
                    }

                    // 往上抓取其余的主背包行
                    int rowsFound = 1;
                    int searchIdx = i + 2;
                    while (rowsFound < 3 && searchIdx < rows.size()) {
                        List<Slot> row = rows.get(searchIdx);
                        if (row.size() >= 9) {
                            row.sort(Comparator.comparingInt(s -> s.x));
                            for (int k = 0; k < Math.min(9, row.size()); k++) {
                                result.mainInventory.add(row.get(k));
                            }
                            rowsFound++;
                        }
                        searchIdx++;
                    }

                    if (result.isValid()) {
                        return result;
                    } else {
                        result.mainInventory.clear();
                        result.hotbar.clear();
                    }
                }
            }
        }
        return result;
    }

    /**
     * 通过网格特征扫描出“容器”所在的矩形边界。
     * 依赖原版背包的绝对特征：最底部的 4 行必定属于玩家（1行快捷栏 + 3行主背包）。
     * 这 4 行以上的所有槽位，都属于容器。
     *
     * @param validSlots 已经过滤过、有效且可见的槽位列表
     * @return {minX, minY, maxX, maxY}，如果没有容器区域则返回 null
     */
    /**
     * 高性能容器边界扫描（保留“总行数-4”的核心思路）
     * 彻底消灭双层循环与全量排序，采用 TreeMap + 两次极速遍历。
     */
    // 注意,这个返回的是玩家背包的bounds
    public static int[] scanContainerGridBounds(List<Slot> allSlots) {
        // 快速防御，玩家背包至少 36 格 (3x9 + 1x9)
        if (allSlots == null || allSlots.size() < 36) return null;

        // 【极速核心 1】：使用 TreeMap 自动对 Y 坐标进行排序，免去 sort() 的开销
        // Key: 真实Y坐标, Value: 这一行的槽位列表
        TreeMap<Integer, List<Slot>> rowMap = new TreeMap<>();

        // === 第一次遍历：分组 (只需 O(N) 复杂度) ===
        for (Slot slot : allSlots) {
            Integer matchedY = null;
            // 利用 TreeMap 的红黑树特性，直接查前一个和后一个元素，避免遍历所有已有的行
            Map.Entry<Integer, List<Slot>> floor = rowMap.floorEntry(slot.y + 2);
            if (floor != null && Math.abs(floor.getKey() - slot.y) <= 2) {
                matchedY = floor.getKey();
            } else {
                Map.Entry<Integer, List<Slot>> ceil = rowMap.ceilingEntry(slot.y - 2);
                if (ceil != null && Math.abs(ceil.getKey() - slot.y) <= 2) {
                    matchedY = ceil.getKey();
                }
            }

            // 如果没有匹配到相近的行，就新建一行
            int finalY = (matchedY != null) ? matchedY : slot.y;
            rowMap.computeIfAbsent(finalY, k -> new ArrayList<>()).add(slot);
        }

        // 如果总行数 <= 4，说明只有玩家背包，没有额外的容器槽位
        if (rowMap.size() <= 4) return null;

        // 容器占据的行数 = 总行数 - 4
        int containerRowCount = rowMap.size() - 4;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        // === 第二次遍历：只遍历容器行，提取极值 ===
        // TreeMap 默认按 Y 升序（从上到下），所以直接迭代前 containerRowCount 个 value 即可
        int currentRow = 0;
        for (List<Slot> row : rowMap.values()) {
            if (currentRow >= containerRowCount) break; // 越界则停止

            // 遍历这一行的槽位（通常最多 9 个）
            for (Slot slot : row) {
                if (slot.x < minX) minX = slot.x;
                if (slot.x > maxX) maxX = slot.x;
                if (slot.y < minY) minY = slot.y;
                if (slot.y > maxY) maxY = slot.y;
            }
            currentRow++;
        }

        // 如果没有任何容器槽位
        if (minX == Integer.MAX_VALUE) return null;

        // 槽位宽高是 16，所以右下角边界要 +16
        return new int[]{minX, minY, maxX + 16, maxY + 16};
    }

    /**
     * 快速判断给定的槽位是否在指定的矩形边界内。
     *
     * @param slot   要判断的槽位
     * @param bounds 矩形边界数组，格式必须为 {minX, minY, maxX, maxY}
     * @return 如果槽位在矩形内返回 true，否则返回 false
     */
    public static boolean isSlotInBounds(Slot slot, int[] bounds) {
        // 防御性判空，防止 NPE
        if (slot == null || bounds == null || bounds.length < 4) {
            return false;
        }

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        // 判断槽位的 x 和 y 是否落在该矩形范围内
        // 注意：如果你的 maxX 已经包含了 +16，这里就用 < maxX
        return slot.x >= minX && slot.x < maxX &&
                slot.y >= minY && slot.y < maxY;
    }

    /**
     * 安全地清理并获取指定槽位中的物品（常用于“存入网络”时的数据读取）。
     *
     * @param slot   槽位
     * @param amount 要取出的数量（-1 表示全部取出）
     * @return 实际取出的物品栈（如果为空，返回 ItemStack.EMPTY）
     */
    public static ItemStack safeExtract(Slot slot, int amount) {
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        int extractCount = (amount == -1) ? stack.getCount() : Math.min(amount, stack.getCount());

        if (extractCount <= 0) return ItemStack.EMPTY;

        // 复制一份，避免直接修改原物品栈导致状态异常
        ItemStack extracted = stack.copy();
        extracted.setCount(extractCount);

        // 计算剩余数量并更新槽位
        int remaining = stack.getCount() - extractCount;
        if (remaining <= 0) {
            slot.set(ItemStack.EMPTY);
        } else {
            stack.setCount(remaining);
            slot.setChanged(); // 通知容器数据发生变化
        }

        return extracted;
    }
}