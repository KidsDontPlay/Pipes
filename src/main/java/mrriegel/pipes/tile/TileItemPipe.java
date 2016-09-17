package mrriegel.pipes.tile;

import net.minecraft.util.EnumFacing;

public class TileItemPipe extends TilePipeBase{

	@Override
	public boolean validConnection(EnumFacing facing) {
		return true;
	}

}
