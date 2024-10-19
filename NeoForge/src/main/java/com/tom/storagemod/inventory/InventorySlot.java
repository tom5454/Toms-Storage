package com.tom.storagemod.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.inventory.IInventoryAccess.IChangeNotifier;

public class InventorySlot {
	private final IItemHandler handler;
	private final IChangeNotifier notifier;
	private final int id;

	public InventorySlot(IItemHandler h, IChangeNotifier notifier, int i) {
		this.handler = h;
		this.notifier = notifier;
		this.id = i;
	}

	public ItemStack getStack() {
		return handler.getStackInSlot(id);
	}

	public ItemStack extract(int amount) {
		ItemStack is = handler.extractItem(id, amount, false);
		if (!is.isEmpty())notifyChange();
		return is;
	}

	public ItemStack insert(ItemStack stack) {
		int c = stack.getCount();
		ItemStack is = handler.insertItem(id, stack, false);
		if (c != is.getCount())notifyChange();
		return is;
	}

	public boolean transferTo(int amount, InventorySlot to) {
		ItemStack is = handler.extractItem(id, amount, true);
		if (is.isEmpty())return false;
		int ex = is.getCount();
		is = to.handler.insertItem(to.id, is, true);
		if (is.isEmpty()) {
			is = handler.extractItem(id, amount, false);
			to.handler.insertItem(to.id, is, false);
			notifyChange();
			to.notifyChange();
			return true;
		} else if (is.getCount() < ex) {//Try inserting less
			int ins = ex - is.getCount();
			is = handler.extractItem(id, ins, true);
			is = to.handler.insertItem(to.id, is, true);
			if (is.isEmpty()) {
				is = handler.extractItem(id, ins, false);
				to.handler.insertItem(to.id, is, false);
				notifyChange();
				to.notifyChange();
				return true;
			}
		}
		return false;
	}

	private void notifyChange() {
		notifier.onSlotChanged(this);
	}

	protected IItemHandler getHandler() {
		return handler;
	}

	protected int getId() {
		return id;
	}
}