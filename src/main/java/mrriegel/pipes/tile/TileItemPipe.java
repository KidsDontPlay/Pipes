package mrriegel.pipes.tile;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mrriegel.limelib.helper.InvHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.StackHelper;
import mrriegel.limelib.util.Utils;
import mrriegel.pipes.TransferItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TileItemPipe extends TilePipeBase {

	protected List<TransferItem> items = Lists.newArrayList();
	protected Map<EnumFacing, Setting> settings = Maps.newHashMap();
	{
		for (EnumFacing f : EnumFacing.VALUES) {
			settings.put(f, new Setting());
		}
	}

	@Override
	public void update() {
		super.update();
		for (EnumFacing f : EnumFacing.VALUES)
			if (outs.get(f))
				startTransfer(f);
		moveItems();
	}

	void moveItems() {
		// if (true)
		// return;
		Iterator<TransferItem> it = items.listIterator();
		while (it.hasNext()) {
			TransferItem item = it.next();
			if (item.toRemove && !item.getCurrentPos().equals(pos)) {
				// System.out.println("remove: " + pos);
				// System.out.println(item.current);
				// System.out.println(item.getCurrentPos());
				// System.out.println(worldObj.getBlockState(item.getCurrentPos()).getBlock());
				if (worldObj.getTileEntity(item.getCurrentPos()) instanceof TileItemPipe) {
					item.getCurrentPipe(worldObj).items.add(item);
				} else if (InvHelper.hasItemHandler(worldObj, item.getCurrentPos(), item.in.getRight().getOpposite())) {
					IItemHandler inv = InvHelper.getItemHandler(worldObj, item.getCurrentPos(), item.in.getRight().getOpposite());
					ItemStack rest = ItemHandlerHelper.insertItemStacked(inv, item.stack, false);
					System.out.println("rest: " + rest);
					if(rest!=null)
						//add to blocked items
						;
				}
				item.toRemove = false;
				item.centerReached = false;
				// item.getCurrentPipe(worldObj).buildNetwork();
				// item.getCurrentPipe(worldObj).sync();
				it.remove();
				markForSync();
			}
		}
		for (TransferItem item : items) {
			if (worldObj.getTileEntity(item.getCurrentPos()) instanceof TileItemPipe
			// && !worldObj.isRemote
			) {
				item.move(worldObj, getSpeed());
			}
		}
	}

	public List<TransferItem> getItems() {
		return items;
	}

	public Map<EnumFacing, Setting> getSettings() {
		return settings;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		items = Lists.newArrayList();
		List<NBTTagCompound> i = NBTHelper.getTagList(compound, "items");
		for (NBTTagCompound n : i) {
			TransferItem t = new TransferItem();
			t.deserializeNBT(n);
			items.add(t);
		}
		settings = Maps.newHashMap();
		for (EnumFacing f : EnumFacing.VALUES) {
			Setting s = new Setting();
			if (compound.hasKey(f.toString() + "setting"))
				s.deserializeNBT(compound.getCompoundTag(f.toString() + "setting"));
			settings.put(f, s);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		List<NBTTagCompound> i = Lists.newArrayList();
		for (TransferItem t : items)
			i.add(t.serializeNBT());
		NBTHelper.setTagList(compound, "items", i);
		for (EnumFacing f : EnumFacing.VALUES) {
			compound.setTag(f.toString() + "setting", settings.get(f).serializeNBT());
		}
		return super.writeToNBT(compound);
	}

	public short getFrequence() {
		return Boost.defaultFrequence;
	}

	public double getSpeed() {
		return Boost.defaultSpeed;
	}

	public int getStackSize() {
		return Boost.defaultStackSize;
	}

	@Override
	public List<ItemStack> getDroppingItems() {
		List<ItemStack> lis = Lists.newArrayList();
		for (TransferItem t : items)
			lis.add(t.stack);
		for (EnumFacing f : EnumFacing.VALUES)
			lis.addAll(settings.get(f).blockedItems);
		return lis;
	}

	boolean startTransfer(EnumFacing outFacing) {
		if (!worldObj.isRemote && worldObj.getTotalWorldTime() % getFrequence() == 0 && worldObj.isBlockPowered(pos) && settings.get(outFacing).needRedstone) {
			EnumFacing face = outFacing;
			if (!worldObj.getChunkFromBlockCoords(pos.offset(face)).isLoaded())
				return false;
			IItemHandler inv = InvHelper.getItemHandler(worldObj.getTileEntity(pos.offset(face)), face.getOpposite());
			if (inv == null)
				return false;
			List<Pair<BlockPos, EnumFacing>> destis = Lists.newArrayList(targets);
			switch (settings.get(outFacing).mode) {
			case FF:
				Collections.sort(destis, new Comparator<Pair<BlockPos, EnumFacing>>() {
					@Override
					public int compare(Pair<BlockPos, EnumFacing> o1, Pair<BlockPos, EnumFacing> o2) {
						int d1 = getGraph().getShortestPath(o2.getLeft()).size();
						int d2 = getGraph().getShortestPath(o1.getLeft()).size();
						return Integer.compare(d1, d2);
					}
				});
				break;
			case NF:
				Collections.sort(destis, new Comparator<Pair<BlockPos, EnumFacing>>() {
					@Override
					public int compare(Pair<BlockPos, EnumFacing> o1, Pair<BlockPos, EnumFacing> o2) {
						int d1 = getGraph().getShortestPath(o2.getLeft()).size();
						int d2 = getGraph().getShortestPath(o1.getLeft()).size();
						return Integer.compare(d2, d1);
					}
				});
				break;
			case RA:
				Collections.shuffle(destis);
				break;
			case RR:
				if (settings.get(outFacing).lastInsertIndex + 1 >= destis.size())
					settings.get(outFacing).lastInsertIndex = 0;
				else
					settings.get(outFacing).lastInsertIndex++;
				List<Pair<BlockPos, EnumFacing>> k = Lists.newArrayList();
				for (int i = 0; i < destis.size(); i++) {
					k.add(destis.get((settings.get(outFacing).lastInsertIndex + i) % destis.size()));
				}
				destis = Lists.newArrayList(k);
				break;
			}
			for (Pair<BlockPos, EnumFacing> pair : destis) {
				TileItemPipe destPipe = (TileItemPipe) worldObj.getTileEntity(pair.getLeft());
				Setting destSetting = destPipe.settings.get(pair.getRight());
				IItemHandler dest = InvHelper.getItemHandler(worldObj.getTileEntity(pair.getLeft().offset(pair.getRight())), pair.getRight().getOpposite());
				if (dest == null)
					continue;
				for (int i = 0; i < inv.getSlots(); i++) {
					ItemStack send = inv.extractItem(i, getStackSize(), true);
					if (send == null || !settings.get(outFacing).blockedItems.isEmpty() || !settings.get(outFacing).canTransfer(send) || !destSetting.blockedItems.isEmpty() || !destSetting.canTransfer(send))
						continue;
					int canInsert = InvHelper.canInsert(dest, send);
					if (canInsert <= 0)
						continue;
					int missing = Integer.MAX_VALUE;
					if (destSetting.stockNum > 0) {
						int contains = 0;
						for (int j = 0; j < dest.getSlots(); j++) {
							if (dest.getStackInSlot(j) != null && destSetting.equal(dest.getStackInSlot(j), send)) {
								contains += dest.getStackInSlot(j).stackSize;
							}
						}
						for (TransferItem t : items) {
							if (t.in.equals(pair) && destSetting.equal(t.stack, send)) {
								contains += t.stack.stackSize;
							}
						}
						missing = destSetting.stockNum - contains;
					}
					if (missing <= 0)
						continue;
					canInsert = Math.min(canInsert, missing);
					ItemStack x = inv.extractItem(i, canInsert, true);
					if (x != null) {
						Vec3d vec = null;
						switch (outFacing) {
						case DOWN:
							vec = new Vec3d(.5, 0.05, .5);
							break;
						case EAST:
							vec = new Vec3d(0.95, .5, .5);
							break;
						case NORTH:
							vec = new Vec3d(.5, .5, 0.05);
							break;
						case SOUTH:
							vec = new Vec3d(.5, .5, 0.95);
							break;
						case UP:
							vec = new Vec3d(.5, 0.95, .5);
							break;
						case WEST:
							vec = new Vec3d(0.05, .5, .5);
							break;
						}
						TransferItem tr = new TransferItem(Pair.of(pos, outFacing), Pair.of(pair.getLeft(), pair.getRight()), vec.addVector(getX(), getY(), getZ()), x);
						items.add(tr);
						inv.extractItem(i, canInsert, false);
						markForSync();
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean isOpaque() {
		return blockType != null ? blockType.getClass().getName().contains("opaqu") : false;
	}

	public static class Setting implements INBTSerializable<NBTTagCompound> {
		Mode mode = Mode.NF;
		ItemStackHandler filter = new ItemStackHandler(12);
		boolean needRedstone = true, whitelist = false, oreDict = false, meta = true, nbt = false, mod = false;
		Deque<ItemStack> blockedItems = new ArrayDeque<ItemStack>();
		int lastInsertIndex, stockNum;

		@Override
		public NBTTagCompound serializeNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("mode", mode.ordinal());
			nbt.setTag("filter", filter.serializeNBT());
			nbt.setBoolean("needRedstone", needRedstone);
			nbt.setBoolean("whitelist", whitelist);
			nbt.setBoolean("oreDict", oreDict);
			nbt.setBoolean("meta", meta);
			nbt.setBoolean("nbt", this.nbt);
			nbt.setBoolean("mod", mod);
			NBTHelper.setItemStackList(nbt, "blocked", Lists.newArrayList(blockedItems));
			nbt.setInteger("last", lastInsertIndex);
			nbt.setInteger("stock", stockNum);
			return nbt;
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			mode = Mode.VALUES[nbt.getInteger("mode")];
			filter = new ItemStackHandler(12);
			filter.deserializeNBT(nbt.getCompoundTag("filter"));
			needRedstone = nbt.getBoolean("needRedstone");
			whitelist = nbt.getBoolean("whitelist");
			oreDict = nbt.getBoolean("oreDict");
			meta = nbt.getBoolean("meta");
			this.nbt = nbt.getBoolean("nbt");
			mod = nbt.getBoolean("mod");
			blockedItems = new ArrayDeque<ItemStack>(NBTHelper.getItemStackList(nbt, "blocked"));
			lastInsertIndex = nbt.getInteger("last");
			stockNum = nbt.getInteger("stock");
		}

		public boolean canTransfer(ItemStack stack) {
			if (stack == null)
				return false;
			for (int i = 0; i < filter.getSlots(); i++) {
				ItemStack s = filter.getStackInSlot(i);
				if (s != null && equal(stack, s))
					return whitelist;
			}
			return !whitelist;
		}

		public boolean equal(ItemStack stack1, ItemStack stack2) {
			if (oreDict && StackHelper.equalOreDict(stack1, stack2))
				return true;
			if (mod && Utils.getModName(stack1.getItem()).equals(Utils.getModName(stack2.getItem())))
				return true;
			if (nbt && !ItemStack.areItemStackTagsEqual(stack1, stack2))
				return false;
			if (meta && stack1.getItemDamage() != stack2.getItemDamage())
				return false;
			return stack1.getItem() == stack2.getItem();
		}

	}

	static class Boost {
		public static final short defaultFrequence = 40;
		public static final double defaultSpeed = .02;
		public static final int defaultStackSize = 1;

		public final short frequence;
		public final double speed;
		public final int stackSize;

		public Boost(short frequence, double speed, int stackSize) {
			this.frequence = frequence;
			this.speed = speed;
			this.stackSize = stackSize;
		}

	}

	public enum Mode {
		NF("Nearest first"), FF("Farthest first"), RA("Random"), RR("Round Robin");
		String text;

		public static final Mode[] VALUES = values();

		Mode(String text) {
			this.text = text;
		}

		public Mode next() {
			return values()[(this.ordinal() + 1) % values().length];
		}
	}

}
