package mrriegel.pipes;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class TransferItem implements INBTSerializable<NBTTagCompound> {
	public Pair<BlockPos, EnumFacing> out, in;
	public Vec3d current;
	public ItemStack stack;
	public boolean blocked = false;
	public Path path;

	public TransferItem() {
	}

	public void readFromNBT(NBTTagCompound compound) {
		stack = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("stack"));
		out = new ImmutablePair<BlockPos, EnumFacing>(BlockPos.fromLong(compound.getLong("outpos")), EnumFacing.values()[compound.getInteger("outface")]);
		in = new ImmutablePair<BlockPos, EnumFacing>(BlockPos.fromLong(compound.getLong("inpos")), EnumFacing.values()[compound.getInteger("inface")]);
		current = new Vec3d(compound.getDouble("xx"), compound.getDouble("yy"), compound.getDouble("zz"));
		blocked = compound.getBoolean("blocked");
		path=new Path();
		path.deserializeNBT(compound.getCompoundTag("path"));
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
		compound.setTag("path", path.serializeNBT());
		return compound;
	}

	public TransferItem(Pair<BlockPos, EnumFacing> out, Pair<BlockPos, EnumFacing> in, Vec3d current, ItemStack stack) {
		this.out = out;
		this.in = in;
		this.current = current;
		this.stack = stack;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((current == null) ? 0 : current.hashCode());
		result = prime * result + ((in == null) ? 0 : in.hashCode());
		result = prime * result + ((out == null) ? 0 : out.hashCode());
		result = prime * result + ((stack == null) ? 0 : stack.toString().hashCode() + (stack.hasTagCompound()?stack.getTagCompound().hashCode():0));
		return result;
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
		if (current == null) {
			if (other.current != null)
				return false;
		} else if (!current.equals(other.current))
			return false;
		if (in == null) {
			if (other.in != null)
				return false;
		} else if (!in.equals(other.in))
			return false;
		if (out == null) {
			if (other.out != null)
				return false;
		} else if (!out.equals(other.out))
			return false;
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.isItemEqual(other.stack))
			return false;
		return true;
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
