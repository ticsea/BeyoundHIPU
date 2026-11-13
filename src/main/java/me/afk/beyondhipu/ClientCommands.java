package me.afk.beyondhipu;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.afk.beyondhipu.config.ClientConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = BeyondHIPU.MODID, value = Dist.CLIENT)
public class ClientCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("beyondhipu")
                .then(Commands.literal("auto")
                        .then(Commands.argument("auto", BoolArgumentType.bool())
                                // 自动提示 true / false
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("true", "false"), builder))
                                .executes(context -> {
                                    // 获取玩家输入的布尔值
                                    boolean auto = BoolArgumentType.getBool(context, "auto");

                                    // 保存到配置
                                    ClientConfig.setOn(auto);

                                    // 根据输入的值，选择对应的翻译键
                                    String key = ClientConfig.isOn() ? "command.beyondhipu.auto.on" : "command.beyondhipu.auto.off";
                                    context.getSource().sendSuccess(() -> Component.translatable(key), false);

                                    return 1;
                                })
                        )
                        .executes(ClientCommands::executeToggleAuto)
                )
                .then(Commands.literal("print")
                        .executes(ClientCommands::executeTogglePrint)
                )
                // /beyondhipu add <id>
                .then(Commands.literal("add")
                        .then(Commands.argument("containerId", StringArgumentType.greedyString())
                                .suggests(SUGGEST_MENU_TYPES) // 自动补全所有模组的 MenuType
                                .executes(ClientCommands::executeAdd)
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("containerId", StringArgumentType.greedyString())
                                .suggests(suggestWhitelist())
                                .executes(ClientCommands::executeRemove)
                        )
                )
                .then(Commands.literal("useVisualGrid")
                        .then(Commands.argument("useVisualGrid", BoolArgumentType.bool())
                                // 自动提示 true / false
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(List.of("true", "false"), builder))
                                .executes(ClientCommands::executeSetMode)
                        )
                        // 不带参数时，直接翻转当前状态（Toggle）
                        .executes(context -> {
                            boolean newMode = ClientConfig.toggleSlotDetection();
                            String key = newMode ? "command.haveitpickup.mode.visual_grid" : "command.haveitpickup.mode.object_reference";
                            context.getSource().sendSuccess(() -> Component.translatable(key), false);
                            return 1;
                        })
                )
        );
    }

    // 执行 /haveitpickup mode <true|false>
    private static int executeSetMode(CommandContext<CommandSourceStack> context) {
        // 获取玩家输入的布尔值
        boolean useVisualGrid = BoolArgumentType.getBool(context, "useVisualGrid");

        // 保存到配置
        ClientConfig.setUseVisualGrid(useVisualGrid);

        // 根据输入的值，选择对应的翻译键
        String key = useVisualGrid ? "command.haveitpickup.mode.visual_grid" : "command.haveitpickup.mode.object_reference";
        context.getSource().sendSuccess(() -> Component.translatable(key), false);

        return 1;
    }

    private static int executeToggleAuto(CommandContext<CommandSourceStack> context) {
        ClientConfig.toggle();

        String key = ClientConfig.isOn() ? "command.beyondhipu.auto.on" : "command.beyondhipu.auto.off";
        context.getSource().sendSuccess(() -> Component.translatable(key), false);
        return 1;
    }

    private static int executeTogglePrint(CommandContext<CommandSourceStack> context) {
        ClientConfig.isDebugPrintMode = !ClientConfig.isDebugPrintMode;

        String key = ClientConfig.isDebugPrintMode ? "command.beyondhipu.print.on" : "command.beyondhipu.print.off";
        context.getSource().sendSuccess(() -> Component.translatable(key), false);
        return 1;
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context) {
        String id = StringArgumentType.getString(context, "containerId");

        // 1. 安全地解析 ID
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);

        // 2. 校验解析结果和注册表存在性 (使用 ForgeRegistries 避免废弃警告)
        if (resourceLocation == null || !BuiltInRegistries.MENU.containsKey(resourceLocation)) {
            context.getSource().sendFailure(Component.translatable("command.beyondhipu.add.invalid", id));
            return 0;
        }

        // 3. 存入白名单
        if (!ClientConfig.isInWhitelist(id)) {
            ClientConfig.addToWhitelist(id);
            context.getSource().sendSuccess(() -> Component.translatable("command.beyondhipu.add.success", id), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("command.beyondhipu.add.exists", id));
            return 0;
        }
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) {
        String id = StringArgumentType.getString(context, "containerId");

        if (ClientConfig.isInWhitelist(id)) {
            ClientConfig.removeFromWhitelist(id);
            // 带参数的翻译文本
            context.getSource().sendSuccess(() -> Component.translatable("command.beyondhipu.remove.success", id), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable("command.beyondhipu.remove.fail", id));
            return 0;
        }
    }

    // 补全当前白名单里的 ID（用于 remove）
    private static SuggestionProvider<CommandSourceStack> suggestWhitelist() {
        return (context, builder) -> {
            List<String> whitelist = ClientConfig.getWhitelist();
            return SharedSuggestionProvider.suggest(whitelist, builder);
        };
    }

    // 补全所有已注册的 MenuType ID
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_MENU_TYPES = (context, builder) -> {
        List<String> ids = new ArrayList<>();
        for (ResourceLocation rl : BuiltInRegistries.MENU.keySet()) {
            ids.add(rl.toString());
        }
        return SharedSuggestionProvider.suggest(ids, builder);
    };
}