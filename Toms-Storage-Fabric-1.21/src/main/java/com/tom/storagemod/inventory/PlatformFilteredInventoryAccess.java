package com.tom.storagemod.inventory;

import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.BlankVariantView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.google.common.collect.Iterators;

import com.tom.storagemod.inventory.InventoryImage.FabricStack;
import com.tom.storagemod.util.Priority;
import com.tom.storagemod.util.Priority.IPriority;

public class PlatformFilteredInventoryAccess extends FilteringStorage<ItemVariant> implements IInventoryAccess, IPriority {
	private BlankVariantView<ItemVariant> nullSlot = new BlankVariantView<>(ItemVariant.blank(), 0);
	private final IInventoryAccess acc;
	private final BlockFilter filter;
	private final InventoryChangeTracker tracker;

	public PlatformFilteredInventoryAccess(IInventoryAccess acc, BlockFilter filter) {
		super(acc::getPlatformHandler);
		this.acc = acc;
		this.filter = filter;
		this.tracker = new InventoryChangeTracker(acc.getPlatformHandler()) {

			@Override
			protected boolean checkFilter(StoredItemStack stack) {
				return filter.getItemPred().test(stack);
			}

			@Override
			protected int getCount(StorageView<ItemVariant> is) {
				return (int) (filter.isKeepLast() ? is.getAmount() - 1 : is.getAmount());
			}

			@Override
			protected int getCount(FabricStack is) {
				return (int) (filter.isKeepLast() ? is.count() - 1 : is.count());
			}

			@Override
			protected StorageView<ItemVariant> getSlotHandler(StorageView<ItemVariant> def) {
				if (def instanceof FilteringStorageView s && s.getOuter() == PlatformFilteredInventoryAccess.this)
					return def;
				return new FilteringStorageView(def);
			}

			@Override
			protected Storage<ItemVariant> getSlotHandler(Storage<ItemVariant> def) {
				return PlatformFilteredInventoryAccess.this;
			}

			@Override
			public InventoryImage prepForOffThread(Level level) {
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
		return acc.pushStack(stack);
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
	public Storage<ItemVariant> get() {
		return this;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(!canExtract(resource))return 0;
		if(filter.isKeepLast()) {
			Iterator<StorageView<ItemVariant>> itr = super.iterator();
			long amount = 0;
			while (itr.hasNext()) {
				StorageView<ItemVariant> sv = itr.next();
				if(!sv.isResourceBlank() && sv.getResource().equals(resource)) {
					long maxEx = Math.min(maxAmount - amount, sv.getAmount() - 1);
					amount += sv.extract(resource, maxEx, transaction);
					if (amount == maxAmount) break;
				}
			}
			return amount;
		}
		return backingStorage.get().extract(resource, maxAmount, transaction);
	}

	@Override
	protected boolean canInsert(ItemVariant resource) {
		return inFilter(resource, 1);
	}

	@Override
	protected boolean canExtract(ItemVariant resource) {
		return inFilter(resource, 1);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		return Iterators.transform(super.iterator(), sv -> {
			if(!inFilter(sv.getResource(), sv.getAmount()))return nullSlot;
			return filter.isKeepLast() ? new FilteringStorageView(sv) : sv;
		});
	}

	private boolean inFilter(ItemVariant resource, long cnt) {
		return filter.getItemPred().test(new StoredItemStack(resource.toStack(), cnt, resource.hashCode()));
	}

	@Override
	public Priority getPriority() {
		return filter.getPriority();
	}

	private boolean test(ItemStack stack) {
		return filter.getItemPred().test(new StoredItemStack(stack, stack.getCount()));
	}

	private class FilteringStorageView implements StorageView<ItemVariant> {
		private StorageView<ItemVariant> delegate;

		public FilteringStorageView(StorageView<ItemVariant> delegate) {
			this.delegate = delegate;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if(filter.isKeepLast()) {
				maxAmount = Math.min(delegate.getAmount() - 1, maxAmount);
				if(maxAmount < 1)return 0L;
			}
			return delegate.extract(resource, maxAmount, transaction);
		}

		@Override
		public boolean isResourceBlank() {
			return delegate.isResourceBlank() || (filter.isKeepLast() && delegate.getAmount() == 1L);
		}

		@Override
		public ItemVariant getResource() {
			return delegate.getResource();
		}

		@Override
		public long getAmount() {
			return filter.isKeepLast() ? delegate.getAmount() - 1L : delegate.getAmount();
		}

		@Override
		public long getCapacity() {
			return filter.isKeepLast() && !delegate.isResourceBlank() ? delegate.getCapacity() - 1L : delegate.getCapacity();
		}

		@Override
		public StorageView<ItemVariant> getUnderlyingView() {
			return delegate.getUnderlyingView();
		}

		public PlatformFilteredInventoryAccess getOuter() {
			return PlatformFilteredInventoryAccess.this;
		}
	}

	@Override
	public IInventoryAccess getRootHandler() {
		return acc.getRootHandler();
	}
}
