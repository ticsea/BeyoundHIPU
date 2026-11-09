package me.afk.beyondhipu.utils;

import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;

public class WhiteListUtil {
    public static String getScreenKey(AbstractContainerScreen<?> containerScreen) {
        // 1. 防御性判空
        if (containerScreen == null) {
            return null;
        }

        // 2. 提前拦截原版玩家背包（无需查注册表，且原版 InventoryMenu 会抛异常）
        if (containerScreen.getMenu() instanceof InventoryMenu) {
            return null;
        }

        // 3. 尝试获取 MenuType
        try {
            MenuType<?> menuType = containerScreen.getMenu().getType();
            ResourceLocation menuId = BuiltInRegistries.MENU.getKey(menuType);
            if (menuId != null) {
                return menuId.toString();
            }
        } catch (UnsupportedOperationException e) {
            // 兜底：某些特殊模组容器没有实现 getType()
            return null;
        }

        return null;
    }
}
