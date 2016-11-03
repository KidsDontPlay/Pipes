package mrriegel.pipes.proxy;

import mrriegel.pipes.TileItemPipeRender;
import mrriegel.pipes.init.ModBlocks;
import mrriegel.pipes.init.ModItems;
import mrriegel.pipes.tile.TileItemPipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {


	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModBlocks.initClient();
		ModItems.initClient();
		ClientRegistry.bindTileEntitySpecialRenderer(TileItemPipe.class, new TileItemPipeRender());
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}

}
