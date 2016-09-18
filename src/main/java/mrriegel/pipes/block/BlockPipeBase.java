package mrriegel.pipes.block;

import java.util.List;

import javax.annotation.Nullable;

import mrriegel.limelib.block.CommonBlockContainer;
import mrriegel.pipes.proxy.ClientProxy;
import mrriegel.pipes.tile.TilePipeBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class BlockPipeBase extends CommonBlockContainer<TilePipeBase> {

	public static final IProperty<Connect> NORTH = PropertyEnum.<Connect> create("north", Connect.class);
	public static final IProperty<Connect> SOUTH = PropertyEnum.<Connect> create("south", Connect.class);
	public static final IProperty<Connect> WEST = PropertyEnum.<Connect> create("west", Connect.class);
	public static final IProperty<Connect> EAST = PropertyEnum.<Connect> create("east", Connect.class);
	public static final IProperty<Connect> UP = PropertyEnum.<Connect> create("up", Connect.class);
	public static final IProperty<Connect> DOWN = PropertyEnum.<Connect> create("down", Connect.class);

	public static final BiMap<IProperty<Connect>, EnumFacing> map = HashBiMap.create(6);

	public BlockPipeBase(String name) {
		super(Material.IRON, name);
		setCreativeTab(ClientProxy.tab);
		map.put(NORTH, EnumFacing.NORTH);
		map.put(SOUTH, EnumFacing.SOUTH);
		map.put(WEST, EnumFacing.WEST);
		map.put(EAST, EnumFacing.EAST);
		map.put(UP, EnumFacing.UP);
		map.put(DOWN, EnumFacing.DOWN);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState blockState) {
		return false;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer) {
		return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, NORTH, SOUTH, WEST, EAST, UP, DOWN);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		TilePipeBase tile = (TilePipeBase) worldIn.getTileEntity(pos);
		return state.withProperty(NORTH, getConnect(worldIn, pos, EnumFacing.NORTH)).withProperty(SOUTH, getConnect(worldIn, pos, EnumFacing.SOUTH)).withProperty(WEST, getConnect(worldIn, pos, EnumFacing.WEST)).withProperty(EAST, getConnect(worldIn, pos, EnumFacing.EAST)).withProperty(UP, getConnect(worldIn, pos, EnumFacing.UP)).withProperty(DOWN, getConnect(worldIn, pos, EnumFacing.DOWN));
	}

	public Connect getConnect(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
		TilePipeBase tile = (TilePipeBase) worldIn.getTileEntity(pos);
		if (tile != null && !tile.validConnection(facing))
			return Connect.NULL;
		if (getTile().isInstance(worldIn.getTileEntity(pos.offset(facing))) && ((TilePipeBase) worldIn.getTileEntity(pos.offset(facing))).validConnection(facing.getOpposite()))
			return Connect.PIPE;
		else if (validTile(worldIn, pos.offset(facing), facing.getOpposite()))
			if (tile != null && tile.getOuts().get(facing))
				return Connect.TILEOUT;
			else
				return Connect.TILE;
		return Connect.NULL;
	}

	protected abstract boolean validTile(IBlockAccess worldIn, BlockPos pos, EnumFacing facing);

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		EnumFacing f = getFace(hitX, hitY, hitZ);
		TilePipeBase tile = (TilePipeBase) worldIn.getTileEntity(pos);
		if (new ItemStack(Items.STICK).isItemEqual(playerIn.inventory.getCurrentItem())) {
			if (f != null) {
				tile.getValids().put(f, false);
				if (worldIn.getTileEntity(pos.offset(f)) instanceof TilePipeBase)
					((TilePipeBase) worldIn.getTileEntity(pos.offset(f))).getValids().put(f.getOpposite(), false);
			} else {
				if (state.getValue(map.inverse().get(side)) == Connect.NULL && !((TilePipeBase) worldIn.getTileEntity(pos)).getValids().get(side)) {
					tile.getValids().put(side, true);
					if (worldIn.getTileEntity(pos.offset(side)) instanceof TilePipeBase) {
						((TilePipeBase) worldIn.getTileEntity(pos.offset(side))).getValids().put(side.getOpposite(), true);
					}
				}
			}
		} else if (new ItemStack(Items.REDSTONE).isItemEqual(playerIn.inventory.getCurrentItem())) {
			if (f != null) {
				tile.getOuts().put(f, !tile.getOuts().get(f));
			}
		} else {
			// tile.buildNetwork();
			// for (BlockPos p : tile.ends)
			// worldIn.setBlockState(p.up(),
			// Blocks.STAINED_GLASS.getDefaultState());
		}
		worldIn.markBlockRangeForRenderUpdate(pos.add(1, 1, 1), pos.add(-1, -1, -1));
		if (tile != null)
			tile.buildNetwork();
		System.out.println(getActualState(state, worldIn, pos));
		return super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}

	private EnumFacing getFace(float hitX, float hitY, float hitZ) {
		if (!center(hitY) && !center(hitZ))
			if (hitX < .25F)
				return EnumFacing.WEST;
			else if (hitX > .75F)
				return EnumFacing.EAST;
		if (!center(hitY) && !center(hitX))
			if (hitZ < .25F)
				return EnumFacing.NORTH;
			else if (hitZ > .75F)
				return EnumFacing.SOUTH;
		if (!center(hitX) && !center(hitZ))
			if (hitY < .25F)
				return EnumFacing.DOWN;
			else if (hitY > .75F)
				return EnumFacing.UP;
		return null;
	}

	private boolean center(float foo) {
		return foo > .25f && foo < .25f;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		state = getActualState(state, worldIn, pos);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875));
		if (state.getValue(DOWN) != Connect.NULL)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.3125, 0.6875));
		if (state.getValue(UP) != Connect.NULL)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125, 0.6875, 0.3125, 0.6875, 1, 0.6875));
		if (state.getValue(WEST) != Connect.NULL)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.0, 0.3125, 0.3125, 0.3125, 0.6875, 0.6875));
		if (state.getValue(EAST) != Connect.NULL)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.6875, 0.3125, 0.3125, 1, 0.6875, 0.6875));
		if (state.getValue(NORTH) != Connect.NULL)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125, 0.3125, 0, 0.6875, 0.6875, 0.3125));
		if (state.getValue(SOUTH) != Connect.NULL)
			addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(0.3125, 0.3125, 0.6875, 0.6875, 0.6875, 1));
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = getActualState(state, source, pos);
		double f = 0.3125;
		double f1 = 0.6875;
		double f2 = 0.3125;
		double f3 = 0.6875;
		double f4 = 0.3125;
		double f5 = 0.6875;
		if (state.getValue(NORTH) != Connect.NULL)
			f2 = 0;
		if (state.getValue(SOUTH) != Connect.NULL)
			f3 = 1;
		if (state.getValue(WEST) != Connect.NULL)
			f = 0;
		if (state.getValue(EAST) != Connect.NULL)
			f1 = 1;
		if (state.getValue(DOWN) != Connect.NULL)
			f4 = 0;
		if (state.getValue(UP) != Connect.NULL)
			f5 = 1;
		return new AxisAlignedBB(f, f4, f2, f1, f5, f3);
	}

	public static enum Connect implements IStringSerializable {
		NULL("null"), PIPE("pipe"), TILE("tile"), TILEOUT("tileout");
		String name;

		private Connect(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}
	}

}
