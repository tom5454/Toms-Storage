package com.tom.storagemod.inventory;

import java.util.Collections;
import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.minecraft.world.item.ItemStack;

import com.google.common.base.Predicates;

import com.tom.storagemod.inventory.IInventoryAccess.IChangeNotifier;

public class InventorySlot implements Storage<ItemVariant> {
	private StorageView<ItemVariant> view;
	private Storage<ItemVariant> parent;
	private IChangeNotifier ch;

	public InventorySlot(Storage<ItemVariant> parent, StorageView<ItemVariant> h, IChangeNotifier notif) {
		this.parent = parent;
		this.ch = notif;
		this.view = h;
	}

	public ItemStack getStack() {
		return view == null ? ItemStack.EMPTY : view.getResource().toStack((int) view.getAmount());
	}

	public ItemStack insert(ItemStack stack) {
		try (Transaction tr = Transaction.openOuter()) {
			long ins = insert(ItemVariant.of(stack), stack.getCount(), tr);
			if (ins == 0)return stack;
			tr.commit();
			notifyChange();
			if (ins < stack.getCount()) {
				return stack.copyWithCount((int) (stack.getCount() - ins));
			} else {
				return ItemStack.EMPTY;
			}
		}
	}

	public ItemStack extract(int i) {
		if (view == null)return ItemStack.EMPTY;
		try (Transaction tr = Transaction.openOuter()) {
			var r = view.getResource();
			long ex = view.extract(r, i, tr);
			if (ex > 0) {
				tr.commit();
				notifyChange();
				return r.toStack((int) ex);
			}
		}
		return ItemStack.EMPTY;
	}

	public boolean transferTo(int i, InventorySlot to) {
		try (Transaction tr = Transaction.openOuter()) {
			long t = StorageUtil.move(this, to, Predicates.alwaysTrue(), i, tr);
			if (t > 0) {
				notifyChange();
				to.notifyChange();
				tr.commit();
				return true;
			}
		}
		return false;
	}

	public StorageView<ItemVariant> getView() {
		return view;
	}

	@SuppressWarnings("unchecked")
	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if (view instanceof Storage slot) {
			return slot.insert(resource, maxAmount, transaction);
		} else {
			return StorageUtil.tryInsertStacking(parent, resource, maxAmount, transaction);
		}
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		return view == null ? 0 : view.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		if (view == null)return Collections.emptyIterator();
		return TransferApiImpl.singletonIterator(view);
	}

	private void notifyChange() {
		ch.onSlotChanged(this);
	}
}
