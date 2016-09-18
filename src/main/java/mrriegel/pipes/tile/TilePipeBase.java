package mrriegel.pipes.tile;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import mrriegel.limelib.tile.CommonTile;

public class TilePipeBase extends CommonTile {

	protected Map<EnumFacing, Boolean> valids = Maps.newHashMap();
	protected boolean out = false, in = false;

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
		if (compound.hasKey("out"))
			out = compound.getBoolean("out");
		if (compound.hasKey("in"))
			in = compound.getBoolean("in");
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for (EnumFacing f : EnumFacing.VALUES)
			compound.setBoolean(f.toString(), valids.get(f));
		compound.setBoolean("out", out);
		compound.setBoolean("in", in);
		return super.writeToNBT(compound);
	}

	public Map<EnumFacing, Boolean> getValids() {
		return valids;
	}

	public boolean isOut() {
		bullshit
		must 6x out
		return out;
	}

	public boolean isIn() {
		return in;
	}

}
