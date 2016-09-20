package mrriegel.pipes;

import java.util.List;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.util.Utils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class Path implements INBTSerializable<NBTTagCompound> {

	List<BlockPos> pos;

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTHelper.setLongList(nbt, "pos", Utils.getLongList(pos));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		pos = Utils.getBlockPosList(NBTHelper.getLongList(nbt, "pos"));
	}

}
