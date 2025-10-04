package com.tom.storagemod.inventory;

import java.util.Set;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import com.tom.storagemod.inventory.filter.IFilter;
import com.tom.storagemod.util.Priority;
import com.tom.storagemod.util.Priority.IPriority;

public class PlatformFilteredInventoryAccess implements IInventoryAccess, IPriority, ResourceHandler<ItemResource> {
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
			protected long getCount(NeoStack is) {
				return filter.isKeepLast() ? is.count() - 1 : is.count();
			}

			@Override
			protected ResourceHandler<ItemResource> getSlotHandler(ResourceHandler<ItemResource> def) {
				return PlatformFilteredInventoryAccess.this;
			}

			@Override
			public NeoStack[] prepForOffThread(Level level) {
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
	public ResourceHandler<ItemResource> get() {
		return this;
	}

	@Override
	public Priority getPriority() {
		Priority pr = IPriority.get(acc);
		return pr.add(filter.getPriority());
	}

	@Override
	public int size() {
		return getP().size();
	}

	@Override
	public ItemResource getResource(int index) {
		var is = getP().getResource(index);
		if (!test(is))return ItemResource.EMPTY;
		return is;
	}

	@Override
	public long getAmountAsLong(int index) {
		var handler = getP();
		var is = getP().getResource(index);
		if (!test(is))return 0L;
		long cnt = handler.getAmountAsLong(index);
		if (filter.isKeepLast()) {
			return cnt - 1L;
		}
		return cnt;
	}

	@Override
	public int insert(int slot, ItemResource stack, int amount, TransactionContext transaction) {
		if (!test(stack))return 0;
		return getP().insert(slot, stack, amount, transaction);
	}

	@Override
	public int insert(ItemResource stack, int amount, TransactionContext transaction) {
		if (!test(stack))return 0;
		return getP().insert(stack, amount, transaction);
	}

	@Override
	public int extract(int slot, ItemResource stack, int amount, TransactionContext transaction) {
		if (!test(stack))return 0;
		if (filter.isKeepLast()) {
			long count = getP().getAmountAsLong(slot);
			amount = (int) Math.min(amount, count - 1);
		}
		return getP().extract(slot, stack, amount, transaction);
	}

	@Override
	public long getCapacityAsLong(int slot, ItemResource resource) {
		long cap = getP().getCapacityAsLong(slot, resource);
		if (filter.isKeepLast()) {
			return cap - 1L;
		}
		return cap;
	}

	private ResourceHandler<ItemResource> getP() {
		return acc.getPlatformHandler();
	}

	@Override
	public boolean isValid(int slot, ItemResource stack) {
		return test(stack) && getP().isValid(slot, stack);
	}

	private boolean test(ItemResource stack) {
		return filter.getItemPred().test(new StoredItemStack(stack.toStack(), 1));
	}

	private boolean test(ItemStack stack) {
		return filter.getItemPred().test(new StoredItemStack(stack, 1));
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
