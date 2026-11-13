package me.afk.beyondhipu.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // 磁盘缓存列表
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOUNDS_CACHE;
    // 内存高速缓存
    private static final Map<String, int[]> BOUNDS_MAP = new HashMap<>();

    static {
        BUILDER.comment("Server-side cache for container bounds").push("server_cache");
        BOUNDS_CACHE = BUILDER
                .comment("Cached container bounds. Format: 'menu_id:x1,y1,x2,y2'")
                .defineListAllowEmpty("containerBounds", List.of(), obj -> obj instanceof String);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static void loadBounds() {
        BOUNDS_MAP.clear();
        for (String entry : BOUNDS_CACHE.get()) {
            String[] parts = entry.split(":");
            if (parts.length == 2) {
                String[] coords = parts[1].split(",");
                if (coords.length == 4) {
                    try {
                        BOUNDS_MAP.put(parts[0], new int[]{
                                Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
                                Integer.parseInt(coords[2]), Integer.parseInt(coords[3])
                        });
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public static void saveBounds() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : BOUNDS_MAP.entrySet()) {
            int[] b = entry.getValue();
            list.add(entry.getKey() + ":" + b[0] + "," + b[1] + "," + b[2] + "," + b[3]);
        }
        BOUNDS_CACHE.set(list);
        SPEC.save();
    }

    public static int[] getBounds(String menuId) {
        if (BOUNDS_MAP.isEmpty()) loadBounds();
        return BOUNDS_MAP.get(menuId);
    }

    public static void setBounds(String menuId, int x1, int y1, int x2, int y2) {
        BOUNDS_MAP.put(menuId, new int[]{x1, y1, x2, y2});
        saveBounds();
    }
}