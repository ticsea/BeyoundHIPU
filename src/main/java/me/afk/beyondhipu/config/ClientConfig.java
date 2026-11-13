package me.afk.beyondhipu.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 白名单配置
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONTAINER_WHITELIST;

    // 自动拾取开关状态
    private static final ForgeConfigSpec.BooleanValue AUTO_PICKUP_ON;

    // Slot划分算法
    private static final ForgeConfigSpec.BooleanValue USE_VISUAL_GRID;

    // 【新增】按钮位置缓存 (格式: "menu_id:x,y")
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BUTTON_POS_CACHE;

    // 内存中缓存的按钮位置 Map (MenuId -> [offsetX, offsetY])
    private static final Map<String, int[]> BUTTON_POS_MAP = new HashMap<>();

    // 调试模式状态（不写入文件，重启游戏重置）
    public static boolean isDebugPrintMode = false;

    static {
        BUILDER.comment("Configuration for HaveItPickup").push("general");

        // 1. 自动拾取开关默认值设为 true
        AUTO_PICKUP_ON = BUILDER
                .comment("Whether the auto pickup is enabled by default.")
                .define("autoPickupOn", false);

        // 2. 白名单配置
        CONTAINER_WHITELIST = BUILDER
                .comment("List of container Menu IDs that will show the pickup button.")
                .defineListAllowEmpty(
                        "containerWhitelist",
                        List.of("minecraft:generic_9x6", "minecraft:generic_9x3"),
                        obj -> obj instanceof String
                );

        // 3. 容器槽位计算算法
        USE_VISUAL_GRID = BUILDER
                .comment("If true, the mod will detect the player's inventory using a visual grid layout (highly compatible with modded UIs).",
                        "If false, it will use the standard object reference check (fast but may fail on some mods).")
                .define("useVisualGridDetection", false); // 默认推荐开启网格

        // 4. 按钮位置缓存配置
        BUTTON_POS_CACHE = BUILDER
                .comment("Cache for custom button positions. Format: 'menu_id:x,y'")
                .defineListAllowEmpty(
                        "buttonPosCache",
                        List.of(),
                        obj -> obj instanceof String
                );

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    // ==========================================
    // 自动拾取状态管理 (替代原来的 PickupState)
    // ==========================================

    /**
     * 获取当前自动拾取是否开启
     */
    public static boolean isOn() {
        try {
            return AUTO_PICKUP_ON.get();
        } catch (IllegalStateException e) {
            // 如果配置还没加载完就调用，默认返回 true 防止崩溃
            return true;
        }
    }

    /**
     * 设置自动拾取状态并保存到配置文件
     */
    public static void setOn(boolean state) {
        try {
            AUTO_PICKUP_ON.set(state);
            SPEC.save(); // 立即保存
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换状态并返回新状态
     */
    public static boolean toggle() {
        boolean newState = !isOn();
        setOn(newState);
        return newState;
    }

    // ==========================================
    // 白名单管理 (保留之前的逻辑)
    // ==========================================
    public static List<String> getWhitelist() {
        return new ArrayList<>(CONTAINER_WHITELIST.get());
    }

    public static boolean isInWhitelist(String id) {
        if (id == null) return false;
        return CONTAINER_WHITELIST.get().contains(id);
    }

    public static void addToWhitelist(String id) {
        List<String> currentList = getWhitelist();
        if (!currentList.contains(id)) {
            currentList.add(id);
            CONTAINER_WHITELIST.set(currentList);
            SPEC.save();
        }
    }

    public static void removeFromWhitelist(String id) {
        List<String> currentList = getWhitelist();
        if (currentList.contains(id)) {
            currentList.remove(id);
            CONTAINER_WHITELIST.set(currentList);
            SPEC.save();
        }
    }

    // ==========================================
    // Slot
    // ==========================================
    public static boolean isUseVisualGrid() {
        try {
            return USE_VISUAL_GRID.get();
        } catch (IllegalStateException e) {
            // 如果配置还没加载完就调用，默认返回 true 防止崩溃
            return true;
        }
    }

    /**
     * 设置是否使用“视觉网格法”。
     * @param useVisualGrid true 开启网格法，false 开启对象引用法。
     */
    public static void setUseVisualGrid(boolean useVisualGrid) {
        try {
            USE_VISUAL_GRID.set(useVisualGrid);
            SPEC.save(); // 立即保存
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 快速切换识别模式。
     * @return 切换后的新状态（true=网格法，false=对象引用法）
     */
    public static boolean toggleSlotDetection() {
        boolean newState = !isUseVisualGrid();
        setUseVisualGrid(newState);
        return newState;
    }

    // ==========================================
    // 按钮位置缓存管理
    // ==========================================
    public static void loadButtonPositions() {
        BUTTON_POS_MAP.clear();
        for (String entry : BUTTON_POS_CACHE.get()) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                String menuId = parts[0];
                String[] coords = parts[1].split(",");
                if (coords.length == 2) {
                    try {
                        BUTTON_POS_MAP.put(menuId, new int[]{Integer.parseInt(coords[0]), Integer.parseInt(coords[1])});
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public static void saveButtonPositions() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : BUTTON_POS_MAP.entrySet()) {
            list.add(entry.getKey() + ":" + entry.getValue()[0] + "," + entry.getValue()[1]);
        }
        BUTTON_POS_CACHE.set(list);
        SPEC.save();
    }

    /**
     * 获取自定义按钮偏移量。
     * @return 包含 [offsetX, offsetY] 的数组，如果没有自定义过则返回 null
     */
    public static int[] getButtonOffset(String menuId) {
        if (BUTTON_POS_MAP.isEmpty()) loadButtonPositions();
        return BUTTON_POS_MAP.get(menuId);
    }

    // todo ,偷懒,先直接存x,y了,
    // 注意,这里每次重启都会清空配置文件里的x,y.
    /**
     * 保存自定义按钮偏移量。
     */
    public static void setButtonOffset(String menuId, int offsetX, int offsetY) {
        BUTTON_POS_MAP.put(menuId, new int[]{offsetX, offsetY});
        saveButtonPositions();
    }
}