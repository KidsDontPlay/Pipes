package mrriegel.pipes.proxy;

import mrriegel.limelib.helper.IProxy;
import mrriegel.pipes.Pipes;
import mrriegel.pipes.handler.ConfigHandler;
import mrriegel.pipes.handler.GuiHandler;
import mrriegel.pipes.init.ModBlocks;
import mrriegel.pipes.init.ModItems;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy implements IProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
		ModItems.init();
		ModBlocks.init();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Pipes.instance, new GuiHandler());
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
	}

}
