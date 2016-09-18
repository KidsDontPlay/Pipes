package mrriegel.pipes.tile;

import java.util.Iterator;
import java.util.Set;

import mrriegel.pipes.block.BlockPipeBase;
import mrriegel.pipes.block.BlockPipeBase.Connect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import com.google.common.collect.Sets;

public class TileItemPipe extends TilePipeBase implements ITickable {

	private boolean needsRefresh = false;

	@Override
	public void update() {
		if (needsRefresh) {
			needsRefresh = false;
			buildNetwork();
		}
	}

	private void buildNetwork() {
		if (!isOut())
			return;
		Set<BlockPos> ends = Sets.newHashSet();
		addEnds(ends, pos);
		Iterator<BlockPos> it = ends.iterator();
		while (it.hasNext()) {
			BlockPos p = it.next();
			if (!hasCons(p))
				it.remove();

		}
	}

	private boolean hasCons(BlockPos p) {
		BlockPipeBase block = (BlockPipeBase) blockType;
		for (EnumFacing f : EnumFacing.VALUES)
			if (block.getConnect(worldObj, p, f) == Connect.TILE)
				return true;
		return false;
	}

	private void addEnds(Set<BlockPos> ends, BlockPos pos) {
		for (EnumFacing f : EnumFacing.VALUES) {
			BlockPos nei = pos.offset(f);
			BlockPipeBase block = (BlockPipeBase) blockType;
			Chunk chunk = worldObj.getChunkFromBlockCoords(nei);
			if (chunk == null || !chunk.isLoaded() || block.getConnect(worldObj, pos, f) != Connect.PIPE)
				continue;
			if (worldObj.getTileEntity(nei) instanceof TileItemPipe && !ends.contains(nei)) {
				ends.add(nei);
				addEnds(ends, nei);
			}

		}
	}

	public void setNeedsRefresh(boolean needsRefresh) {
		this.needsRefresh = needsRefresh;
	}

}
