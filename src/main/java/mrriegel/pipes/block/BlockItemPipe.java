package mrriegel.pipes.block;

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.pipes.Graph;
import mrriegel.pipes.TransferItem;
import mrriegel.pipes.tile.TileItemPipe;
import mrriegel.pipes.tile.TilePipeBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockItemPipe extends BlockPipeBase {

	public BlockItemPipe(String name) {
		super(name);
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

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileItemPipe tile = (TileItemPipe) worldIn.getTileEntity(pos);
		tile.buildNetwork();
		EnumFacing f = getFace(hitX, hitY, hitZ);
		if (f == null)
			return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
		Vec3d vec = null;
		switch (f) {
		case DOWN:
			vec = new Vec3d(.5, 0, .5);
			break;
		case EAST:
			vec = new Vec3d(1, .5, .5);
			break;
		case NORTH:
			vec = new Vec3d(.5, .5, 0);
			break;
		case SOUTH:
			vec = new Vec3d(.5, .5, 1);
			break;
		case UP:
			vec = new Vec3d(.5, 1, .5);
			break;
		case WEST:
			vec = new Vec3d(0, .5, .5);
			break;
		}
		TransferItem tr = new TransferItem(Pair.of(pos, f), Pair.of(BlockPos.ORIGIN, EnumFacing.DOWN), vec.addVector(pos.getX(), pos.getY(), pos.getZ()), new ItemStack(Blocks.CACTUS));
		
		// tile.getItems().add(tr);
		// System.out.println(tile.getItems());
//		if (!worldIn.isRemote && !tile.getEnds().isEmpty())
//			for (BlockPos p : tile.getGraph().getShortestPath(Lists.newArrayList(tile.getEnds()).get(new Random().nextInt(tile.getEnds().size()))))
//				worldIn.setBlockState(p.up(), Blocks.RED_SANDSTONE.getDefaultState());
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}

}
