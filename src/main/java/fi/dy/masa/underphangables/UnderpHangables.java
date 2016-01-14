package fi.dy.masa.underphangables;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptableRemoteVersions="*")
public class UnderpHangables
{
    @Instance(Reference.MOD_ID)
    public static UnderpHangables instance;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(new AttackEntityEventHandler());
    }
}
