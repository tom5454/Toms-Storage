package com.tom.storagemod.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import com.tom.storagemod.inventory.IInventoryAccess.IChangeNotifier;

public class InventorySlot {
	private final ResourceHandler<ItemResource> handler;
	private final IChangeNotifier notifier;
	private final int id;

	public InventorySlot(ResourceHandler<ItemResource> h, IChangeNotifier notifier, int i) {
		this.handler = h;
		this.notifier = notifier;
		this.id = i;
	}

	public ItemStack getStack() {
		return handler.getResource(id).toStack(handler.getAmountAsInt(id));
	}

	public NeoStack getNeoStack() {
		return new NeoStack(handler, id);
	}

	public ItemStack extract(int amount) {
		try (Transaction tx = Transaction.open(null)) {
			var resource = handler.getResource(id);
			int is = handler.extract(id, resource, amount, tx);
			if (is != 0)notifyChange();
			tx.commit();
			return resource.toStack(is);
		}
	}

	public ItemStack insert(ItemStack stack) {
		try (Transaction tx = Transaction.open(null)) {
			int c = stack.getCount();
			int is = handler.insert(id, ItemResource.of(stack), stack.getCount(), tx);
			if (is != 0)notifyChange();
			if (c == is)return ItemStack.EMPTY;
			return stack.copyWithCount(c - is);
		}
	}

	public boolean transferTo(int amount, InventorySlot to) {
		var resource = handler.getResource(id);
		int extracted, inserted;
		try (Transaction tx = Transaction.open(null)) {
			extracted = handler.extract(id, resource, amount, tx);
			if (extracted == 0)return false;
			inserted = to.handler.insert(to.id, resource, extracted, tx);
			if (inserted == extracted) {
				tx.commit();
				notifyChange();
				to.notifyChange();
				return true;
			}
		}
		if (inserted > 0) {//Try inserting less
			try (Transaction tx = Transaction.open(null)) {
				int ins = handler.extract(id, resource, inserted, tx);
				int ex = to.handler.insert(to.id, resource, ins, tx);
				if (ex == ins) {
					tx.commit();
					notifyChange();
					to.notifyChange();
					return true;
				}
			}
		}
		return false;
	}

	private void notifyChange() {
		notifier.onSlotChanged(this);
	}

	protected ResourceHandler<ItemResource> getHandler() {
		return handler;
	}

	protected int getId() {
		return id;
	}
}