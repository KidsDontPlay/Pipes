package mrriegel.pipes.tile;

import java.util.Map;
import java.util.Set;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.util.Utils;
import mrriegel.pipes.Graph;
import mrriegel.pipes.block.BlockPipeBase;
import mrriegel.pipes.block.BlockPipeBase.Connect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class TilePipeBase extends CommonTile implements ITickable {

	protected Map<EnumFacing, Boolean> valids = Maps.newHashMap(), outs = Maps.newHashMap();
	protected boolean needsRefresh = false;
	protected Set<BlockPos> pipes, ends;
	protected Set<Pair<BlockPos, EnumFacing>> targets;

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
			pipes = Sets.newHashSet(Utils.getBlockPosList(NBTHelper.getLongList(compound, "pipes")));
		}
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for (EnumFacing f : EnumFacing.VALUES) {
			compound.setBoolean(f.toString() + "valid", valids.get(f));
			compound.setBoolean(f.toString() + "out", outs.get(f));
			if (pipes != null)
				NBTHelper.setLongList(compound, "pipes", Utils.getLongList(Lists.newArrayList(pipes)));
		}
		return super.writeToNBT(compound);
	}

	public Map<EnumFacing, Boolean> getValids() {
		return valids;
	}

	public Map<EnumFacing, Boolean> getOuts() {
		return outs;
	}

	public Set<BlockPos> getPipes() {
		return pipes;
	}

	public Set<BlockPos> getEnds() {
		return ends;
	}

	public Set<Pair<BlockPos, EnumFacing>> getTargets() {
		return targets;
	}

	@Override
	public void update() {
		if (pipes == null || ends == null || targets == null)
			needsRefresh = true;
		// if (!needsRefresh) {
		// for (BlockPos p : pipes)
		// if
		// (!worldObj.getBlockState(p).getBlock().getClass().equals(blockType.getClass()))
		// {
		// needsRefresh = true;
		// break;
		// }
		// }
		if (needsRefresh) {
			buildNetwork();
			markDirty();
			needsRefresh = false;
		}
	}

	public void buildNetwork() {
		pipes = Sets.newHashSet();
		ends = Sets.newHashSet();
		targets = Sets.newHashSet();
		pipes.add(pos);
		addPipes(pos);
		if (!hasCons(pos))
			return;
		for (BlockPos t : pipes)
			if (hasCons(t))
				ends.add(t);
		for (BlockPos t : ends) {
			BlockPipeBase block = (BlockPipeBase) worldObj.getBlockState(t).getBlock();
			for (EnumFacing f : EnumFacing.VALUES)
				if (block.getConnect(worldObj, t, f) == Connect.TILE)
					targets.add(Pair.<BlockPos, EnumFacing> of(t, f));
		}
		sync();
	}

	private boolean hasCons(BlockPos p) {
		BlockPipeBase block = (BlockPipeBase) worldObj.getBlockState(p).getBlock();
		for (EnumFacing f : EnumFacing.VALUES)
			if (block.getConnect(worldObj, p, f) == Connect.TILE || block.getConnect(worldObj, p, f) == Connect.TILEOUT)
				return true;
		return false;
	}

	public Graph getGraph() {
		return new Graph(this);
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
		if (pipes != null)
			for (BlockPos p : pipes)
				if (worldObj.getTileEntity(p) instanceof TilePipeBase && !((TilePipeBase) worldObj.getTileEntity(p)).needsRefresh)
					((TilePipeBase) worldObj.getTileEntity(p)).setNeedsRefresh(true);
	}

	public abstract boolean isOpaque();

}
