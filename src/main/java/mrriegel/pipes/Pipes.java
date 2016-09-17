package mrriegel.pipes;

import mrriegel.limelib.helper.IProxy;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = Pipes.MODID, name = Pipes.MODNAME, version = Pipes.VERSION, dependencies = "required-after:LimeLib@[1.0.0,)")
public class Pipes {
	public static final String MODID = "pipes";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "Pipes";

	@Instance(Pipes.MODID)
	public static Pipes instance;
	public static Logger logger;

	@SidedProxy(clientSide = "mrriegel.pipes.proxy.ClientProxy", serverSide = "mrriegel.pipes.proxy.CommonProxy")
	public static IProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
		logger = event.getModLog();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}
}
