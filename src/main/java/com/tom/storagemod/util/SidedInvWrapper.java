package com.tom.storagemod.util;

import java.util.Objects;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

public class SidedInvWrapper implements IItemHandler {
	protected final SidedInventory inv;
	protected final Direction side;

	public SidedInvWrapper(SidedInventory inv, Direction side)
	{
		this.inv = inv;
		this.side = side;
	}

	public static int getSlot(SidedInventory inv, int slot, Direction side)
	{
		int[] slots = inv.getAvailableSlots(side);
		if (slot < slots.length)
			return slots[slot];
		return -1;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SidedInvWrapper that = (SidedInvWrapper) o;

		return inv.equals(that.inv) && side == that.side;
	}

	@Override
	public int hashCode()
	{
		int result = inv.hashCode();
		result = 31 * result + Objects.hashCode(side);
		return result;
	}

	@Override
	public int getSlots()
	{
		return inv.getAvailableSlots(side).length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		int i = getSlot(inv, slot, side);
		return i == -1 ? ItemStack.EMPTY : inv.getStack(i);
	}

	@Override

	public ItemStack insertItem(int slot,  ItemStack stack, boolean simulate)
	{
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		int slot1 = getSlot(inv, slot, side);

		if (slot1 == -1)
			return stack;

		ItemStack stackInSlot = inv.getStack(slot1);

		int m;
		if (!stackInSlot.isEmpty())
		{
			if (stackInSlot.getCount() >= Math.min(stackInSlot.getMaxCount(), getSlotLimit(slot)))
				return stack;

			if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
				return stack;

			if (!inv.canInsert(slot1, stack, side) || !inv.isValid(slot1, stack))
				return stack;

			m = Math.min(stack.getMaxCount(), getSlotLimit(slot)) - stackInSlot.getCount();

			if (stack.getCount() <= m)
			{
				if (!simulate)
				{
					ItemStack copy = stack.copy();
					copy.increment(stackInSlot.getCount());
					setInventorySlotContents(slot1, copy);
				}

				return ItemStack.EMPTY;
			}
			else
			{
				// copy the stack to not modify the original one
				stack = stack.copy();
				if (!simulate)
				{
					ItemStack copy = stack.split(m);
					copy.increment(stackInSlot.getCount());
					setInventorySlotContents(slot1, copy);
					return stack;
				}
				else
				{
					stack.decrement(m);
					return stack;
				}
			}
		}
		else
		{
			if (!inv.canInsert(slot1, stack, side) || !inv.isValid(slot1, stack))
				return stack;

			m = Math.min(stack.getMaxCount(), getSlotLimit(slot));
			if (m < stack.getCount())
			{
				// copy the stack to not modify the original one
				stack = stack.copy();
				if (!simulate)
				{
					setInventorySlotContents(slot1, stack.split(m));
					return stack;
				}
				else
				{
					stack.decrement(m);
					return stack;
				}
			}
			else
			{
				if (!simulate)
					setInventorySlotContents(slot1, stack);
				return ItemStack.EMPTY;
			}
		}

	}

	private void setInventorySlotContents(int slot, ItemStack stack) {
		inv.markDirty(); //Notify vanilla of updates, We change the handler to be responsible for this instead of the caller. So mimic vanilla behavior
		inv.setStack(slot, stack);
	}

	@Override

	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if (amount == 0)
			return ItemStack.EMPTY;

		int slot1 = getSlot(inv, slot, side);

		if (slot1 == -1)
			return ItemStack.EMPTY;

		ItemStack stackInSlot = inv.getStack(slot1);

		if (stackInSlot.isEmpty())
			return ItemStack.EMPTY;

		if (!inv.canExtract(slot1, stackInSlot, side))
			return ItemStack.EMPTY;

		if (simulate)
		{
			if (stackInSlot.getCount() < amount)
			{
				return stackInSlot.copy();
			}
			else
			{
				ItemStack copy = stackInSlot.copy();
				copy.setCount(amount);
				return copy;
			}
		}
		else
		{
			int m = Math.min(stackInSlot.getCount(), amount);
			ItemStack ret = inv.removeStack(slot1, m);
			inv.markDirty();
			return ret;
		}
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return inv.getMaxCountPerStack();
	}

	@Override
	public boolean isItemValid(int slot,  ItemStack stack)
	{
		int slot1 = getSlot(inv, slot, side);
		return slot1 == -1 ? false : inv.isValid(slot1, stack);
	}
}
