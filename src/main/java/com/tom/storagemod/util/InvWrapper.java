package com.tom.storagemod.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class InvWrapper implements IItemHandler {
	private Inventory inv;
	private InvWrapper(Inventory inv) {
		this.inv = inv;
	}

	public static IItemHandler wrap(Inventory inv, Direction side) {
		if(inv instanceof SidedInventory) {
			return new SidedInvWrapper((SidedInventory) inv, side);
		} else return new InvWrapper(inv);
	}

	@Override
	public int getSlots() {
		return inv.size();
	}

	@Override
	public ItemStack getStackInSlot(int paramInt) {
		return inv.getStack(paramInt);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack stackInSlot = inv.getStack(slot);


		if (!stackInSlot.isEmpty()) {

			if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxCount(), getSlotLimit(slot))) {
				return stack;
			}
			if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot)) {
				return stack;
			}
			if (!inv.isValid(slot, stack)) {
				return stack;
			}
			int m = Math.min(stack.getMaxCount(), getSlotLimit(slot)) - stackInSlot.getCount();

			if (stack.getCount() <= m) {

				if (!simulate) {

					ItemStack copy = stack.copy();
					copy.increment(stackInSlot.getCount());
					inv.setStack(slot, copy);
					inv.markDirty();
				}

				return ItemStack.EMPTY;
			}



			stack = stack.copy();
			if (!simulate) {

				ItemStack copy = stack.split(m);
				copy.increment(stackInSlot.getCount());
				inv.setStack(slot, copy);
				inv.markDirty();
				return stack;
			}


			stack.decrement(m);
			return stack;
		}




		if (!inv.isValid(slot, stack)) {
			return stack;
		}
		int m = Math.min(stack.getMaxCount(), getSlotLimit(slot));
		if (m < stack.getCount()) {


			stack = stack.copy();
			if (!simulate) {

				inv.setStack(slot, stack.split(m));
				inv.markDirty();
				return stack;
			}


			stack.decrement(m);
			return stack;
		}



		if (!simulate) {

			inv.setStack(slot, stack);
			inv.markDirty();
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		ItemStack stackInSlot = inv.getStack(slot);

		if (stackInSlot.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (simulate) {

			if (stackInSlot.getCount() < amount)
			{
				return stackInSlot.copy();
			}


			ItemStack copy = stackInSlot.copy();
			copy.setCount(amount);
			return copy;
		}



		int m = Math.min(stackInSlot.getCount(), amount);

		ItemStack decrStackSize = inv.removeStack(slot, m);
		inv.markDirty();
		return decrStackSize;
	}

	@Override
	public int getSlotLimit(int paramInt) {
		return inv.getMaxCountPerStack();
	}

	@Override
	public boolean isItemValid(int paramInt, ItemStack paramItemStack) {
		return inv.isValid(paramInt, paramItemStack);
	}

}
