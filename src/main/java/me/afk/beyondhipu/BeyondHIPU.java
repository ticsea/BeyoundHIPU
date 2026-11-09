package me.afk.beyondhipu;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BeyondHIPU.MODID)
public class BeyondHIPU
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "beyondhipu";

    public BeyondHIPU(FMLJavaModLoadingContext context)
    {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }
    }
}
