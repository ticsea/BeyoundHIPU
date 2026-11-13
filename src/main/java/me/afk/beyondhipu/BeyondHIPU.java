package me.afk.beyondhipu;

import me.afk.beyondhipu.config.ClientConfig;
import me.afk.beyondhipu.config.ServerConfig;
import net.minecraftforge.api.distmarker.Dist;
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
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
