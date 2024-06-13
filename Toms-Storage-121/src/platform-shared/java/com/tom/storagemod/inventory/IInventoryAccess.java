package com.tom.storagemod.inventory;

import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.tom.storagemod.inventory.filter.ItemPredicate;

public interface IInventoryAccess extends IChangeTrackerAccess {

	default ItemStack pullMatchingStack(ItemStack st, long max) {
		InventorySlot slot = null;
		var tr = tracker();
		StoredItemStack s = new StoredItemStack(st, 1);
		ItemPredicate ip = s::equalItem;
		ItemStack ext = null;
		long ex = 0;
		while (ex < max && (slot = tr.findSlotAfter(slot, ip, false, false)) != null) {
			ItemStack e = slot.extract((int) (max - ex));
			if (ext == null)ext = e;
			else ext.grow(e.getCount());
			ex += e.getCount();
		}
		return ext != null ? ext : ItemStack.EMPTY;
	}

	default ItemStack pushStack(ItemStack stack) {
		var tr = tracker();
		StoredItemStack s = new StoredItemStack(stack);
		InventorySlot slot = null;
		while (!stack.isEmpty() && (slot = tr.findSlotDestAfter(slot, s, false)) != null) {
			stack = slot.insert(stack);
			s.setCount(stack.getCount());
		}
		return stack;
	}

	int getFreeSlotCount();
	int getSlotCount();

	@Deprecated
	Object get();

	@SuppressWarnings("unchecked")
	default <T> T getPlatformHandler() {
		return (T) get();
	}

	default void markInvalid() {}

	public static interface IMultiThreadedTracker<A, B> {
		A prepForOffThread(Level level);
		B processOffThread(A array);
		long finishOffThreadProcess(Level level, B ct);
	}

	public static interface IChangeNotifier {
		void onSlotChanged(InventorySlot slot);
	}

	public static interface IInventoryChangeTracker {
		long getChangeTracker(Level level);

		Stream<StoredItemStack> streamWrappedStacks(boolean parallel);
		long countItems(StoredItemStack filter);
		InventorySlot findSlot(ItemPredicate filter, boolean findEmpty);
		InventorySlot findSlotAfter(InventorySlot slot, ItemPredicate filter, boolean findEmpty, boolean loop);
		InventorySlot findSlotDest(StoredItemStack forStack);
		InventorySlot findSlotDestAfter(InventorySlot slot, StoredItemStack forStack, boolean loop);

		public static class Delegate implements IInventoryChangeTracker, IMultiThreadedTracker<Object, Object> {
			private IInventoryChangeTracker delegate;

			@Override
			public Stream<StoredItemStack> streamWrappedStacks(boolean parallel) {
				return delegate.streamWrappedStacks(parallel);
			}

			@Override
			public long countItems(StoredItemStack filter) {
				return delegate.countItems(filter);
			}

			@Override
			public long getChangeTracker(Level level) {
				return delegate.getChangeTracker(level);
			}

			public void setDelegate(IInventoryChangeTracker delegate) {
				this.delegate = delegate;
			}

			@Override
			public InventorySlot findSlot(ItemPredicate filter, boolean findEmpty) {
				return delegate.findSlot(filter, findEmpty);
			}

			@Override
			public Object prepForOffThread(Level level) {
				if (delegate instanceof IMultiThreadedTracker t)
					return t.prepForOffThread(level);
				else
					return null;
			}

			@SuppressWarnings("unchecked")
			@Override
			public Object processOffThread(Object array) {
				return ((IMultiThreadedTracker<Object, Object>) delegate).processOffThread(array);
			}

			@SuppressWarnings("unchecked")
			@Override
			public long finishOffThreadProcess(Level level, Object ct) {
				return ((IMultiThreadedTracker<Object, Object>) delegate).finishOffThreadProcess(level, ct);
			}

			@Override
			public InventorySlot findSlotDest(StoredItemStack forStack) {
				return delegate.findSlotDest(forStack);
			}

			@Override
			public InventorySlot findSlotAfter(InventorySlot slot, ItemPredicate filter, boolean findEmpty, boolean loop) {
				return delegate.findSlotAfter(slot, filter, findEmpty, loop);
			}

			@Override
			public InventorySlot findSlotDestAfter(InventorySlot slot, StoredItemStack forStack, boolean loop) {
				return delegate.findSlotDestAfter(slot, forStack, loop);
			}
		}
	}

	public static interface IInventory {
		IInventoryAccess getInventoryAccess();
	}
}
