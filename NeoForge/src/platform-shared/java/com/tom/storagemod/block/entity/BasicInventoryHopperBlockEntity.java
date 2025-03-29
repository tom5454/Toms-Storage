package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.AbstractInventoryHopperBlock;
import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.InventorySlot;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFaceReference;

public class BasicInventoryHopperBlockEntity extends AbstractInventoryHopperBlockEntity {
	private ItemStack filter = ItemStack.EMPTY;
	private int cooldown;
	private long topChange, bottomChange;
	public int waiting = 0;
	private ItemPredicate filterPred;
	private InventorySlot topSlot;

	public BasicInventoryHopperBlockEntity(BlockPos pos, BlockState state) {
		super(Content.basicInvHopperBE.get(), pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
		ItemStack is = getFilter();
		if (!is.isEmpty())
			compound.put("Filter", is.save(provider, new CompoundTag()));
	}

	@Override
	public void loadAdditional(CompoundTag nbtIn, HolderLookup.Provider provider) {
		super.loadAdditional(nbtIn, provider);
		this.filter = ItemStack.parse(provider, nbtIn.getCompoundOrEmpty("Filter")).orElse(ItemStack.EMPTY);
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
		if (this.filter.isEmpty())filterPred = null;
		else if (this.filter.getItem() instanceof IItemFilter i) {
			filterPred = i.createFilter(BlockFaceReference.touching(level, worldPosition, getBlockState().getValue(AbstractInventoryHopperBlock.FACING)), filter);
		} else {
			filterPred = s -> ItemStack.isSameItemSameComponents(s.getStack(), filter);
		}
		waiting = 0;
		setChanged();
	}

	public ItemStack getFilter() {
		return filter;
	}

	@Override
	public void updateServer() {
		if(!filter.isEmpty() && filterPred == null)setFilter(filter);//update predicate
		BlockState state = level.getBlockState(worldPosition);
		Direction facing = state.getValue(AbstractInventoryHopperBlock.FACING);
		IInventoryAccess top = topCache.getAccess(level, worldPosition.relative(facing.getOpposite()));
		IInventoryAccess bottom = bottomCache.getAccess(level, worldPosition.relative(facing));
		boolean topNet = topCache.isNetwork();
		if (!topCache.isValid() || !bottomCache.isValid())return;
		if (!topNet && !bottomCache.isNetwork())return;
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		boolean hasFilter = filterPred != null;
		if (topNet && !hasFilter)return;
		if (!isEnabled())return;

		IInventoryChangeTracker tt = top.tracker();
		long t = tt.getChangeTracker(level);
		if (topChange != t) {
			topChange = t;
			waiting = 0;
			topSlot = null;
		} else cooldown = 4;
		if (waiting == 1)return;

		IInventoryChangeTracker bt = bottom.tracker();
		long b = bt.getChangeTracker(level);
		if (bottomChange != b) {
			bottomChange = b;
			waiting = 0;
		} else cooldown = 4;
		if (waiting == 2)return;

		boolean topWasNull = topSlot == null;
		if(hasFilter)filterPred.updateState();
		if (topSlot == null || waiting == 3)
			topSlot = tt.findSlotAfter(topSlot, hasFilter ? filterPred : (s -> true), false, true);

		if (topSlot == null) {
			if(topWasNull) {
				waiting = 1;
				cooldown = 10;
			} else {
				cooldown = 4;
			}
			return;
		}

		ItemStack is = topSlot.getStack();
		if (is.isEmpty()) {
			waiting = 3;
			cooldown = 1;
			return;
		}
		StoredItemStack st = new StoredItemStack(is);
		if(hasFilter && !filterPred.test(st)) {
			waiting = 3;
			cooldown = 1;
			return;
		}

		InventorySlot bottomSlot = bt.findSlotDest(st);
		if (bottomSlot == null) {
			waiting = 3;
			cooldown = 10;
			return;
		}

		if (topSlot.transferTo(1, bottomSlot)) {
			cooldown = 10;
		} else {
			waiting = 3;
			cooldown = 10;
		}
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), filter);
	}
}
