package me.afk.beyondhipu;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 白名单配置
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONTAINER_WHITELIST;

    // 自动拾取开关状态
    private static final ForgeConfigSpec.BooleanValue AUTO_PICKUP_ON;

    // 调试模式状态（不写入文件，重启游戏重置）
    public static boolean isDebugPrintMode = false;

    static {
        BUILDER.comment("Configuration for HaveItPickup").push("general");

        // 1. 自动拾取开关默认值设为 true
        AUTO_PICKUP_ON = BUILDER
                .comment("Whether the auto pickup is enabled by default.")
                .define("autoPickupOn", true);

        // 2. 白名单配置
        CONTAINER_WHITELIST = BUILDER
                .comment("List of container Menu IDs that will show the pickup button.")
                .defineListAllowEmpty(
                        "containerWhitelist",
                        List.of("minecraft:generic_9x6", "minecraft:generic_9x3"),
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
}