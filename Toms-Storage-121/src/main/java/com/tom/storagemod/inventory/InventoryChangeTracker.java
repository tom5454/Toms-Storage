package com.tom.storagemod.inventory;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.inventory.IInventoryAccess.IChangeNotifier;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.IInventoryAccess.IMultiThreadedTracker;
import com.tom.storagemod.inventory.filter.ItemPredicate;

public class InventoryChangeTracker implements IInventoryChangeTracker, IMultiThreadedTracker<ItemStack[], Long>, IChangeNotifier {
	public static final InventoryChangeTracker NULL = new InventoryChangeTracker(null);
	private final WeakReference<IItemHandler> itemHandler;
	private long lastUpdate, lastChange;
	private StoredItemStack[] lastItems = new StoredItemStack[0];

	public InventoryChangeTracker(IItemHandler itemHandler) {
		this.itemHandler = new WeakReference<>(itemHandler);
	}

	@Override
	public long getChangeTracker(Level level) {
		IItemHandler h = itemHandler.get();
		if (h == null)return 0L;
		if (lastUpdate != level.getGameTime()) {
			int slots = h.getSlots();
			if (lastItems.length != slots)lastItems = new StoredItemStack[slots];
			boolean change = false;

			for (int i = 0;i<slots;i++) {
				ItemStack is = h.getStackInSlot(i);
				change |= updateChange(i, is);
			}
			if (change)lastChange = System.nanoTime();
			lastUpdate = level.getGameTime();
		}
		return lastChange;
	}

	protected boolean checkFilter(ItemStack stack) {
		return checkFilter(new StoredItemStack(stack, stack.getCount()));
	}

	protected boolean checkFilter(StoredItemStack stack) {
		return true;
	}

	protected int getCount(ItemStack is) {
		return is.getCount();
	}

	protected IItemHandler getSlotHandler(IItemHandler def) {
		return def;
	}

	private boolean updateChange(int i, ItemStack is) {
		if (!is.isEmpty() && checkFilter(is)) {
			int cnt = getCount(is);
			if (lastItems[i] == null || !ItemStack.isSameItemSameComponents(lastItems[i].getStack(), is)) {
				lastItems[i] = new StoredItemStack(is);
				lastItems[i].setCount(cnt);
				return true;
			} else if(lastItems[i].getQuantity() != cnt) {
				lastItems[i].setCount(cnt);
				return true;
			}
		} else if (lastItems[i] != null) {
			lastItems[i] = null;
			return true;
		}
		return false;
	}

	@Override
	public Stream<StoredItemStack> streamWrappedStacks(boolean parallel) {
		IItemHandler h = itemHandler.get();
		if (h == null)return Stream.empty();
		return Arrays.stream(lastItems).filter(e -> e != null);
	}

	@Override
	public long countItems(StoredItemStack filter) {
		IItemHandler h = itemHandler.get();
		if (h == null)return 0;
		long c = 0;
		for (int i = 0; i < lastItems.length; i++) {
			StoredItemStack is = lastItems[i];
			if (is != null && is.equalItem(filter))c += is.getQuantity();
		}
		return c;
	}

	@Override
	public InventorySlot findSlot(ItemPredicate filter, boolean findEmpty) {
		IItemHandler h = itemHandler.get();
		if (h == null)return null;
		for (int i = 0; i < lastItems.length; i++) {
			StoredItemStack is = lastItems[i];
			if (is == null) {
				if (findEmpty)return new InventorySlot(getSlotHandler(h), this, i);
				continue;
			}
			if (filter.test(is))return new InventorySlot(getSlotHandler(h), this, i);
		}
		return null;
	}

	@Override
	public ItemStack[] prepForOffThread(Level level) {
		if (lastUpdate == level.getGameTime())return null;
		IItemHandler h = itemHandler.get();
		if (h == null)return null;
		int slots = h.getSlots();
		if (lastItems.length != slots)lastItems = new StoredItemStack[slots];
		ItemStack[] items = new ItemStack[slots];
		for (int i = 0;i<slots;i++) {
			items[i] = h.getStackInSlot(i);
		}
		return items;
	}

	@Override
	public Long processOffThread(ItemStack[] array) {
		int slots = array.length;
		boolean change = false;
		for (int i = 0;i<slots;i++) {
			ItemStack is = array[i];
			change |= updateChange(i, is);
		}
		if (change) return System.nanoTime();
		return lastChange;
	}

	@Override
	public long finishOffThreadProcess(Level level, Long ct) {
		if (ct == null)return lastChange;
		lastChange = ct;
		lastUpdate = level.getGameTime();
		return ct;
	}

	@Override
	public void onSlotChanged(InventorySlot slot) {
		if (lastItems.length > slot.getId() && updateChange(slot.getId(), slot.getStack())) {
			lastChange = System.nanoTime();
		}
	}

	@Override
	public InventorySlot findSlotDest(StoredItemStack forStack) {
		IItemHandler h = itemHandler.get();
		if (h == null)return null;
		if (!checkFilter(forStack))return null;
		for (int i = 0; i < lastItems.length; i++) {
			StoredItemStack is = lastItems[i];
			if (is == null && !h.isItemValid(i, forStack.getStack())) {
				continue;
			}
			if (is == null || is.equalItem(forStack)) {
				int rem = h.insertItem(i, forStack.getActualStack(), true).getCount();
				if (rem < forStack.getQuantity()) {
					return new InventorySlot(getSlotHandler(h), this, i);
				}
			}
		}
		return null;
	}

	@Override
	public InventorySlot findSlotAfter(InventorySlot slot, ItemPredicate filter, boolean findEmpty, boolean loop) {
		if (slot == null)return findSlot(filter, findEmpty);
		IItemHandler h = itemHandler.get();
		if (h == null || slot.getHandler() != h)return null;
		if (h.getSlots() <= slot.getId() + 1)return loop ? findSlot(filter, findEmpty) : null;
		for (int i = slot.getId() + 1; i < lastItems.length; i++) {
			StoredItemStack is = lastItems[i];
			if (is == null) {
				if (findEmpty)return new InventorySlot(getSlotHandler(h), this, i);
				continue;
			}
			if (filter.test(is))return new InventorySlot(getSlotHandler(h), this, i);
		}
		return null;
	}

	@Override
	public InventorySlot findSlotDestAfter(InventorySlot slot, StoredItemStack forStack, boolean loop) {
		if (slot == null)return findSlotDest(forStack);
		IItemHandler h = itemHandler.get();
		if (h == null)return null;
		if (!checkFilter(forStack))return null;
		if (slot.getHandler() != h)return null;
		if (h.getSlots() <= slot.getId() + 1)return loop ? findSlotDest(forStack) : null;
		for (int i = slot.getId() + 1; i < lastItems.length; i++) {
			StoredItemStack is = lastItems[i];
			if (is == null && !h.isItemValid(i, forStack.getStack())) {
				continue;
			}
			if (is == null || is.equalItem(forStack)) {
				int rem = h.insertItem(i, forStack.getActualStack(), true).getCount();
				if (rem < forStack.getQuantity()) {
					return new InventorySlot(getSlotHandler(h), this, i);
				}
			}
		}
		return null;
	}
}
