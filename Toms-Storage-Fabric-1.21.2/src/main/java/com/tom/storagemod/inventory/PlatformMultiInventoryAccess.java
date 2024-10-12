package com.tom.storagemod.inventory;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class PlatformMultiInventoryAccess extends MultiInventoryAccess implements Storage<ItemVariant> {
	private boolean iterating;
	private boolean calling;

	@Override
	public Storage<ItemVariant> get() {
		return this;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		if(calling)return 0L;
		calling = true;
		long amount = 0;

		try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
			for (IInventoryAccess part : connected) {
				Storage<ItemVariant> h = part.getPlatformHandler();
				if (h == null)continue;
				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					amount += h.insert(resource, maxAmount - amount, transferTransaction);
					transferTransaction.commit();
				}
				if (amount == maxAmount) break;
			}

			iterationTransaction.commit();
		} finally {
			calling = false;
		}

		return amount;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		if(calling)return 0L;
		calling = true;
		long amount = 0;

		try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
			for (IInventoryAccess part : connected) {
				Storage<ItemVariant> h = part.getPlatformHandler();
				if (h == null)continue;
				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					amount += h.extract(resource, maxAmount - amount, transferTransaction);
					transferTransaction.commit();
				}
				if (amount == maxAmount) break;
			}
			iterationTransaction.commit();
		} finally {
			calling = false;
		}

		return amount;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		if(iterating) {
			new Throwable("Recursive storage access").printStackTrace();
			return Collections.emptyIterator();
		}
		return new MultiIterator();
	}

	/**
	 * The combined iterator for multiple storages.
	 */
	private class MultiIterator implements Iterator<StorageView<ItemVariant>> {
		final Iterator<IInventoryAccess> partIterator = connected.iterator();
		// Always holds the next StorageView<T>, except during next() while the iterator is being advanced.
		Iterator<? extends StorageView<ItemVariant>> currentPartIterator = null;

		MultiIterator() {
			advanceCurrentPartIterator();
		}

		@Override
		public boolean hasNext() {
			return currentPartIterator != null && currentPartIterator.hasNext();
		}

		@Override
		public StorageView<ItemVariant> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			StorageView<ItemVariant> returned = currentPartIterator.next();

			// Advance the current part iterator
			if (!currentPartIterator.hasNext()) {
				advanceCurrentPartIterator();
			}

			return returned;
		}

		private void advanceCurrentPartIterator() {
			iterating = true;
			while (partIterator.hasNext()) {
				Storage<ItemVariant> h = partIterator.next().getPlatformHandler();
				if (h == null)h = Storage.empty();
				this.currentPartIterator = h.iterator();

				if (this.currentPartIterator.hasNext()) {
					break;
				}
			}
			iterating = false;
		}
	}
}
