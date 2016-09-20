package mrriegel.pipes.init;

import mrriegel.limelib.block.CommonBlock;
import mrriegel.pipes.block.BlockItemPipe;

public class ModBlocks {

	public static final CommonBlock itemPipe = new BlockItemPipe("itemPipe");

	public static void init() {
		itemPipe.registerBlock();
	}

	public static void initClient() {
		itemPipe.initModel();
	}

}
