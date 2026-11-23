package com.example;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("template")
public class TemplateMod {
    public static final Logger LOGGER = LoggerFactory.getLogger("template");
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.11";

    // See https://docs.neoforged.net/docs/gettingstarted/modfiles#javafml-and-mod
    public TemplateMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Hello NeoForge World!");

        //? if !release
        LOGGER.warn("I'm still a template!");
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