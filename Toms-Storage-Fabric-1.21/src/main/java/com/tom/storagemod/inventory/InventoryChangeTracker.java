package com.tom.storagemod.inventory;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.tom.storagemod.inventory.IInventoryAccess.IChangeNotifier;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.filter.ItemPredicate;

public class InventoryChangeTracker implements IInventoryChangeTracker, IChangeNotifier {
	public static final InventoryChangeTracker NULL = new InventoryChangeTracker(null);
	private WeakReference<Storage<ItemVariant>> storage;
	private long lastUpdate, lastVersion, lastChange;
	private Map<StorageView<ItemVariant>, StoredItemStack> lastItems = new HashMap<>();

	public InventoryChangeTracker(Storage<ItemVariant> itemHandler) {
		storage = new WeakReference<>(itemHandler);
	}

	@Override
	public long getChangeTracker(Level level) {
		Storage<ItemVariant> h = storage.get();
		if (h == null)return 0L;
		if (lastUpdate != level.getGameTime()) {
			long ver = h.getVersion();
			if (ver != lastVersion) {
				boolean change = false;
				for (StorageView<ItemVariant> is : h) {
					change |= updateChange(is);
				}
				if (change)lastChange = System.nanoTime();
				lastUpdate = level.getGameTime();
				lastVersion = ver;
			}
		}
		return lastChange;
	}

	protected boolean checkFilter(StorageView<ItemVariant> stack) {
		return checkFilter(new StoredItemStack(stack.getResource().toStack(), stack.getAmount(), stack.getResource().hashCode()));
	}

	protected boolean checkFilter(StoredItemStack stack) {
		return true;
	}

	protected int getCount(StorageView<ItemVariant> is) {
		return (int) is.getAmount();
	}

	private boolean updateChange(StorageView<ItemVariant> iv) {
		StorageView<ItemVariant> uv = iv.getUnderlyingView();
		StoredItemStack li = lastItems.get(uv);
		if (!iv.isResourceBlank() && checkFilter(iv)) {
			int cnt = getCount(iv);
			if (li == null || !ItemStack.isSameItemSameComponents(li.getStack(), iv.getResource().toStack())) {
				lastItems.put(uv, new StoredItemStack(iv.getResource().toStack(), cnt, iv.getResource().hashCode()));
				return true;
			} else if(li.getQuantity() != cnt) {
				li.setCount(cnt);
				return true;
			}
		} else if (li != null) {
			lastItems.put(uv, null);
			return true;
		}
		return false;
	}

	protected StorageView<ItemVariant> getSlotHandler(StorageView<ItemVariant> def) {
		return def;
	}

	protected Storage<ItemVariant> getSlotHandler(Storage<ItemVariant> def) {
		return def;
	}

	@Override
	public Stream<StoredItemStack> streamWrappedStacks(boolean parallel) {
		Storage<ItemVariant> h = storage.get();
		if (h == null)return Stream.empty();
		return lastItems.values().stream().filter(e -> e != null);
	}

	@Override
	public long countItems(StoredItemStack filter) {
		Storage<ItemVariant> h = storage.get();
		if (h == null)return 0;
		long c = 0;
		for (StoredItemStack is : lastItems.values()) {
			if (is != null && is.equalItem(filter))c += is.getQuantity();
		}
		return c;
	}

	@Override
	public InventorySlot findSlot(ItemPredicate filter, boolean findEmpty) {
		Storage<ItemVariant> h = storage.get();
		if (h == null)return null;
		for (var slot : h) {
			if (slot.isResourceBlank()) {
				if (findEmpty)return new InventorySlot(getSlotHandler(h), getSlotHandler(slot), this);
				continue;
			}
			ItemVariant iv = slot.getResource();
			if(filter.test(new StoredItemStack(iv.toStack(), slot.getAmount(), slot.hashCode()))) {
				try (Transaction tr = Transaction.openOuter()) {
					if (slot.extract(iv, 1, tr) == 1)
						return new InventorySlot(getSlotHandler(h), getSlotHandler(slot), this);
				}
			}
			continue;
		}
		return null;
	}

	@Override
	public InventorySlot findSlotAfter(InventorySlot slotIn, ItemPredicate filter, boolean findEmpty, boolean loop) {
		if (slotIn == null)return findSlot(filter, findEmpty);
		var s = findSlot(filter, findEmpty);
		return s == slotIn ? null : s;
	}

	@Override
	public InventorySlot findSlotDest(StoredItemStack forStack) {
		Storage<ItemVariant> h = storage.get();
		if (h == null)return null;
		if (!checkFilter(forStack))return null;
		ItemVariant iv = ItemVariant.of(forStack.getStack());
		try (Transaction tr = Transaction.openOuter()) {
			long c = h.insert(iv, forStack.getQuantity(), tr);
			if (c > 0)
				return new InventorySlot(getSlotHandler(h), null, this);
		}
		return null;
	}

	@Override
	public InventorySlot findSlotDestAfter(InventorySlot slotIn, StoredItemStack forStack, boolean loop) {
		if (slotIn == null)return findSlotDest(forStack);
		var s = findSlotDest(forStack);
		return s == slotIn ? null : s;
	}

	@Override
	public void onSlotChanged(InventorySlot slot) {
		if (slot.getView() != null && updateChange(slot.getView())) {
			lastChange = System.nanoTime();
		}
	}
}
