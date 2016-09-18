package mrriegel.pipes.tile;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import mrriegel.limelib.tile.CommonTile;
import mrriegel.pipes.block.BlockPipeBase;
import mrriegel.pipes.block.BlockPipeBase.Connect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TilePipeBase extends CommonTile implements ITickable {

	protected Map<EnumFacing, Boolean> valids = Maps.newHashMap(), outs = Maps.newHashMap();
	protected boolean needsRefresh = false;
	public Set<BlockPos> pipes, ends;

	{
		for (EnumFacing f : EnumFacing.VALUES) {
			valids.put(f, true);
			outs.put(f, false);
		}
	}

	public boolean validConnection(EnumFacing facing) {
		return valids.get(facing);
	};

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		for (EnumFacing f : EnumFacing.VALUES) {
			valids.put(f, compound.hasKey(f.toString() + "valid") ? compound.getBoolean(f.toString() + "valid") : true);
			outs.put(f, compound.hasKey(f.toString() + "out") ? compound.getBoolean(f.toString() + "out") : false);
		}
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for (EnumFacing f : EnumFacing.VALUES) {
			compound.setBoolean(f.toString() + "valid", valids.get(f));
			compound.setBoolean(f.toString() + "out", outs.get(f));
		}
		return super.writeToNBT(compound);
	}

	public Map<EnumFacing, Boolean> getValids() {
		return valids;
	}

	public Map<EnumFacing, Boolean> getOuts() {
		return outs;
	}

	@Override
	public void update() {
		if (pipes == null || ends == null)
			needsRefresh = true;
		if (needsRefresh) {
			needsRefresh = false;
			buildNetwork();
		}
	}

	public void buildNetwork() {
		pipes = Sets.newHashSet();
		ends = Sets.newHashSet();
		addPipes(pos);
		pipes.forEach(new Consumer<BlockPos>() {
			@Override
			public void accept(BlockPos t) {
				if (hasCons(t))
					ends.add(t);
			}
		});
		ends.remove(pos);
	}

	private boolean hasCons(BlockPos p) {
		BlockPipeBase block = (BlockPipeBase) worldObj.getBlockState(p).getBlock();
		for (EnumFacing f : EnumFacing.VALUES)
			if (block.getConnect(worldObj, p, f) == Connect.TILE)
				return true;
		return false;
	}

	private void addPipes(BlockPos pos) {
		for (EnumFacing f : EnumFacing.VALUES) {
			BlockPos nei = pos.offset(f);
			BlockPipeBase block = (BlockPipeBase) worldObj.getBlockState(pos).getBlock();
			Chunk chunk = worldObj.getChunkFromBlockCoords(nei);
			if (chunk == null || !chunk.isLoaded() || block.getConnect(worldObj, pos, f) != Connect.PIPE)
				continue;
			if (worldObj.getTileEntity(nei) instanceof TileItemPipe && !pipes.contains(nei)) {
				pipes.add(nei);
				addPipes(nei);
			}

		}
	}

	public void setNeedsRefresh(boolean needsRefresh) {
		this.needsRefresh = needsRefresh;
	}

}
