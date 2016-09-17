package mrriegel.pipes.tile;

import net.minecraft.util.EnumFacing;
import mrriegel.limelib.tile.CommonTile;

public abstract class TilePipeBase extends CommonTile{
	
	public abstract boolean validConnection(EnumFacing facing);

}
