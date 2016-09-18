package mrriegel.pipes.proxy;

import mrriegel.pipes.Pipes;
import mrriegel.pipes.TileItemPipeRender;
import mrriegel.pipes.init.ModBlocks;
import mrriegel.pipes.init.ModItems;
import mrriegel.pipes.tile.TileItemPipe;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	public final static CreativeTabs tab = new CreativeTabs(Pipes.MODID) {

		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(Blocks.HOPPER);
		}

		@Override
		public String getTranslatedTabLabel() {
			return Pipes.MODNAME;
		}
	};

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
