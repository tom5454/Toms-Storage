package com.tom.storagemod.inventory;

import java.util.Set;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.inventory.filter.IFilter;
import com.tom.storagemod.util.Priority;
import com.tom.storagemod.util.Priority.IPriority;

public class PlatformFilteredInventoryAccess implements IInventoryAccess, IPriority, IItemHandler {
	private final IInventoryAccess acc;
	private final IFilter filter;
	private final InventoryChangeTracker tracker;

	public PlatformFilteredInventoryAccess(IInventoryAccess acc, IFilter filter) {
		this.acc = acc;
		this.filter = filter;
		this.tracker = new InventoryChangeTracker(acc.getPlatformHandler()) {

			@Override
			protected boolean checkFilter(StoredItemStack stack) {
				return filter.getItemPred().test(stack);
			}

			@Override
			protected int getCount(ItemStack is) {
				return filter.isKeepLast() ? is.getCount() - 1 : is.getCount();
			}

			@Override
			protected IItemHandler getSlotHandler(IItemHandler def) {
				return PlatformFilteredInventoryAccess.this;
			}

			@Override
			public ItemStack[] prepForOffThread(Level level) {
				filter.getItemPred().updateState();
				return super.prepForOffThread(level);
			}

			@Override
			public long getChangeTracker(Level level) {
				filter.getItemPred().updateState();
				return super.getChangeTracker(level);
			}
		};
	}

	@Override
	public IInventoryChangeTracker tracker() {
		return tracker;
	}

	@Override
	public ItemStack pushStack(ItemStack stack) {
		if (!test(stack))return stack;
		return IInventoryAccess.super.pushStack(stack);
	}

	@Override
	public ItemStack pullMatchingStack(ItemStack st, long max) {
		if (!test(st))return ItemStack.EMPTY;
		return IInventoryAccess.super.pullMatchingStack(st, max);
	}

	@Override
	public int getFreeSlotCount() {
		return acc.getFreeSlotCount();
	}

	@Override
	public int getSlotCount() {
		return acc.getSlotCount();
	}

	@Override
	public IItemHandler get() {
		return this;
	}

	@Override
	public Priority getPriority() {
		Priority pr = IPriority.get(acc);
		return pr.add(filter.getPriority());
	}

	@Override
	public int getSlots() {
		return getP().getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack is = getP().getStackInSlot(slot);
		if (!test(is))return ItemStack.EMPTY;
		if (filter.isKeepLast()) {
			is = is.copy();
			is.shrink(1);
		}
		return is;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (!test(stack))return stack;
		return getP().insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack is = getP().getStackInSlot(slot);
		if (!test(is))return ItemStack.EMPTY;
		if (filter.isKeepLast()) {
			amount = Math.min(amount, is.getCount() - 1);
		}
		return getP().extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return getP().getSlotLimit(slot);
	}

	private IItemHandler getP() {
		return acc.getPlatformHandler();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return test(stack) && getP().isItemValid(slot, stack);
	}

	private boolean test(ItemStack stack) {
		return filter.getItemPred().test(new StoredItemStack(stack, stack.getCount()));
	}

	@Override
	public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
		return acc.getRootHandler(dejaVu);
	}

	@Override
	public String toString() {
		return "Filtering: {" + acc + " by " + filter + "}";
	}

	public IFilter getFilter() {
		return filter;
	}

	public IInventoryAccess getActualInventory() {
		return acc;
	}
}
