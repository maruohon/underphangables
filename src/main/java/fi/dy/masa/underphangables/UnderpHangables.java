package fi.dy.masa.underphangables;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
    acceptableRemoteVersions="*", acceptedMinecraftVersions = "1.11")
public class UnderpHangables
{
    @Mod.Instance(Reference.MOD_ID)
    public static UnderpHangables instance;

    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(new AttackEntityEventHandler());
    }
}
