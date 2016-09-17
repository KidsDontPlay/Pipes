package mrriegel.pipes.tile;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import mrriegel.limelib.tile.CommonTile;

public class TilePipeBase extends CommonTile {

	protected Map<EnumFacing, Boolean> valids = Maps.newHashMap();

	{
		for (EnumFacing f : EnumFacing.VALUES)
			valids.put(f, true);
	}

	public boolean validConnection(EnumFacing facing) {
		return valids.get(facing);
	};

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		for (EnumFacing f : EnumFacing.VALUES)
			valids.put(f, compound.hasKey(f.toString()) ? compound.getBoolean(f.toString()) : true);
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for (EnumFacing f : EnumFacing.VALUES)
			compound.setBoolean(f.toString(), valids.get(f));
		return super.writeToNBT(compound);
	}

}
