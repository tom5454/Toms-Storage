package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class MergedStorage implements Storage<ItemVariant> {
	private Set<Storage<ItemVariant>> dupCheck = new HashSet<>();
	public List<Storage<ItemVariant>> parts = new ArrayList<>();
	private boolean iterating;

	public MergedStorage() {
	}

	public Collection<Storage<ItemVariant>> getStorages() {
		return parts;
	}

	public void add(Storage<ItemVariant> storage) {
		if(storage == this)return;
		if(storage instanceof MergedStorage str) {
			str.parts.forEach(this::add);
			return;
		}
		if(dupCheck.add(storage))
			this.parts.add(storage);
	}

	public void clear() {
		parts.clear();
		dupCheck.clear();
	}

	@Override
	public boolean supportsInsertion() {
		for (Storage<ItemVariant> part : parts) {
			if (part.supportsInsertion()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		long amount = 0;

		try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
			for (Storage<ItemVariant> part : parts) {
				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					amount += part.insert(resource, maxAmount - amount, transferTransaction);
					transferTransaction.commit();
				}
				if (amount == maxAmount) break;
			}

			iterationTransaction.commit();
		}

		return amount;
	}

	@Override
	public boolean supportsExtraction() {
		for (Storage<ItemVariant> part : parts) {
			if (part.supportsExtraction()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notNegative(maxAmount);
		long amount = 0;

		try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
			for (Storage<ItemVariant> part : parts) {
				try (Transaction transferTransaction = iterationTransaction.openNested()) {
					amount += part.extract(resource, maxAmount - amount, transferTransaction);
					transferTransaction.commit();
				}
				if (amount == maxAmount) break;
			}
			iterationTransaction.commit();
		}

		return amount;
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		if(iterating) {
			new Throwable("Recursive storage access").printStackTrace();
			return Collections.emptyIterator();
		}
		return new CombinedIterator(transaction);
	}

	/**
	 * The combined iterator for multiple storages.
	 */
	private class CombinedIterator implements Iterator<StorageView<ItemVariant>>, Transaction.CloseCallback {
		boolean open = true;
		final TransactionContext transaction;
		final Iterator<Storage<ItemVariant>> partIterator = parts.iterator();
		// Always holds the next StorageView<T>, except during next() while the iterator is being advanced.
		Iterator<? extends StorageView<ItemVariant>> currentPartIterator = null;

		CombinedIterator(TransactionContext transaction) {
			this.transaction = transaction;
			advanceCurrentPartIterator();
			transaction.addCloseCallback(this);
		}

		@Override
		public boolean hasNext() {
			return open && currentPartIterator != null && currentPartIterator.hasNext();
		}

		@Override
		public StorageView<ItemVariant> next() {
			if (!open) {
				throw new NoSuchElementException("The transaction for this iterator was closed.");
			}

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
				this.currentPartIterator = partIterator.next().iterator(transaction);

				if (this.currentPartIterator.hasNext()) {
					break;
				}
			}
			iterating = false;
		}

		@Override
		public void onClose(TransactionContext transaction, Transaction.Result result) {
			// As soon as the transaction is closed, this iterator is not valid anymore.
			open = false;
		}
	}
}