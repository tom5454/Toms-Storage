package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.block.AbstractInventoryHopperBlock;
import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.InventorySlot;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFaceReference;

public class BasicInventoryHopperBlockEntity extends AbstractInventoryHopperBlockEntity implements Clearable {
	private static final ItemPredicate ACCEPT_ALL = s -> true;

	private enum WaitState {
		NONE,
		WAITING_FOR_SOURCE_CHANGE,
		RESCAN_SOURCE
	}

	private ItemStack filter = ItemStack.EMPTY;
	private int cooldown;
	private long topChange, bottomChange;
	private WaitState waitState = WaitState.NONE;
	private ItemPredicate filterPred;
	private InventorySlot topSlot;

	public BasicInventoryHopperBlockEntity(BlockPos pos, BlockState state) {
		super(Content.basicInvHopperBE.get(), pos, state);
	}

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
		super.saveAdditional(compound, provider);
		ItemStack is = getFilter();
		if (!is.isEmpty())
			compound.put("Filter", is.save(provider, new CompoundTag()));
	}

	@Override
	public void loadAdditional(CompoundTag nbtIn, HolderLookup.Provider provider) {
		super.loadAdditional(nbtIn, provider);
		this.filter = ItemStack.parseOptional(provider, nbtIn.getCompound("Filter"));
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
		if (this.filter.isEmpty())filterPred = null;
		else if (this.filter.getItem() instanceof IItemFilter i) {
			filterPred = i.createFilter(BlockFaceReference.touching(level, worldPosition, getBlockState().getValue(AbstractInventoryHopperBlock.FACING)), filter);
		} else {
			filterPred = s -> ItemStack.isSameItemSameComponents(s.getStack(), filter);
		}
		resetSourceState();
		setChanged();
	}

	public ItemStack getFilter() {
		return filter;
	}

	@Override
	public void updateServer() {
		if(!filter.isEmpty() && filterPred == null)setFilter(filter);//update predicate
		Config.BasicHopperSettings settings = Config.get().basicHopperSettings();
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
			resetSourceState();
		} else if (waitState == WaitState.WAITING_FOR_SOURCE_CHANGE) {
			scheduleCooldown(settings.idleCooldown());
			return;
		}

		IInventoryChangeTracker bt = bottom.tracker();
		long b = bt.getChangeTracker(level);
		if (bottomChange != b) {
			bottomChange = b;
		}

		boolean topWasNull = topSlot == null;
		if(hasFilter)filterPred.updateState();
		if (topSlot == null || waitState == WaitState.RESCAN_SOURCE) {
			topSlot = tt.findSlotAfter(topSlot, hasFilter ? filterPred : ACCEPT_ALL, false, true);
			waitState = WaitState.NONE;
		}

		if (topSlot == null) {
			waitForSourceChange(topWasNull ? settings.idleCooldown() : settings.retryCooldown());
			return;
		}

		ItemStack is = topSlot.getStack();
		if (is.isEmpty()) {
			waitForSourceRescan(settings.retryCooldown());
			return;
		}
		StoredItemStack st = new StoredItemStack(is);
		if(hasFilter && !filterPred.test(st)) {
			waitForSourceRescan(settings.retryCooldown());
			return;
		}

		int moved = transferItems(bt, settings.transferAmount(), hasFilter);
		if (moved > 0) {
			waitState = WaitState.NONE;
			scheduleCooldown(settings.transferCooldown());
		} else {
			waitForSourceRescan(settings.retryCooldown());
		}
	}

	private int transferItems(IInventoryChangeTracker bottomTracker, int transferBudget, boolean filtered) {
		int moved = 0;
		while (topSlot != null && moved < transferBudget) {
			ItemStack stack = topSlot.getStack();
			if (stack.isEmpty()) {
				topSlot = null;
				break;
			}

			StoredItemStack stored = new StoredItemStack(stack);
			if (filtered && !filterPred.test(stored)) {
				topSlot = null;
				break;
			}

			InventorySlot bottomSlot = bottomTracker.findSlotDest(stored);
			if (bottomSlot == null)break;

			int requested = Math.min(transferBudget - moved, stack.getCount());
			int transferred = topSlot.transferToAmount(requested, bottomSlot);
			if (transferred <= 0)break;

			moved += transferred;
			if (topSlot.getStack().isEmpty()) {
				topSlot = null;
				break;
			}
		}
		return moved;
	}

	private void resetSourceState() {
		waitState = WaitState.NONE;
		topSlot = null;
	}

	private void waitForSourceChange(int nextCooldown) {
		waitState = WaitState.WAITING_FOR_SOURCE_CHANGE;
		topSlot = null;
		scheduleCooldown(nextCooldown);
	}

	private void waitForSourceRescan(int nextCooldown) {
		waitState = WaitState.RESCAN_SOURCE;
		scheduleCooldown(nextCooldown);
	}

	private void scheduleCooldown(int nextCooldown) {
		cooldown = Math.max(1, nextCooldown);
	}

	@Override
	public void clearContent() {
		setFilter(ItemStack.EMPTY);
	}
}
