package mrriegel.pipes;

import java.util.List;

import mrriegel.pipes.tile.TileItemPipe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class TransferItem implements INBTSerializable<NBTTagCompound> {
	public Pair<BlockPos, EnumFacing> out, in;
	public Vec3d current;
	public ItemStack stack;
	public boolean blocked, centerReached, toRemove;
	public BlockPos next;
	private int id;

	public TransferItem() {
	}

	public void refreshNext(World world) {
		BlockPos now = getCurrentPos();
		if (next != null && !now.equals(next))
			return;
		if (next != null) {
			toRemove = true;
			System.out.println("remove");
		}
		TileItemPipe pipe = getCurrentPipe(world);
		// System.out.println(pipe+"  "+toRemove);
		List<BlockPos> path = pipe.getGraph().getShortestPath(in.getLeft());
		// toRemove=true;
		if (path.isEmpty()) {
			blocked = true;
			return;
		}
		if (path.size() > 1) {
			next = path.get(1);
			return;
		} else {
			next = in.getLeft().offset(in.getRight());
			return;
		}
	}

	public TileItemPipe getCurrentPipe(World world) {
		return (TileItemPipe) world.getTileEntity(getCurrentPos());
	}

	public BlockPos getCurrentPos() {
		return new BlockPos(current);
	}

	public void move(World world, double speed) {
		refreshNext(world);
		// System.out.println("rmove " + toRemove+'\t'+getCurrentPos());
		if (blocked) {
			if (!next.equals(getCurrentPos())) {
				blocked = false;
			}
		} else {
			// System.out.println("1+ "+current);
			if (!centerReached) {
				current = current.add(getVecToCenter().scale(speed / getVecToCenter().lengthVector()));
				if (inCenter(getCurrentPos())) {
					centerReached = true;
					getCurrentPipe(world).sync();
					BlockPos tmp = getCurrentPos();
//					current = new Vec3d(tmp.getX() + .5, tmp.getY() + .5, tmp.getZ() + .5);
				}
			} else {
				current = current.add(getVecToNextCenter().scale(speed / getVecToNextCenter().lengthVector()));
				if (!(world.getTileEntity(getCurrentPos()) instanceof TileItemPipe))
					toRemove = true;
			}
			// System.out.println("2+ "+current);
		}
	}

	Vec3d getVecToCenter() {
		BlockPos cur = getCurrentPos();
		Vec3d c = new Vec3d(cur.getX() + .5, cur.getY() + .5, cur.getZ() + .5);
		return new Vec3d(c.xCoord - current.xCoord, c.yCoord - current.yCoord, c.zCoord - current.zCoord);
	}

	Vec3d getVecToNextCenter() {
		BlockPos cur = next;
		Vec3d c = new Vec3d(cur.getX() + .5, cur.getY() + .5, cur.getZ() + .5);
		return new Vec3d(c.xCoord - current.xCoord, c.yCoord - current.yCoord, c.zCoord - current.zCoord);
	}

	boolean inCenter(BlockPos pos) {
		double offset = .01;
		Vec3d cen = new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
		return cen.distanceTo(current) < offset;

	}

	public void readFromNBT(NBTTagCompound compound) {
		stack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
		out = new ImmutablePair<BlockPos, EnumFacing>(BlockPos.fromLong(compound.getLong("outpos")), EnumFacing.values()[compound.getInteger("outface")]);
		in = new ImmutablePair<BlockPos, EnumFacing>(BlockPos.fromLong(compound.getLong("inpos")), EnumFacing.values()[compound.getInteger("inface")]);
		current = new Vec3d(compound.getDouble("xx"), compound.getDouble("yy"), compound.getDouble("zz"));
		blocked = compound.getBoolean("blocked");
		centerReached = compound.getBoolean("center");
		toRemove = compound.getBoolean("remove");
		id = compound.getInteger("id");
		if (compound.hasKey("next"))
			next = BlockPos.fromLong(compound.getLong("next"));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("stack", stack.writeToNBT(new NBTTagCompound()));
		compound.setLong("outpos", out.getLeft().toLong());
		compound.setLong("outface", out.getRight().ordinal());
		compound.setLong("inpos", in.getLeft().toLong());
		compound.setLong("inface", in.getRight().ordinal());
		compound.setDouble("xx", current.xCoord);
		compound.setDouble("yy", current.yCoord);
		compound.setDouble("zz", current.zCoord);
		compound.setBoolean("blocked", blocked);
		compound.setBoolean("center", centerReached);
		compound.setBoolean("remove", toRemove);
		compound.setInteger("id", id);
		if (next != null)
			compound.setLong("next", next.toLong());
		return compound;
	}

	public TransferItem(Pair<BlockPos, EnumFacing> out, Pair<BlockPos, EnumFacing> in, Vec3d current, ItemStack stack) {
		this.out = out;
		this.in = in;
		this.current = current;
		this.stack = stack;
		this.id = (int) (System.nanoTime() % Integer.MAX_VALUE);
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransferItem other = (TransferItem) obj;
		return id == other.id;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}

}
