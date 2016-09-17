package mrriegel.pipes.block;

import mrriegel.limelib.helper.InvHelper;
import mrriegel.pipes.tile.TileItemPipe;
import mrriegel.pipes.tile.TilePipeBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockItemPipe extends BlockPipeBase {

	public BlockItemPipe() {
		super("itemPipe");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileItemPipe();
	}

	@Override
	protected Class<? extends TilePipeBase> getTile() {
		return TileItemPipe.class;
	}

	@Override
	protected boolean validTile(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
		return InvHelper.hasItemHandler(worldIn, pos, facing);
	}

}
