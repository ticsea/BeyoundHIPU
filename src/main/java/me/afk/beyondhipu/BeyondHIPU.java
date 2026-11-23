package me.afk.beyondhipu;

import me.afk.beyondhipu.config.ClientConfig;
import me.afk.beyondhipu.config.ServerConfig;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(BeyondHIPU.MOD_ID)
public class BeyondHIPU {
    public static final String MOD_ID = "beyondhipu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/"0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/"26.3";

    // See https://docs.neoforged.net/docs/gettingstarted/modfiles#javafml-and-mod
    public BeyondHIPU(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Hello NeoForge World!");

        //? if !release
        //LOGGER.warn("I'm still a template!");

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        //? if >=26.3
        modContainer.registerConfig(ModConfig.Type.SYNCED, ServerConfig.SPEC);
        //? else
        //modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String namespace, String path) {
        //? if <1.21 {
        /*return new Identifier(namespace, path);
        *///?} else
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}